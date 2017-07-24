/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.zeromq;

import at.rocworks.oa4j.base.JDebug;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import org.zeromq.ZMQ;

/**
 *
 * @author vogler
 */
public class ZMQSubscriber {
    private ZMQ.Context context;
    private ZMQ.Socket receiver;
    private int zmqtype;
    private String endpoint;
    
    private Thread thread;
    private final HashMap<String, IZMQCallback> callbacks = new HashMap<>();

    public ZMQSubscriber(int port) {
        this(port, ZMQ.SUB);
    }    
    
    public ZMQSubscriber(int port, int zmqtype /*SUB|PULL*/) {
        this.zmqtype = zmqtype;
        this.endpoint = "tcp://*:"+port;
        context = ZMQ.context(1);
        receiver = context.socket(this.zmqtype);
    }        

    public void start() {
        receiver.connect(endpoint);        
        switch ( zmqtype ) {
           case ZMQ.SUB: 
               (thread=new Thread(()->{subscribeThread();})).start();
               break;
           case ZMQ.PULL: 
               (thread=new Thread(()->{pullThread();})).start();
               break;
       }    
    }
    
    public void stop() {
        thread.interrupt();
    }
    
    public void subscribe(List<String> topics, IZMQCallback callback) {
        synchronized ( callbacks ) {
            topics.forEach((topic)->subscribe(topic, callback));
        }
    }    
    
    public void subscribe(String topic, IZMQCallback callback) {
        synchronized ( callbacks ) {
            if (!callbacks.containsKey(topic)) {
                callbacks.put(topic, callback);
                if ( zmqtype == ZMQ.SUB )
                    receiver.subscribe(topic.getBytes());
            }
        }
    }
    
    public void unsubscribe(String topic) {
        synchronized ( callbacks ) {        
            if (callbacks.containsKey(topic)) {
                receiver.unsubscribe(topic.getBytes());
                callbacks.remove(topic);
            }
        }
    }

    private void subscribeThread() {
        while (!Thread.currentThread().isInterrupted ()) {
            String topic = receiver.recvStr();
            byte[] bytes = receiver.recv();
            //JDebug.out.info("recv block...got");
            try (ByteArrayInputStream data = new ByteArrayInputStream(bytes);
                    ObjectInput in = new ObjectInputStream(data)) {
                Object object = in.readObject();
                synchronized ( callbacks ) {
                    if ( callbacks.containsKey(topic) )
                        callbacks.get(topic).message(topic, object);
                }
            } catch (IOException | ClassNotFoundException ex) {
                JDebug.StackTrace(Level.SEVERE, ex);
            }
        }
        receiver.close();        
    }
    
    private void pullThread() {
        while (!Thread.currentThread().isInterrupted ()) {
            byte[] bytes = receiver.recv();
            try (ByteArrayInputStream data = new ByteArrayInputStream(bytes);
                    ObjectInput in = new ObjectInputStream(data)) {
                Object message[] = (Object[])in.readObject();
                final String topic = (String)message[0];
                final Object object = message[1];
                callbacks.forEach((name, callback)->{
                    if (topic.equals(name))
                        callback.message(topic, object);
                });
            } catch (IOException | ClassNotFoundException ex) {
                JDebug.StackTrace(Level.SEVERE, ex);
            }
        }
        receiver.close();        
    }        
        
}
