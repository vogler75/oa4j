/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.logger;

import at.rocworks.oa4j.base.JDebug;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level; 

/**
 *
 * @author vogler
 */
public class Utilities {
    static public String getConfigFile(String[] args) {
        String configFile="config.logger";
        StringBuilder options = new StringBuilder();
        for ( int i=0; i<args.length; i++ ) {
            options.append(String.format("\narg[%d] => %s", i, args[i]));
            //JDebug.out.log(Level.INFO, "arg[{0}] => {1}", new Object[]{i, args[i]});
            if (args[i].equals("-logger") && i+1<args.length)
                configFile=args[i+1];
        }
        //JDebug.out.log(Level.INFO, options.toString());
        return configFile;
    }    
        
    static public Properties loadProperties(String configFile) throws IOException {
        Properties properties = new Properties();
        String file = System.getProperty("user.dir")+"/"+configFile;
        JDebug.out.log(Level.INFO, "config={0} ", new Object[]{file});
        try (final BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file))) {
            properties.load(stream);
        }
        return properties;
    }    
    
    static public Level getDebugLevel(Properties properties) {
        return getDebugLevel(properties, "general");
    }
    
    static public Level getDebugLevel(Properties properties, String section) {
        // Logging Level 
        String dbg = properties.getProperty(section+".debug", "severe").toUpperCase();
        switch (dbg) {
            case "SEVERE":
                return(Level.SEVERE);
            case "WARNING":
                return(Level.WARNING);
            case "INFO":
                return(Level.INFO);
            case "CONFIG":
                return(Level.CONFIG);
            case "FINE":
                return(Level.FINE);
            case "FINER":
                return(Level.FINER);
            case "FINEST":
                return(Level.FINEST);
            default:
                return(Level.SEVERE);
        }        
    }
}
