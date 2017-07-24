/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.data.types.protobuf;

import at.rocworks.oa4j.logger.data.base.DataItem;
import at.rocworks.oa4j.logger.data.lists.IDataListImmutable;

/**
 *
 * @author vogler
 */
public class DataListProtobuf implements IDataListImmutable { 

    DpeValueProtos.DpeValueList list;
    
    public DataListProtobuf(DpeValueProtos.DpeValueList list) {
        this.list=list;
    }

    @Override
    public int getHighWaterMark() {
        return list.getDpeValueListCount()-1;
    }

    @Override
    public DataItem getItem(int idx) {
        return new EventItemProtobuf(list.getDpeValueList(idx));
    }

    @Override
    public boolean isEmpty() {
        return list.getDpeValueListCount()==0;
    }

    @Override
    public int size() {
        return list.getDpeValueListCount();
    }
}
