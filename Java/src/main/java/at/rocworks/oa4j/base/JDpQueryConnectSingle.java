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
public class JDpQueryConnectSingle extends JDpQueryConnect {

    public JDpQueryConnectSingle(String query) {
        super(query);
    }
    
    @Override
    protected int apiDpQueryConnect(JHotLinkWaitForAnswer hdl, Boolean values, String query) {
        return JManager.getInstance().apiDpQueryConnectSingle(hdl, values, query);
    }
    
}
