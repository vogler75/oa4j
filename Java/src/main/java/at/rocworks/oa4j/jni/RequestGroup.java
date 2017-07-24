/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.jni;

import at.rocworks.oa4j.var.TimeVar;

/**
 *
 * @author vogler
 */
public class RequestGroup extends Malloc {   
    
    public RequestGroup(long cptr) {
        super.setPointer(cptr);
    }    
    
    public native RequestItem getFirstItem();
    public native RequestItem getNextItem();    
    public native TimeVar getTime1();
    public native TimeVar getTime2();
    public native String toDebug(int level);    
    
    @Override
    protected native long malloc();

    @Override
    protected native void free(long cptr);
    
}
