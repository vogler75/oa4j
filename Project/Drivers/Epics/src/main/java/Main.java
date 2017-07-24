import at.rocworks.oa4j.driver.JDriverItem;
import at.rocworks.oa4j.driver.JDriverItemList;
import at.rocworks.oa4j.driver.JDriverSimple;
import at.rocworks.oa4j.driver.JTransFloatVar;
import at.rocworks.oa4j.driver.JTransIntegerVar;
import at.rocworks.oa4j.driver.JTransTextVar;

import at.rocworks.oa4j.base.JDebug;

import gov.aps.jca.CAException;
import gov.aps.jca.CAStatus;
import gov.aps.jca.Channel;
import gov.aps.jca.Context;
import gov.aps.jca.JCALibrary;
import gov.aps.jca.Monitor;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.DOUBLE;
import gov.aps.jca.dbr.INT;
import gov.aps.jca.dbr.STRING;
import gov.aps.jca.event.ConnectionEvent;
import gov.aps.jca.event.ConnectionListener;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

import java.io.UnsupportedEncodingException;

import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author vogler
 */
public class Main {
    public static void main(String[] args) throws Exception {                        
        new Main().start(args);
    }            
    
    public void start(String[] args) {
        try {
            Driver driver = new Driver(args);
            JDebug.setLevel(Level.INFO);
            driver.startup();
            JDebug.out.info("ok");
        } catch ( Exception ex ) {
            JDebug.StackTrace(Level.SEVERE, ex);
        }
    }    
    
    public class Driver extends JDriverSimple {

        // Epics Connection
        JCALibrary jca;
        Context ctxt;
        EpicsConnectionListener connectionListener;   
        EpicsMonitorListener monitorListener;
        
        ConcurrentHashMap<String, Channel> outputChannels = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Channel> inputChannels = new ConcurrentHashMap<>();        
       
        public Driver(String[] args) throws Exception {
            super(args, 60);
        }    
        
        @Override
        public boolean start() {    
            try {                
                connectEpics();                                                                  
                return super.start();                  
            } catch (CAException ex) {
                JDebug.StackTrace(Level.SEVERE, ex);
                return false;
            }          
        }

        private void connectEpics() throws CAException {
            jca = JCALibrary.getInstance();
            ctxt = jca.createContext("com.cosylab.epics.caj.CAJContext");
            connectionListener = new EpicsConnectionListener(this);
            monitorListener = new EpicsMonitorListener();
        }       

        @Override
        public void sendOutputBlock(JDriverItemList data) {
            try {
                JDriverItem item;
                while ((item=data.pollFirst())!=null) {
                    ((EpicsDriverItem)item).sendToChannel(outputChannels.get(item.getName()));
                }
                ctxt.flushIO();                
            } catch (CAException | IllegalStateException ex) {
                JDebug.StackTrace(Level.SEVERE, ex);                
            }
        }
        
        @Override
        public JDriverItem newOutputItem(String address, int trans, byte[] data, Date time) {
            return new EpicsDriverItem(address, trans, data, time);
        }            
        
        @Override
        protected boolean attachInput(String addr) {
//            JDebug.out.info("attachInput "+addr);
            if (ctxt!=null) {            
                try {
                    Channel channel = ctxt.createChannel(addr, connectionListener);                                                                        
                    ctxt.flushIO();
                    inputChannels.put(addr, channel);
                    return true;
                } catch (CAException | IllegalArgumentException | IllegalStateException ex) {
                    JDebug.StackTrace(Level.SEVERE, ex);
                    return false;
                }
            } else {
                return false;
            }
        }
        
        @Override
        protected boolean attachOutput(String addr) {
            if (ctxt!=null) {
                try {
                    Channel channel = ctxt.createChannel(addr);                    
                    ctxt.flushIO();
                    outputChannels.put(addr, channel);
                    return true;
                } catch (CAException | IllegalArgumentException | IllegalStateException ex) {
                    JDebug.StackTrace(Level.SEVERE, ex);
                    return false;
                }
            } else {
                return false;
            }
        }        

        @Override
        public boolean detachInput(String addr) {
            connectionListener.removeMonitoredAddr(addr);
            return detachChannel(inputChannels, addr);            
        }

        @Override
        public boolean detachOutput(String addr) {
            return detachChannel(outputChannels, addr);
        }
        
        private boolean detachChannel(ConcurrentHashMap<String, Channel> channels, String addr) {
            Channel channel = channels.get(addr);
            if (ctxt!=null && channel!=null) {
                try {                                
                    channel.destroy();
                    return true;
                } catch (CAException | IllegalStateException ex) {
                    JDebug.StackTrace(Level.SEVERE, ex);
                    return false;
                }
            } else {
                return true;
            }
        }        
        
