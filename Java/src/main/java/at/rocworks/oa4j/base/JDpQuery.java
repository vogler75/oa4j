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
public class JDpQuery extends JHotLinkWaitForAnswer {
    String query;
    boolean sent = false;    
         
    public JDpQuery(String query) {
        super();
        this.query = query;
    }
    
    public JDpQuery async() {
        this.setAsync();
        return this;
    }          
    
    public JDpQuery send() {
        this.sent=true;
        JManager.getInstance().enqueueHotlink(this);        
        return this;
    }
        
    public JDpMsgAnswer await() {
        if ( !sent ) this.send();
        this.waitForAnswer();
        this.sent=false;
        return this.getAnswer();
    }   
    
    @Override
    public JDpQuery action(IAnswer answer) {
        super.action(answer);
        return this;
    }

    @Override
    protected int execute() {
        int ret;
        if ( (ret=JManager.getInstance().apiDpQuery(this, query)) == 0 ) 
            this.register();
        return ret;
    }

    @Override
    protected void answer(JDpMsgAnswer answer) {
        this.deregister();
        super.answer(answer);
    }
}
