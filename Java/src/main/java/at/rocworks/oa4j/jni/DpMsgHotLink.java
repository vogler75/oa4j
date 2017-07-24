/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.jni;

/**
 *
 * @author vogler
 */
public class DpMsgHotLink extends Malloc {        
    
    public DpMsgHotLink(long cptr) {
        super.setPointer(cptr);
    }    
    
    @Override
    protected native long malloc();
    
    @Override
    protected native void free(long cptr);
}
