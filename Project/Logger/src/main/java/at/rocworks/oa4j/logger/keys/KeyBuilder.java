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
public abstract class KeyBuilder {
    
    public abstract String getStoreOfDp(Dp dp);
    
    public Dp getDpOfTag(String tag) {
        return new Dp(tag.replace('/', ':'));
    } 

    public String getTagOfDp(Dp dp) {
        return dp.getFQN().replace(':','/');
    }

    public String getTagOfDp(Dp dp, String config) {
        return dp.getSystem()+"/"+dp.getDp()+"."+dp.getElement() + "/" + config;
    }      
    
    public static KeyBuilder getKeyBuilder(String fmt) {
        KeyBuilder key;
        switch (fmt) {
            case "dpel":
                key = new KeyBuilderDpEl();
                break;
            case "dp":
                key = new KeyBuilderDp();
                break;
            case "el":
                key = new KeyBuilderEl();
                break;
            default:
                key = new KeyBuilderDpEl();
                break;
        }
        return key;
    }    
}
