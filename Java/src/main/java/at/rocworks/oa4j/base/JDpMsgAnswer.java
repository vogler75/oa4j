/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.base;

/**
 *
 * @author vogler
 */
public class JDpMsgAnswer extends JDpVCGroup {
    private int retCode = 0;
    
    public int getRetCode() {
        return this.retCode;
    }
    
    protected void setRetCode(int errorCode) {
        this.retCode=errorCode;
    }

}
