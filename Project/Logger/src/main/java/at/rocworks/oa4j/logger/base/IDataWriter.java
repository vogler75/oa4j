/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.base;

import at.rocworks.oa4j.logger.data.lists.DataList;

/**
 *
 * @author vogler
 */
public interface IDataWriter {
    public String getName();    
    public boolean isActive();    
    public int getFlushinterval();
    public int getBlocksize();
    public long getQueuesize();
    public int storeData(DataList list);        
}
