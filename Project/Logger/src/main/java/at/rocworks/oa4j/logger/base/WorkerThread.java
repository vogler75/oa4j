/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.base;

import java.util.logging.Level;

import at.rocworks.oa4j.logger.data.Metrics;
import at.rocworks.oa4j.logger.data.base.DataItem;
import at.rocworks.oa4j.logger.queues.IBufferQueue;
import at.rocworks.oa4j.logger.data.lists.DataList;
import at.rocworks.oa4j.logger.data.lists.IDataListImmutable;

import at.rocworks.oa4j.base.JDebug;
import at.rocworks.oa4j.base.JSemaphore;

import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author vogler
 */
public class WorkerThread implements IDataCollector {
    protected int threadNr;
    protected IDataWriter writer;
    protected IBufferQueue queue;    

    private final JSemaphore writeTrigger;
    private DataList currentBlock;
    private final Object currentBlockLock = new Object();
    private volatile Date lastWriteTime;
        
    public WorkerThread(int threadNr, IDataWriter server, IBufferQueue queue) {
        this.threadNr = threadNr;
        this.writer = server;
        
        this.writeTrigger = new JSemaphore(false);
        this.currentBlock = new DataList(server.getBlocksize());      
        
        this.queue = queue;
        this.queue.start();        
    }    
    
    public int getThreadNr() {
        return threadNr;
    }
    
    public void start() {
        lastWriteTime=new Date();
        (new Thread(()->writeThread())).start();
        (new Thread(()->flushThread())).start();
    }

    public String getName() {
        return writer.getName() + ".T" + threadNr;
    }
    
    public String getSimpleName() {
        return "T"+threadNr;
    }
    
    public long getQueuesize() {
        return queue.size();
    }

    public Metrics getStats() {
        Metrics stats = new Metrics(getSimpleName());        
        stats.put("qlen", (double)queue.size());
        return stats;
    }
    
    public void writeThread() { // Block Writer
        int ret;
        DataList block;
        while (true) {
            try {
                writeTrigger.request();
                while ((block = queue.peek()) != null) {
                    JDebug.out.log(Level.FINEST, "{0} got block {1}", new Object[]{this.getName(), block});
                    if (block.isEmpty()) {
                        queue.pop();
                    } else {
                        try {
                            lastWriteTime = new Date();                            
                            ret = storeData(block);
                            if (ret == INoSQLInterface.ERR_REPEATABLE) {
                                JDebug.out.log(Level.WARNING, "{0} blocked hwm={1} ret={2}", new Object[]{this.getName(), block.getHighWaterMark(), ret});
                                break;
                            } else {                                
                                queue.pop();
                                JDebug.out.log(Level.FINE, "{0} flushed hwm={1} ret={2}", new Object[]{this.getName(), block.getHighWaterMark(), ret});
                            }
                        } catch ( Exception ex ) {
                            queue.pop();                            
                            JDebug.StackTrace(Level.SEVERE, ex);
                        }
                    }
                }
            } catch ( Exception ex ) {
                JDebug.StackTrace(Level.SEVERE, ex);
            }                
        }
    }

    private int storeData(DataList events) {
        if ( this.writer.isActive() ) {
//            for (int i=0; i<=events.getHighWaterMark(); i++)
//                if (events.getItem(i).getDp().getFQN().equals("System1:LoggerQuery."))
//                    JDebug.out.info(this.getName()+": "+events.getItem(i).toJSONObject().toJSONString());
            return writer.storeData(events);
        }
        else
            return INoSQLInterface.OK;
    }

    private final ArrayList<DataList> collectedBlocks = new ArrayList<>();
    
    @Override    
    public synchronized int collectData(IDataListImmutable list) {
        int k = 0;
        collectedBlocks.clear();        
        synchronized (currentBlockLock) {
            //JDebug.out.log(Level.FINER, "{0} add {1} events to block of hwm {2}", new Object[]{this.getThreadName(), events.size(), currentBlock.getHighWaterMark()});
            while ((k = currentBlock.addItems(list, k)) > 0 /* still events left */
                    || (k == -1 /* all events added and block full */)
                    || (k == 0 /* all events added and block not full */ && writer.getFlushinterval() == -1 /* flush immediate */)) {
                //JDebug.out.log(Level.FINER, "{0} add block to queue (k={1})", new Object[]{this.getThreadName(), k});
                collectedBlocks.add(this.currentBlock);
                currentBlock = new DataList(writer.getBlocksize());                
                if (k <= 0) {
                    break;
                }
            }
        }
        if (!collectedBlocks.isEmpty()) {
            synchronized (writeTrigger) {
                collectedBlocks.forEach((block)->queue.push(block));                
            }                        
            writeTrigger.dispatch();
        }
        //JDebug.out.log(Level.FINER, "{0} add {1} events to block of hwm {2} done", new Object[]{this.getThreadName(), events.size(), currentBlock.getHighWaterMark()});
        return INoSQLInterface.OK;
    }
    
