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
public class JDpVCGroup extends JDpIdValueList {
    private int errCode = 0;
    private String errText = null;

    public void setError(int errorCode, String errorText) {
        this.errCode=errorCode;
        this.errText=errorText;
    }

    public int getErrorCode() {
        return this.errCode;
    }
    public String getErrorText() {
        return this.errText;
    }
}
