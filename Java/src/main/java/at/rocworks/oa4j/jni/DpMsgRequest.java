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
public class DpMsgRequest extends Msg {    

    public DpMsgRequest() {
        super();
    }
    
    public DpMsgRequest(long cptr) {
        super.setPointer(cptr);
    }       
    
    public native RequestGroup getFirstGroup();
    public native RequestGroup getNextGroup();
    public native boolean getMultipleAnswersAllowed();
    
    @Override
    public native String toString();    
}