        private class EpicsDriverItem extends JDriverItem {      
            public EpicsDriverItem(String name, int ttnr, byte[] data, Date time) {
                super(name, ttnr, data, time);
            }
            
            public EpicsDriverItem(String name, DBR dbr) throws UnsupportedEncodingException {
                super();
                this.name=name;            
                if ( dbr.getType() == DBRType.DOUBLE ) {
                    double[] value = ((DOUBLE) dbr).getDoubleValue();
                    data = JTransFloatVar.toPeriph(value[0]);
//                    JDebug.out.info(this.name+": "+value[0]+" "+value.length);
                } else if ( dbr.getType() == DBRType.INT ) {
                    int[] value = ((INT) dbr).getIntValue();
                    data = JTransIntegerVar.toPeriph(value[0]);
//                    JDebug.out.info(this.name+": "+value[0]+" "+value.length);
                } else if ( dbr.getType() == DBRType.STRING ) {
                    String[] value = ((STRING) dbr).getStringValue();
                    data = JTransTextVar.toPeriph(value[0]); 
//                    JDebug.out.info(this.name+": "+value[0]+" "+value.length);
                } else {
                    JDebug.out.log(Level.WARNING, "MyDriverItem dbr-type {0} unhandled!", dbr.getType());
                }               
            }            
            
            public boolean sendToChannel(Channel channel) throws CAException {
                if (channel == null || channel.getConnectionState() != Channel.CONNECTED) 
                    return false;
                if ( channel.getFieldType() == DBRType.DOUBLE ) {
                    Double val = JTransFloatVar.toVal(this.getData());
                    channel.put(val);
                } else if ( channel.getFieldType() == DBRType.INT ) {
                    Integer val = JTransIntegerVar.toVal(this.getData());
                    channel.put(val);
                } else if ( channel.getFieldType() == DBRType.STRING ) {
                    String val = JTransTextVar.toVal(this.getData());
                    channel.put(val);
                } else {
                    JDebug.out.log(Level.WARNING, "MyDriverItem dbr-type {0} unhandled!", channel.getFieldType());
                    return false;
                }
                return true;
            }
        }        
                   
        public class EpicsConnectionListener implements ConnectionListener {

            private final JDriverSimple driver;
            
            private final HashSet<String> isAddrMonitored = new HashSet<>();
            public void removeMonitoredAddr(String addr) {
                isAddrMonitored.remove(addr);
            }
            
            public EpicsConnectionListener(JDriverSimple driver) {
                this.driver=driver;
            }
            
            @Override
            public void connectionChanged(ConnectionEvent ev) {
                Channel channel = (Channel) ev.getSource();
                Context ctxt = channel.getContext();
                String addr = channel.getName();
                
                if (channel.getConnectionState() == Channel.CONNECTED) {
                    JDebug.out.log(Level.INFO, "Channel {0} is now connected (state={1})", new Object[]{channel.getName(), channel.getConnectionState()});                    
                      
                    if ( !isAddrMonitored.contains(addr) ) {
                        isAddrMonitored.add(addr);                        
                        JDebug.out.log(Level.INFO, "Channel {0} is now monitored (state={1})", new Object[]{channel.getName(), channel.getConnectionState()});                    
                        try {
                            // Add a monitor listener and flush          
                            channel.addMonitor(channel.getFieldType(), 
                                            1, Monitor.VALUE /*| Monitor.LOG | Monitor.ALARM*/,
                                            /*new EpicsMonitorListener()*/monitorListener);     
                            ctxt.flushIO();
                        } catch (CAException | IllegalStateException ex) {
                            JDebug.StackTrace(Level.SEVERE, ex);
                        }
                    }                 
                } else {
                    JDebug.out.log(Level.WARNING, "Channel {0} is not connected (state={1})", new Object[]{channel.getName(), channel.getConnectionState()});
                }
            }
        };

        private class EpicsMonitorListener implements MonitorListener {            
            Date tLast = new Date();
            JDriverItemList inputItemList = new JDriverItemList();                                    
            
            @Override
            public void monitorChanged(MonitorEvent ev) {                
                Channel ch = (Channel) ev.getSource();
                // Check the status
                if (ev.getStatus() != CAStatus.NORMAL) {
                    JDebug.out.warning("monitorChanged: Bad status");
                }
                // Get the value from the DBR
                try {
                    EpicsDriverItem item = new EpicsDriverItem(ch.getName(), ev.getDBR());  
                    inputItemList.addItem(item);
                    Date tNow=new Date();
//                    if ( inputItemList.getSize() >= 512 || tNow.getTime()-tLast.getTime()>=100) {
                        sendInputBlock(inputItemList);                            
                        inputItemList=new JDriverItemList();
                        tLast=tNow;
//                    }                    
                } catch (IllegalStateException | UnsupportedEncodingException ex) {
                    JDebug.StackTrace(Level.SEVERE, ex);
                }                        
            }
        }
    }
}
