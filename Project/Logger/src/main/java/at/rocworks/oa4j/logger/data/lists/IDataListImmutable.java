/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.data.lists;

import at.rocworks.oa4j.logger.data.base.DataItem;

/**
 *
 * @author vogler
 */
public interface IDataListImmutable {
    
    int getHighWaterMark();

    DataItem getItem(int idx);

    boolean isEmpty();

    int size();
    
}
