/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.logger;

import at.rocworks.oa4j.logger.base.NoSQLSettings;
import at.rocworks.oa4j.logger.data.Metrics;
import at.rocworks.oa4j.base.JDebug;

import java.util.Properties;
import java.util.logging.Level;

/**
 *
 * @author vogler
 */
public class Frontend {

    private FrontendManager manager;  
    private DataWellFilter archives;
    
    private DataSink sink;
    
    public static void main(String[] args) throws Exception {
        new Frontend().start(args);
    }      
    
    public void start(String[] args) throws Exception {    
        try {
            // API
            manager = new FrontendManager();
            manager.init(args); 
            if ( !manager.isEnabled() ) {
                JDebug.out.info("Cannot initialize WinCC OA API Manager!");
                System.exit(-1);
            }                       
            
            // Configs
            Properties properties = Utilities.loadProperties(Utilities.getConfigFile(args));            
            NoSQLSettings settings = new NoSQLSettings(properties, "logger");
            
            // JDebug
            JDebug.setLevel(Utilities.getDebugLevel(properties));

            // Databases
            (sink = new DataSink(settings)).start();
            
            // Statistics
            int delay = settings.getIntProperty("logger", "statistics", 0);
            boolean pretty = settings.getBoolProperty("logger", "statistics.pretty", false);
            if ( delay > 0 ) new Thread(()->showStats("frontend", delay, pretty)).start();            
                        
            // Filter
            boolean filter = settings.getBoolProperty("frontend.winccoa", "filter", false);
            boolean internaldps = settings.getBoolProperty("frontend.winccoa", "internaldps", true);
            JDebug.out.log(Level.CONFIG, "filter={0} internaldps={1}", new Object[]{filter, internaldps});
            archives = new DataWellFilter(filter, internaldps);           
                       
            // Manager
            JDebug.out.info("Starting WinCC OA API Manager...");
            int bulksize = settings.getIntProperty("frontend.winccoa.bulksize", "bulksize", 0);            
            JDebug.out.log(Level.CONFIG, "bulksize={0}", new Object[]{bulksize});
            manager.start(sink.getLogger(), archives, bulksize);
            connectByAPIQuery(settings);
            connectByAPIAlerts(settings);
            JDebug.out.info("Started WinCC OA API Manager.");
            
            // Shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(()->{
                synchronized ( Frontend.this ) { Frontend.this.notifyAll(); }
            }));            
            synchronized ( Frontend.this ) { Frontend.this.wait(); }    
            JDebug.out.info("Shutdown...");
            manager.stop();

        } catch (Exception ex) {
            JDebug.StackTrace(Level.SEVERE, ex);
        }        
        JDebug.out.info("Frontend done.");
    }       
    
    private void connectByAPIQuery(NoSQLSettings settings) {        
        int i = 0;
        String query;
        do {
            query = settings.getStringProperty("frontend.winccoa", "query." + i, "");
            if (!query.isEmpty()) {
                manager.queryConnect(query);
            }
            i++;
        } while (!query.isEmpty());    
    }
    
    private void connectByAPIAlerts(NoSQLSettings settings) {             
        boolean alerts = settings.getBoolProperty("frontend.winccoa", "alerts", false);
        if ( alerts ) {                                    
            manager.alertConnect();
        }
    }        
    
    private void showStats(String dp, int delay, boolean pretty) {
        while ( true ) {
            try {
                Thread.sleep(delay * 1000);
            } catch (InterruptedException ex) {
                JDebug.StackTrace(Level.SEVERE, ex);
            }
            
            final Metrics stats = sink.getStats();                        
            JDebug.out.log(Level.INFO, "{0}", pretty ? stats.toJSONPrettyString() : stats.toJSONString());
        }
    }    
}
