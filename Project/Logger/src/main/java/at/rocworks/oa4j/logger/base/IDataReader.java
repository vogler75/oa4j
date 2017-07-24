/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.base;

import at.rocworks.oa4j.logger.query.DpGetPeriodParameter;
import at.rocworks.oa4j.logger.query.DpGetPeriodResult;

/**
 *
 * @author vogler
 */
public interface IDataReader {
    public boolean dpGetPeriod(DpGetPeriodParameter param, DpGetPeriodResult result);
}
