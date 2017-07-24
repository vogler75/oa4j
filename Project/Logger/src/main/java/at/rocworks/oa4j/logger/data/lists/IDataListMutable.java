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
public interface IDataListMutable extends IDataListImmutable {

    boolean addItem(DataItem item);

    int addItems(IDataListImmutable e, int i);

    void clear();

    void delItem(int idx);

    boolean isFull();

    void setItem(int idx, DataItem item);
}
