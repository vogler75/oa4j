/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.logger;

import at.rocworks.oa4j.var.DpIdentifierVar;

/**
 *
 * @author vogler
 */
public interface IDatapointFilter {    
    public boolean isArchived(DpIdentifierVar dpid);
}
