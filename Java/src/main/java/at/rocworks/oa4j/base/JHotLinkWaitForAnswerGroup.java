/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.base;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author vogler
 */
public abstract class JHotLinkWaitForAnswerGroup extends JHotLinkWaitForAnswer {

    protected class ConnectGroup {
        ArrayList<String> dps;
        long cptr;

        public ConnectGroup() {
            this.dps = new ArrayList<>();
            this.cptr = 0;
        }
        public ConnectGroup(ArrayList<String> dps) {
            this.dps = dps;
            this.cptr = 0;
        }        
        public ConnectGroup(ArrayList<String> dps, long cptr) {
            this.dps = dps;
            this.cptr = cptr;
        }
    }

    protected final ArrayList<ConnectGroup> addList = new ArrayList<>();
    protected final ArrayList<ConnectGroup> delList = new ArrayList<>();
    protected final ArrayList<ConnectGroup> conList = new ArrayList<>();
    
    protected synchronized JHotLinkWaitForAnswerGroup addGroup() {
        addList.add(new ConnectGroup());
        return this;
    }    
          
    protected synchronized JHotLinkWaitForAnswerGroup addGroup(String[] dps) {
        addList.add(new ConnectGroup(new ArrayList<>(Arrays.asList(dps))));
        return this;
    }
    
    protected synchronized JHotLinkWaitForAnswerGroup addGroup(ArrayList<String> dps) {
        addList.add(new ConnectGroup(dps));
        return this;
    }
    
    protected synchronized JHotLinkWaitForAnswerGroup add(String dp) {
        if (addList.isEmpty()) addGroup();
        addList.get(addList.size()-1).dps.add(dp);
        return this;
    }
    
    protected synchronized JHotLinkWaitForAnswerGroup connect() {
        if (!addList.isEmpty()) {
            JManager.getInstance().enqueueHotlink(this);
        }        
        return this;
    }

    public synchronized void disconnect() {
        if (!conList.isEmpty()) {
            delList.addAll(conList);
            JManager.getInstance().enqueueHotlink(this);
        }
    }    
}
