/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.zeromq;

import at.rocworks.oa4j.base.JDebug;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import org.zeromq.ZMQ;

/**
 *
 * @author vogler
 */
public class ZMQListener {
    private final int iothreads=1;
    private final String endpoint;    
    
    private ZMQ.Context context;
    private ZMQ.Socket listener;
    
    private Thread thread;
    private IZMQCallback callback;
    
    public ZMQListener(int port) {
        this.endpoint="tcp://*:"+port;
    }   
    
    public void start(IZMQCallback callback) {
        this.context = ZMQ.context(iothreads);        
        this.listener = context.socket(ZMQ.REP);        
        this.callback = callback;
        (thread=new Thread(()->{listener();})).start();
    }
    
    public void stop() {
        thread.interrupt();
    }
    
    private void listener() {       
        listener.bind(endpoint);
        while (!Thread.currentThread().isInterrupted()) {
            //  Wait for next request from client
            byte[] bytes = listener.recv();
            
            try (ByteArrayInputStream requestData = new ByteArrayInputStream(bytes);
                    ObjectInput requestInput = new ObjectInputStream(requestData)) {
                Object message = requestInput.readObject();
                if ( message instanceof Object[] ) {
                    String topic = (String)((Object[])message)[0];
                    Object object = ((Object[])message)[1];
                    JDebug.out.log(Level.INFO, "Request: {0} {1}", new Object[]{topic, object != null ? object.toString() : "null"});
                    Object response = callback.message(topic, object);
                    JDebug.out.log(Level.INFO, "Response: {0}", response);

                    try (ByteArrayOutputStream responseData = new ByteArrayOutputStream();
                            ObjectOutput responseOutput = new ObjectOutputStream(responseData)) {
                        responseOutput.writeObject(response);                    
                        listener.send(responseData.toByteArray());
                    } catch (IOException ex) {
                        JDebug.StackTrace(Level.SEVERE, ex);
                    }                             
                } else {
                    JDebug.out.severe("Message is not an instance of Object[}!");
                }                                
            } catch (IOException | ClassNotFoundException ex) {
                JDebug.StackTrace(Level.SEVERE, ex);
            }                            
        }
        listener.close();  
        context.term();
    }            
}
