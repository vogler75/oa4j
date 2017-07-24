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
public class ZMQRequester {
    private final int iothreads=1;
    private final String endpoint;    
    
    private ZMQ.Context context;
    private ZMQ.Socket requester;    
    
    public ZMQRequester(int port) {
        this.endpoint="tcp://*:"+port;
    }
    
    public void start() {
        context = ZMQ.context(iothreads);
        requester = context.socket(ZMQ.REQ);   
        requester.connect(endpoint);        
    }        
    
    public void stop() {
        requester.close();
        context.term();
    }
    
    public Object request(String topic) {
        return request(topic, null);
    }
    public Object request(String topic, Object object) {        
        JDebug.out.log(Level.INFO, "Request: {0} {1}", new Object[]{topic, object});
        
        try (ByteArrayOutputStream requestData = new ByteArrayOutputStream();
                ObjectOutput requestOutput = new ObjectOutputStream(requestData)) {
            Object[] message = new Object[]{topic, object};
            requestOutput.writeObject(message);
            requester.send(requestData.toByteArray());
            
            byte[] reply = requester.recv();                   
            try (ByteArrayInputStream responseData = new ByteArrayInputStream(reply);
                    ObjectInput responseInput = new ObjectInputStream(responseData)) {
                Object response = responseInput.readObject();
                JDebug.out.log(Level.INFO, "Response: {0}", response);
                return response;
            } catch (IOException | ClassNotFoundException ex) {
                JDebug.StackTrace(Level.SEVERE, ex);
            }                        
        } catch (IOException ex) {
            JDebug.StackTrace(Level.SEVERE, ex);
        }                    
        
        return null;
    }
}
