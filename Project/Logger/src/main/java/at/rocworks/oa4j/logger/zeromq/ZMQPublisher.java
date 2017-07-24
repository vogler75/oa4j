/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.zeromq;

import at.rocworks.oa4j.base.JDebug;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import org.zeromq.ZMQ;

/**
 *
 * @author vogler
 */
public class ZMQPublisher {
    private final int iothreads=1;
    
    private ZMQ.Context context;
    private ZMQ.Socket sender;
    
    private final int zmqtype;
    private final String endpoint;

    public ZMQPublisher(int port) {
        this(port, ZMQ.PUB);
    }    
    
    public ZMQPublisher(int port, int zmqtype /*PUB|PUSH*/) {
        this.zmqtype = zmqtype;
        this.endpoint="tcp://*:"+port;      
    }
    
    public void start() {
        context = ZMQ.context(iothreads);
        sender = context.socket(this.zmqtype);          
        sender.bind(endpoint);
    }
    
    public void stop() {
        sender.close();
        context.term();
    }
    
    public boolean publish(Object object) { // no topic with push/pull
        return publish("", object);
    }
    
    public boolean publish(String topic, Object object) {
        try (ByteArrayOutputStream data = new ByteArrayOutputStream();
                ObjectOutput output = new ObjectOutputStream(data)) {
            
            if ( zmqtype == ZMQ.PUB) {
                output.writeObject(object);
                sender.sendMore(topic); 
                sender.send(data.toByteArray());
            } else if ( zmqtype == ZMQ.PUSH ) {
                output.writeObject(new Object[]{topic, object});
                sender.send(data.toByteArray());                                             
            }
            
            return true;
        } catch (IOException ex) {
            JDebug.StackTrace(Level.SEVERE, ex);
            return false;
        }                                 
    }       
}
