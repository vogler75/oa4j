/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.queues;

import at.rocworks.oa4j.logger.data.lists.DataList;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import java.io.IOException;

/**
 *
 * @author vogler
 */
@Entity
public class BufferQueueBerkeleyDBEntity {
    @PrimaryKey(sequence="ID")
    private long id;        
    private byte[] bytes;
    public BufferQueueBerkeleyDBEntity() {            
    }
    public BufferQueueBerkeleyDBEntity(DataList block) throws IOException {
        this.bytes=block.toBytes();
    }
    public long getId() {
        return id;
    }
    public DataList getBlock() throws IOException, ClassNotFoundException {
        return DataList.fromBytes(bytes);
    }
}
