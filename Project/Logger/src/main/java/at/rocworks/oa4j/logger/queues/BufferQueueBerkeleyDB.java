/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.queues;

import at.rocworks.oa4j.logger.data.lists.DataList;
import at.rocworks.oa4j.base.JDebug;

import com.sleepycat.je.*;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 *
 * @author vogler
 */
public class BufferQueueBerkeleyDB implements IBufferQueue {
    // TODO check disk space
    
    private final String directory;
    
    private final Environment dbEnv;

    private final EntityStore queueStore;
    
    PrimaryIndex<Long, BufferQueueBerkeleyDBEntity> queue;

    /**
     * This queue name.
     */
    private final String queueName;
        
    public BufferQueueBerkeleyDB(String queueName, String path) {
        // Create parent dirs for queue environment directory
        File file = new File(path);
        if (!file.isAbsolute()) path=System.getProperty("user.dir")+"/"+path;

        this.directory = path + "/" + queueName;

        JDebug.out.config("buffer to disk path: "+this.directory);

        (new File(path)).mkdir();
        (new File(this.directory)).mkdir();        

        // Setup database environment
        final EnvironmentConfig dbEnvConfig = new EnvironmentConfig();
        dbEnvConfig.setTransactional(false);
        dbEnvConfig.setAllowCreate(true);        
        dbEnv = new Environment(new File(directory), dbEnvConfig);        

        this.queueName = queueName;
                
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setAllowCreate(true);
        storeConfig.setTransactional(false);
        storeConfig.setDeferredWrite(true);
        queueStore = new EntityStore(dbEnv, queueName, storeConfig);

        queue = queueStore.getPrimaryIndex(Long.class, BufferQueueBerkeleyDBEntity.class);
    }
        
    @Override
    public DataList peek() {
        return read(false);
    }    
    
    @Override
    public DataList pop() {
        return read(true);
    }

    private DataList read(boolean delete) {
        int errors=0;
        while ( errors <= 10 ){
            BufferQueueBerkeleyDBEntity record;
            try (EntityCursor<BufferQueueBerkeleyDBEntity> blocks = queue.entities()) {
                record = blocks.first();
                if ( delete ) {
                    blocks.delete();
                    queueStore.sync();
                }
            } 
            if ( record == null )
                return null;            
            try {
                DataList block=record.getBlock();
                JDebug.out.log(Level.FINE, "{0} block #{1} hwm {2} read {3}", new Object[]{queueName, record.getId(), block.getHighWaterMark(), delete ? "and deleted":""});
                return block;                
            }
            catch (IOException | ClassNotFoundException ex ) {
                JDebug.StackTrace(Level.SEVERE, ex);
                errors++;  
            }                        
        }
        return null;
    }
    
    @Override
    public boolean push(DataList block) {
        try {
            BufferQueueBerkeleyDBEntity record = new BufferQueueBerkeleyDBEntity(block);
            queue.put(record);
            queueStore.sync();
            JDebug.out.log(Level.FINE, "{0} block #{1} hwm {2} saved", new Object[]{queueName, record.getId(), block.getHighWaterMark()});
            return true;
        } catch (IOException ex) {
            JDebug.StackTrace(Level.SEVERE, ex);
            return false; 
        }
    }    

    @Override
    public long size() {
        return queue.count();
    } 

    @Override
    public void start() {
        JDebug.out.log(Level.INFO, "start queue size={0}", size());
    }        
    
}
