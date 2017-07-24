/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.logger;

import at.rocworks.oa4j.logger.base.IDataCollector;
import at.rocworks.oa4j.logger.data.lists.DataList;
import at.rocworks.oa4j.logger.data.lists.IDataListImmutable;
import at.rocworks.oa4j.logger.data.types.protobuf.DataListProtobuf;
import at.rocworks.oa4j.logger.data.types.protobuf.DpeValueProtos;
import at.rocworks.oa4j.base.JDebug;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.logging.Level;

import org.zeromq.ZMQ;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.Date;

/**
 *
 * @author vogler
 */
public class DataWellZeroMQ {
    private final IDataCollector collector;
    private final String type;
    private final String endpoint;
    private final int iothreads;
    
    public DataWellZeroMQ(String type, String endpoint, int iothreads, IDataCollector collector) {
        this.type=type;
        this.endpoint=endpoint;
        this.iothreads=iothreads;
        this.collector=collector;
    }
    
    public void start() {
        connectByZeroMQ();   
    }
    
    private void connectByZeroMQ() {
        ZMQ.Context context;
        ZMQ.Socket receiver;        

        if ( Arrays.asList("sub","pull","pull.protobuf","router.protobuf").contains(type) && iothreads > 0 ) {
            context = ZMQ.context(iothreads);
            switch ( type ) {
                case "sub": 
                    JDebug.out.log(Level.INFO, "connectByZeroMQ subscribe to {0}", endpoint);
                    receiver = context.socket(ZMQ.SUB); 
                    receiver.connect(endpoint);
                    new Thread(()->connectByZeroMQSubscribe(receiver)).start();
                    break;
                case "pull": 
                    JDebug.out.log(Level.INFO, "connectByZeroMQ pull from {0}", endpoint);
                    receiver = context.socket(ZMQ.PULL); 
                    receiver.connect(endpoint);                    
                    new Thread(()->connectByZeroMQPull(receiver)).start();
                    break;
                case "pull.protobuf": 
                    JDebug.out.log(Level.INFO, "connectByZeroMQ pull.protobuf from {0}", endpoint);
                    receiver = context.socket(ZMQ.PULL); 
                    receiver.setRcvHWM(8);
                    //receiver.setReceiveBufferSize(64*1024);
                    receiver.connect(endpoint);   
                    new Thread(()->connectByZeroMQPullProtobuf(receiver)).start();
                    break;                       
                case "router.protobuf":
                    JDebug.out.log(Level.INFO, "connectByZeroMQ router.protobuf from {0}", endpoint);
                    receiver = context.socket(ZMQ.ROUTER); 
                    receiver.setRcvHWM(8);
                    receiver.connect(endpoint);   
                    new Thread(()->connectByZeroMQRouterProtobuf(receiver)).start();
                    break;                       
                    
            }    
        }
    }

    private void connectByZeroMQSubscribe(ZMQ.Socket receiver) {
        DataList block;
        receiver.subscribe("DataList".getBytes());
        while (!Thread.currentThread().isInterrupted ()) {
            String header = receiver.recvStr();
            if ( "DataList".equals(header) ) {
                byte[] bytes = receiver.recv();
                try (ByteArrayInputStream data = new ByteArrayInputStream(bytes);
                        ObjectInput in = new ObjectInputStream(data)) {
                    Object object = in.readObject();
                    if ( object instanceof DataList ) {
                        block = (DataList)object;
//                        for ( int i=0; i<=block.getHighWaterMark(); i++ ) {
//                            JDebug.out.info(block.getItem(i).toJSONObject().toJSONString());
//                        }
                        collector.collectData(block);
                    }
                } catch (IOException | ClassNotFoundException ex) {
                    JDebug.StackTrace(Level.SEVERE, ex);
                }
            }
        }
    }
    
