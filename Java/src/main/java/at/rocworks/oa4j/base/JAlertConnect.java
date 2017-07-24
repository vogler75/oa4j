/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.base;

import java.util.ArrayList;

/**
 *
 * @author vogler
 */
public class JAlertConnect extends JHotLinkWaitForAnswerGroup {    

    @Override
    public JAlertConnect action(IHotLink hotlink) {
        return (JAlertConnect)super.action(hotlink);
    }
    
    @Override
    public JAlertConnect action(IAnswer answer) {
        return (JAlertConnect)super.action(answer);
    }       
    
    @Override
    public JAlertConnect addGroup() {
        return (JAlertConnect)super.addGroup();
    }    
          
    @Override
    public JAlertConnect addGroup(String[] dps) {
        return (JAlertConnect)super.addGroup(dps);
    }
    
    @Override
    public JAlertConnect addGroup(ArrayList<String> dps) {
        return (JAlertConnect)super.addGroup(dps);
    }
    
    @Override
    public JAlertConnect add(String dp) {
        return (JAlertConnect)super.add(dp);
    }
    
    @Override
    public JAlertConnect connect() {
        return (JAlertConnect)super.connect();
    }         
    
    @Override
    public int execute() {
        int ret = 0;
        
        for (ConnectGroup c : addList) {
            this.cptr = 0;
            if ((ret = JManager.getInstance().apiAlertConnect(this, c.dps.toArray(new String[c.dps.size()]))) == 0) {
                c.cptr = this.cptr;
                conList.add(c);
            }
        }
        addList.clear();
        for (ConnectGroup c : delList) {
            this.cptr = c.cptr;
            if ((ret = JManager.getInstance().apiAlertDisconnect(this, c.dps.toArray(new String[c.dps.size()]))) == 0) {
                conList.remove(c);
            }
        }
        delList.clear();
        if (conList.size() > 0)
            this.register();
        else
            this.deregister();

        return ret;
    }

    @Override
    public void hotlink(JDpHLGroup group) {
        if ( this.cbHotlink != null) 
            this.cbHotlink.hotlink(group);
    }

    @Override
    public void answer(JDpMsgAnswer answer) {
        if (this.cbAnswer != null)
            this.cbAnswer.answer(answer);
    }
}