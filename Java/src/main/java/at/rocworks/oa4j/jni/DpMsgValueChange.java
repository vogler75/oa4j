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
public class DpMsgValueChange extends DpMsg {
    public DpMsgValueChange() {
        super();
    }
    
    public DpMsgValueChange(long cptr) {
        super.setPointer(cptr);
    }        
    
    public native boolean needsAnswer();
    public native void setWantAnswer(boolean answer);
    public native DpVCGroup getFirstGroup();
    public native DpVCGroup getNextGroup();       
    
    @Override
    protected native void free(long cptr);    
}
