/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.queues;

import at.rocworks.oa4j.logger.data.lists.DataList;

/**
 *
 * @author vogler
 */
public interface IBufferQueue {

    void start();    
    
    DataList peek();

    DataList pop();

    boolean push(DataList block);

    long size();

    @Override
    String toString();
    
}
