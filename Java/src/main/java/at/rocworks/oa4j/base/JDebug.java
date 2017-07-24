/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.base;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

/**
 *
 * @author vogler
 */
public class JDebug {
    
    public static final java.util.logging.Logger out = java.util.logging.Logger.getLogger(JDebug.class.getName());
    public static final String fmtDate = "yyyy.MM.dd HH:mm:ss.SSS";

    private static class LogFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            SimpleDateFormat fmt = new SimpleDateFormat(JDebug.fmtDate);
            String formattedMessage = formatMessage(record);
            return fmt.format(new Date()) + " " + String.format("%-60s %-7s", record.getSourceClassName() + "." + record.getSourceMethodName(), record.getLevel()) + ": " + formattedMessage + "\n";
        }
    }

    public static boolean setConsole() {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new LogFormatter());
        out.addHandler(handler);
        out.setUseParentHandlers(false);
        return true;
    }

    public static boolean setOutput(String fqn)  {
        try {
            FileHandler fh = new FileHandler(fqn + ".%g.log", 5242880, 5, true);
            fh.setFormatter(new LogFormatter());
            out.addHandler(fh);
            out.setUseParentHandlers(false);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static void setOutput(String path, String filename)  {
        if (!setOutput(path+filename))
            setOutput(filename);
    }
    
    public static void setLevel(Level level) {
        JDebug.out.setLevel(level);
    }    
    
    public static void StackTrace(Level level, Exception ex) {
        String trace = ex.toString()+":\n";
        for (StackTraceElement ste : ex.getStackTrace()) {
                trace += "  " + ste.getClassName() + "." + ste.getMethodName() + ":" + ste.getLineNumber()+"\n";
        }
        JDebug.out.log(level, trace);
    }    
    
    public static void StackTrace(Level level, String msg) {
        String trace = msg+":\n";
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
                trace += "  " + ste.getClassName() + "." + ste.getMethodName() + ":" + ste.getLineNumber()+"\n";
        }
        JDebug.out.log(level, trace);
    }        
    
    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            JDebug.StackTrace(Level.SEVERE, ex);
        }
    }    
}

//-----------------------------------------------------------------------------------------
