/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.keys;

import at.rocworks.oa4j.logger.data.Dp;

/**
 *
 * @author vogler
 */
public class KeyBuilderEl extends KeyBuilder {
    
    // store .. ElementName
   
    @Override
    public String getStoreOfDp(Dp dp) {
        return dp.getElement().equals("") ? "." : dp.getElement();        
    }
   
}