    @Override
    public synchronized int collectData(DataItem item) {
//        if (item.getDp().getFQN().equals("System1:LoggerQuery."))
//            JDebug.out.info(this.getName()+": "+item.toJSONObject().toJSONString());
        DataList collectedBlock=null;
        synchronized ( currentBlockLock ) {
            currentBlock.addItem(item);
            if ( currentBlock.isFull() ) {
                collectedBlock=currentBlock;
                currentBlock = new DataList(writer.getBlocksize());
            }
        }
        if ( collectedBlock != null ) {
            synchronized (writeTrigger) {
                queue.push(collectedBlock);
            }
            writeTrigger.dispatch();
        }        
        return INoSQLInterface.OK;
    }
    
    public void flushThread() {
        writeTrigger.dispatch(); // on startup check if there is something in the queue (disk queue)
        if ( writer.getFlushinterval()<=0 ) {
            JDebug.out.log(Level.SEVERE, "invalid flush interval {0}", writer.getFlushinterval());
            return;
        }
        Date lastTriggered = new Date(0);
        while ( true ) {
            if ( lastWriteTime.getTime() > lastTriggered.getTime() ) 
                lastTriggered=lastWriteTime;
                
            // sleep timeout            
            long delay = writer.getFlushinterval()-((new Date().getTime())-lastTriggered.getTime());            
            if ( delay > 0 ) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ex) {
                    JDebug.StackTrace(Level.SEVERE, ex);
                }
            }                        

            // check timeout
            if ( writer.getFlushinterval()-((new Date().getTime())-lastWriteTime.getTime()) <= 0 ) {
                //JDebug.out.log(Level.INFO, "flush interval {0} {1} {2} {3}", new Object[]{writer.getFlushinterval(), lastWriteTime, lastTriggered, delay});
                lastTriggered=new Date(); 
                ArrayList<DataList> blocks = new ArrayList<>();
                synchronized (currentBlockLock) {
                    if (!this.currentBlock.isEmpty()) {
                        JDebug.out.log(Level.FINE, "{0} timeout queue {1} hwm={2}", new Object[]{this.getName(), queue.toString(), this.currentBlock.getHighWaterMark()});
                        blocks.add(this.currentBlock);
                        currentBlock = new DataList(writer.getBlocksize());
                    }
//                    if (!this.currentBlockSingle.isEmpty()) {
//                        JDebug.out.log(Level.FINE, "{0} timeout queue {1} hwm={2}", new Object[]{this.getName(), queue.toString(), this.currentBlockSingle.getHighWaterMark()});
//                        blocks.add(currentBlockSingle);
//                        currentBlockSingle = new DataList(writer.getBlocksize());
//                    }
                }
                if ( !blocks.isEmpty() ) {
                    synchronized (writeTrigger) {
                        blocks.forEach((block)->queue.push(block));
                    }
                    writeTrigger.dispatch();
                }
            }
        }
    }    
    
    
    public int collectData_old(IDataListImmutable list) {
        int k = 0;
        synchronized (currentBlockLock) {
            //JDebug.out.log(Level.FINER, "{0} add {1} events to block of hwm {2}", new Object[]{this.getThreadName(), events.size(), currentBlock.getHighWaterMark()});
            while ((k = currentBlock.addItems(list, k)) > 0 /* still events left */
                    || (k == -1 /* all events added and block full */)
                    || (k == 0 /* all events added and block not full */ && writer.getFlushinterval() == -1 /* flush immediate */)) {
                //JDebug.out.log(Level.FINER, "{0} add block to queue (k={1})", new Object[]{this.getThreadName(), k});
                synchronized (writeTrigger) {
                    queue.push(this.currentBlock);
                    currentBlock = new DataList(writer.getBlocksize());
                }
                writeTrigger.dispatch();
                if (k <= 0) {
                    break;
                }
            }
        }
        //JDebug.out.log(Level.FINER, "{0} add {1} events to block of hwm {2} done", new Object[]{this.getThreadName(), events.size(), currentBlock.getHighWaterMark()});
        return INoSQLInterface.OK;
    }    
}    

