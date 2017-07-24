/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.jni;

import at.rocworks.oa4j.var.DpIdentifierVar;
import at.rocworks.oa4j.var.Variable;


/**
 *
 * @author vogler
 */
public class DpVCItem extends Malloc {

    public DpVCItem() {
        super();
    }
    
    public DpVCItem(long cptr) {
        super.setPointer(cptr);
    }      
    
    @Override
    protected native long malloc();

    @Override
    protected native void free(long cptr);
    
    public native String toDebug(int level);        
    
    public native DpIdentifierVar getDpIdentifier();
    public native boolean setDpIdentifier(DpIdentifierVar dpid);
    
    public native Variable getValue();
    public native boolean setValue(Variable value);
}
