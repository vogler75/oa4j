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
public class DpMsgAnswer extends Msg {
    public DpMsgAnswer() {
        super();
    }
    
    public DpMsgAnswer(long cptr) {
        super.setPointer(cptr);
    }       
    
    public DpMsgAnswer(Msg message) {
        this.cptr=newFromMsg(message.getPointer());        
    }
    
    public DpMsgAnswer(DpMsgAnswer answer) {
        this.cptr=newFromMsgAnswer(answer.getPointer());        
    }    
    
    public native boolean insertGroup(AnswerGroup group);
    public native void setOutstandingProgress(int percents);
    
    protected native long newFromMsg(long dpMsgPtr);        
    protected native long newFromMsgAnswer(long dpMsgPtr);                
}
