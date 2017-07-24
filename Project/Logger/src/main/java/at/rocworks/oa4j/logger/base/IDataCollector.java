/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.base;

import at.rocworks.oa4j.logger.data.base.DataItem;
import at.rocworks.oa4j.logger.data.lists.IDataListImmutable;

/**
 *
 * @author vogler
 */
public interface IDataCollector {
    int collectData(DataItem item);
    int collectData(IDataListImmutable list);
}