    private void connectByZeroMQPull(ZMQ.Socket receiver) {
        //  Process tasks forever
        DataList block;
        while (!Thread.currentThread().isInterrupted ()) {            
            byte[] bytes = receiver.recv();
            try (ByteArrayInputStream data = new ByteArrayInputStream(bytes);
                    ObjectInput in = new ObjectInputStream(data)) {
                Object object = in.readObject();
                if ( object instanceof DataList ) {
                    block = (DataList)object;
                    collector.collectData(block);
//                    for ( int i=0; i<=block.getHighWaterMark(); i++ ) {
//                        JDebug.out.info(block.getItem(i).toJSONObject().toJSONString());
//                    }
                }
            } catch (IOException | ClassNotFoundException ex) {
                JDebug.StackTrace(Level.SEVERE, ex);
            }
        }
    }                   

    private void connectByZeroMQPullProtobuf(ZMQ.Socket receiver) {
        //  Process tasks forever
        int counter=0;
        IDataListImmutable block;
        Date t1=new Date();
        while (!Thread.currentThread().isInterrupted ()) {            
            //JDebug.out.info("waiting for block...");
            byte[] bytes = receiver.recv();         
            //JDebug.out.info("received one block...");
            try {
                Date pt1=new Date();
                DpeValueProtos.DpeValueList object = DpeValueProtos.DpeValueList.parseFrom(bytes);
                
                block = new DataListProtobuf(object);
                collector.collectData(block);                
                
                counter+=object.getDpeValueListCount();
                Date pt2=new Date();
                
                //JDebug.out.log(Level.INFO, "block size={0} processing time={1}", new Object[]{object.getDpeValueListCount(), ((pt2.getTime()-pt1.getTime())/1000.0)});
                //Thread.sleep(1000);
                //JDebug.out.info("protobuf:"+object.toString());
//                if ( counter > 100 ) {
//                    Date t2=new Date();                    
//                    JDebug.out.log(Level.INFO, "v={0} v/s={1}", new Object[]{counter, counter/((t2.getTime()-t1.getTime())/1000.0)});
//                    counter=0;
//                    t1=new Date();
//                }
                
            } catch (InvalidProtocolBufferException ex) {
                JDebug.StackTrace(Level.SEVERE, ex);
            } 
        }
    }      
    
    private void connectByZeroMQRouterProtobuf(ZMQ.Socket receiver) {
        //  Process tasks forever
        int counter=0;
        IDataListImmutable block;
        Date t1=new Date();
        boolean ackMode;
        while (!Thread.currentThread().isInterrupted ()) {            
//            JDebug.out.info("waiting for identity...");
            byte[] identity = receiver.recv(); // read identitiy 
            byte[] bytes = receiver.recv(); // read data (1)
            
            ackMode=(bytes.length==0); // ack mode!
                        
            try {
                if (ackMode) {
                    // in ack mode the data block will be read now, 
                    // in "no ack mode" the block has already been read before (1)
                    bytes = receiver.recv(); // read the data block
                }

                // process data block
                Date pt1=new Date();
                DpeValueProtos.DpeValueList object = DpeValueProtos.DpeValueList.parseFrom(bytes);
                block = new DataListProtobuf(object);
                collector.collectData(block);                
                counter+=object.getDpeValueListCount();
                Date pt2=new Date();

//                JDebug.out.log(Level.INFO, "block size={0} processing time={1} ack={2}",
//                        new Object[]{object.getDpeValueListCount(), ((pt2.getTime()-pt1.getTime())/1000.0), ackMode});

                //JDebug.out.log(Level.INFO, "protobuf:{0}", object.toString());
//                if ( counter > 1000 ) {
//                    Date t2=new Date();                    
//                    JDebug.out.log(Level.INFO, "v={0} v/s={1}", new Object[]{counter, counter/((t2.getTime()-t1.getTime())/1000.0)});
//                    counter=0;
//                    t1=new Date();
//                }

                // in ack mode we now have to send back the "ack"
                if (ackMode) {                      
                    receiver.sendMore(identity); // send identity
                    receiver.sendMore(""); // send empty string 
                    receiver.send(String.valueOf(object.getDpeValueListCount())); // size of block we have got
                }

            } catch (InvalidProtocolBufferException ex) {
                JDebug.StackTrace(Level.SEVERE, ex);
            }             
        }
    }          
}
