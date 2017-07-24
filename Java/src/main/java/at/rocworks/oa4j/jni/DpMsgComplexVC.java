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
public class DpMsgComplexVC extends DpMsgValueChange {
    public DpMsgComplexVC() {
        super();
    }
    
    public DpMsgComplexVC(long cptr) {
        super.setPointer(cptr);
    }
    
    @Override
    public native DpVCGroup getFirstGroup();
    
    @Override
    public native DpVCGroup getNextGroup();
    
    public native DpVCGroup getLastGroup();
    
    public native int getNrOfGroups();

    @Override
    public native String toDebug(int level);
}
