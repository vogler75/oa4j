/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.base;

import at.rocworks.oa4j.logger.data.lists.DataList;
import at.rocworks.oa4j.logger.data.Dp;
import at.rocworks.oa4j.logger.query.DpGetPeriodResult;
import at.rocworks.oa4j.base.JDebug;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.Set;
import java.util.logging.Level;
import org.zeromq.ZMQ;

/**
 *
 * @author vogler
 */
public class NoSQLZeroMQ extends NoSQLServer {
  
    private final ZMQ.Context context;
    private final ZMQ.Socket sender;
    private final int qtype;
        
    public static NoSQLServer createServer(NoSQLSettings srvcfg, String srvprefix) {
        int port = srvcfg.getIntProperty(srvprefix, "port", 5557);
        String qtype = srvcfg.getStringProperty(srvprefix, "qtype", "pub").toLowerCase();
        int iothreads = srvcfg.getIntProperty(srvprefix, "iothreads", 1);
        JDebug.out.log(Level.CONFIG, "{0}.type: {1} \n{2}\nport: {3}\nqtype: {4}\niothreads: {5}",
                new Object[]{srvprefix, NoSQLZeroMQ.class.getName(), srvcfg, port, qtype, iothreads});
        return new NoSQLZeroMQ(srvcfg, port, qtype, iothreads);
    }  
    
    public NoSQLZeroMQ(NoSQLSettings settings, int port, String qtype, int iothreads) {
        super(settings);
        JDebug.out.info("zeromq init storage...");
        this.qtype=qtype.equals("pub") ? ZMQ.PUB : ZMQ.PUSH;
        context = ZMQ.context(iothreads);
        sender = context.socket(this.qtype);
        sender.bind("tcp://*:"+port);
        JDebug.out.info("zeromq init storage...done");
    }    
    
    @Override
    public boolean dpGetPeriod(Date t1, Date t2, Dp dp, Set<String> configs, DpGetPeriodResult result) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override   
    public int storeData(DataList list) {
        Date t1 = new Date();        
        try (ByteArrayOutputStream data = new ByteArrayOutputStream();
                ObjectOutput object = new ObjectOutputStream(data)) {
            object.writeObject(list);
            if ( qtype == ZMQ.PUB) {
                sender.sendMore("DataList"); 
            }
            sender.send(data.toByteArray()); 
        } catch (IOException ex) {
            JDebug.StackTrace(Level.SEVERE, ex);
        }                                

        Date t2 = new Date();
        addServerStats(list.getHighWaterMark(), t2.getTime()-t1.getTime());   
        return INoSQLInterface.OK;        
    }
}
