/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.jni;

import at.rocworks.oa4j.var.DpIdentifierVar;
import at.rocworks.oa4j.var.TimeVar;
import at.rocworks.oa4j.var.Variable;

/**
 *
 * @author vogler
 */
public class DpVCGroup extends DpIdValueList {
    public DpVCGroup() {
        super();
    }
    
    public DpVCGroup(long cptr) {
        super.setPointer(cptr);
    }            
    
    @Override
    public native String toDebug(int level);    
    
    public native TimeVar getOriginTime();
    public native void setOriginTime(TimeVar time);
    public native boolean insertValueChange(DpIdentifierVar dpid, Variable var);
    
    @Override
    protected native long malloc();

    @Override
    protected native void free(long cptr);      
}
