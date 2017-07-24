/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.queues;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import at.rocworks.oa4j.logger.data.lists.DataList;
import at.rocworks.oa4j.base.JDebug;

/**
 *
 * @author vogler
 */
//-----------------------------------------------------------------------------------------
public class BufferQueue implements IBufferQueue {
    private final String queueName;
    
    private final int maxMemBlocks;

    private final Queue<DataList> memQueue = new LinkedList<>();
    protected final IBufferQueue diskQueue;

    private boolean bufferToDiskIndicator = false;
    private boolean memoryFullIndicator = false;
    private boolean bufferToDiskOnly = false;
    
    public BufferQueue(String queueName, int maxmemblocks) {
        this(queueName, maxmemblocks, null);
    }    
    
    public BufferQueue(String name, int maxmemblocks, IBufferQueue secondary) {
        this.queueName = name;
        this.maxMemBlocks = maxmemblocks;
        this.diskQueue = secondary;
        if ( this.maxMemBlocks == 0 ) {
            this.bufferToDiskOnly = true;
        }
    }
    
    @Override
    public void start() {
        if (diskQueue != null) {
            diskQueue.start();
            bufferToDiskIndicator=( diskQueue.size()>0 );
        }        
    }

    @Override
    public String toString() {
        return this.queueName + " mq: " + String.format("%3d", memQueue.size()) + (diskQueue == null ? "" : " dq: " + String.format("%3d", diskQueue.size()));
    }

    @Override
    public long size() {
        synchronized ( this ) {
            return memQueue.size()+diskSize();
        }
    }

    @Override
    public boolean push(DataList block) {
        JDebug.out.log(Level.FINER, "{0} state before push mfi={1} btdi={2}", new Object[]{this.queueName, memoryFullIndicator, bufferToDiskIndicator});

        boolean ok;
        synchronized (this) 
        {
            if (memoryFullIndicator || bufferToDiskIndicator || bufferToDiskOnly ) {
                if (diskQueue != null) {
                    if ((ok = diskQueue.push(block))) {
                        bufferToDiskIndicator = true;
                    }
                    JDebug.out.log(Level.FINER, "{0} added to disk {1} mfi={2} btdi={3}", new Object[]{queueName, ok, memoryFullIndicator, bufferToDiskIndicator});
                    return ok;
                } else {
                    pop(); // drop block from memory
                    JDebug.out.log(Level.WARNING, "{0} dropped block mfi={1} btdi={2}", new Object[]{queueName, memoryFullIndicator, bufferToDiskIndicator});
                }
            }
            if (ok = memQueue.add(block)) {
                memoryFullIndicator = (memQueue.size() >= maxMemBlocks);
                JDebug.out.log(Level.FINE, "{0} added to memory size={1} mfi={2} btdi={3}", new Object[]{queueName, memQueue.size(), memoryFullIndicator, bufferToDiskIndicator});
            }
        }
        return ok;
    }

    @Override
    public DataList peek() {
        DataList block;
        synchronized (this) 
        {
            if (bufferToDiskOnly || ((block = memQueue.peek()) == null && bufferToDiskIndicator)) {
                block = diskQueue.peek();
            }            
        }
        return block;
    }

    @Override
    public DataList pop() {
        JDebug.out.log(Level.FINER, "{0} state before pop mfi={1} btdi={2}", new Object[]{queueName, memoryFullIndicator, bufferToDiskIndicator});

        DataList block;
        synchronized (this) 
        {
            if (bufferToDiskOnly || ((block = memQueue.poll()) == null && bufferToDiskIndicator)) {
                block = diskQueue.pop();
                bufferToDiskIndicator = (diskQueue.size() > 0);                
            }
            memoryFullIndicator = (size() == maxMemBlocks);
                
//            if ((block = memQueue.poll()) != null) {
//                memoryFullIndicator = (size() == maxMemBlocks);
//            } else if (bufferToDiskIndicator || bufferToDiskOnly) {
//                block = diskQueue.pop();
//                bufferToDiskIndicator = (diskQueue.size() > 0);
//            }
        }

        JDebug.out.log(Level.FINE, "{0} memory size={1} mfi={2} btdi={3}", new Object[]{queueName, memQueue.size(), memoryFullIndicator, bufferToDiskIndicator});
        return block;
    }
    

    private long diskSize() {
        return (diskQueue == null ? 0 : diskQueue.size());
    }    
}