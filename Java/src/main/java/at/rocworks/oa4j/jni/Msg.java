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
public class Msg extends Malloc {         
    public Msg() {
        super();
    }
    
    public Msg(long cptr) {
        super.setPointer(cptr);
    }        
    
    public MsgType isA() {
        return MsgType.values()[isA(cptr)];
    }            
        
    protected native int isA(long cptr);

    public native long getMsgId();

    public void forwardMsg(ManagerType manType, int manNum) {
        forwardMsg(manType.value, manNum);
    }

    public native void forwardMsg(int manType, int manNum);

    public native void forwardMsgToData();

    public native int getSourceManTypeNr();

    public ManagerType getSourceManType() {
        return ManagerType.values()[getSourceManTypeNr()];
    }

    public native int getSourceManNum();
    
    @Override
    public native String toString();
    
    public native String toDebug(int level);

    @Override
    protected long malloc() {
        throw new UnsupportedOperationException("cannot instantiate abstract class"); 
    }

    @Override
    protected native void free(long cptr);
}
