/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.base;

import at.rocworks.oa4j.var.VariablePtr;
import at.rocworks.oa4j.var.DpIdentifierVar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author vogler
 */
public class JDpGet extends JHotLinkWaitForAnswer {    
    List<DpIdentifierVar> dps;
    HashMap<String, VariablePtr> vcs;
    
    boolean sent = false;

    public JDpGet async(boolean async) {
        this.setAsync(async);
        return this;
    }
    
    public JDpGet async() {
        this.setAsync();
        return this;
    }        
    
    public JDpGet() {
        super();
        this.dps = new ArrayList<>();
        this.vcs = new HashMap<>();
    }
    
    public JDpGet add(String dp) {
        dps.add(new DpIdentifierVar(dp, "_original.._value"));
        return this;
    }
        
    public JDpGet add(DpIdentifierVar dpid) {
        dps.add(dpid);
        return this;
    }
        
    public JDpGet add(String dp, VariablePtr var) {
        DpIdentifierVar dpid = new DpIdentifierVar(dp, "_original.._value");
        dps.add(dpid);
        vcs.put(dpid.getName(), var);
        return this;
    }    
    
    public JDpGet add(DpIdentifierVar dpid, VariablePtr var) {
        dps.add(dpid);
        vcs.put(dpid.getName(), var);
        return this;
    }            
    
    @Override
    public JDpGet action(IAnswer answer) {
        super.action(answer);
        return this;
    }

    public JDpGet answer(IAnswer answer) {
        return action(answer);
    }
    
    public JDpGet send() {
        this.sent=true;
        JManager.getInstance().enqueueHotlink(this);
        return this;
    }       
    
    public JDpMsgAnswer await() {        
        if ( !sent ) this.send();
        this.waitForAnswer();
        sent=false;
        JDpMsgAnswer answer = this.getAnswer();
        if ( answer.getNumberOfItems() > 0 ) {
            answer.getItems().forEach((vc)->{
                VariablePtr var = vcs.get(vc.getDpIdentifier().getName());  
                if ( var != null ) var.set(vc.getVariable());           
            });
        }
        return answer;
    }        
        
    @Override
    protected int execute() {
        int ret;
        DpIdentifierVar[] arr = new DpIdentifierVar[dps.size()];
        for (int i=0; i<dps.size(); i++) arr[i]=dps.get(i);
        if ( (ret=JManager.getInstance().apiDpGet(this, arr)) == 0 ) 
            this.register();
        return ret;
    }

    @Override
    protected void answer(JDpMsgAnswer answer) {        
        this.deregister();
        super.answer(answer);
    }    
}
