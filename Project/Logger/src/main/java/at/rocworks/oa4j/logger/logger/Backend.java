/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.logger;

import at.rocworks.oa4j.base.JDebug;
import at.rocworks.oa4j.base.JManager;
import at.rocworks.oa4j.logger.base.NoSQLSettings;
import at.rocworks.oa4j.logger.data.Metrics;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;


/**
 *
 * @author vogler
 */
public class Backend {

    private JManager manager;
    private DataSink sink;

    public static void main(String[] args) throws Exception {
        new Backend().start(args); 
    }

    public void start(String[] args) throws Exception {    
        try {
            // Init
            Properties properties = Utilities.loadProperties(Utilities.getConfigFile(args));
            NoSQLSettings settings = new NoSQLSettings(properties, "logger");

            // API
            manager = new JManager();
            manager.init(args);
            if ( manager.isEnabled() ) {
                manager.start();
            } else {
                JDebug.out.warning("Cannot initialize WinCC OA API Manager!");
                JDebug.setOutput(properties.getProperty("general.logfile", this.getClass().getSimpleName()));
                //System.exit(-1);
            }

            // JDebug
            JDebug.setLevel(Utilities.getDebugLevel(properties));

            // Databases
            (sink = new DataSink(settings)).start();
            
            // Frontend
            String type = settings.getStringProperty("frontend.zeromq", "qtype", "sub").toLowerCase();        
            String endpoint = settings.getStringProperty("frontend.zeromq", "endpoint", "tcp://*:5557");
            int iothreads = settings.getIntProperty("frontend.zeromq", "iothreads", 1);
            JDebug.out.info("ZeroMQ..."+type+"/"+endpoint+"/"+iothreads);
            (new DataWellZeroMQ(type, endpoint, iothreads, sink.getLogger())).start();
                               
            // Statistics
            int delay = settings.getIntProperty("logger", "statistics", 0);
            boolean pretty = settings.getBoolProperty("logger", "statistics.pretty", false);
            if ( delay > 0 ) new Thread(()->showStats("backend", delay, pretty)).start();
            
            // Shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(()->{
                synchronized ( Backend.this ) { Backend.this.notifyAll(); }
            }));  
            //Thread.sleep(30000);
            synchronized ( Backend.this ) { Backend.this.wait(); }   
            JDebug.out.info("Shutdown...");
                                    
        } catch (IOException | InterruptedException ex) {
            JDebug.StackTrace(Level.SEVERE, ex);
        }
        manager.stop();
        JDebug.out.info("Backend done.");
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