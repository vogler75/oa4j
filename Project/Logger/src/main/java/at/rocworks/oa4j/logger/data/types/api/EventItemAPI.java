/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.data.types.api;

import java.io.Serializable;

import at.rocworks.oa4j.logger.data.Dp;
import at.rocworks.oa4j.logger.data.base.ValueItem;
import at.rocworks.oa4j.var.Variable;
import at.rocworks.oa4j.logger.data.base.EventItem;

/**
 *
 * @author vogler
 */
public class EventItemAPI extends EventItem implements Serializable {
    private final ValueItemAPI value;
    private final long stime;
    private final long status;
    private final int manager;
    private final int user;        
    private final boolean hasAttributes;
       
    public EventItemAPI(String dpName, Variable value, double stime) {
        super(new Dp(dpName));

        this.stime=(new Double(stime)).longValue();
        this.value=new ValueItemAPI(value);
        
        this.hasAttributes=false;
        this.status=0L;
        this.manager=0;
        this.user=0;       
    }      
    
    public EventItemAPI(String dpName, Variable value, double stime, long status, int manager, int user) {
        super(new Dp(dpName));
        
        this.stime=(new Double(stime)).longValue();
        this.value=new ValueItemAPI(value);
        
        this.hasAttributes=true;
        this.status=status;
        this.manager=manager;
        this.user=user;    
    }       
    
    @Override
    public boolean hasAttributes() {
        return this.hasAttributes;
    }

    @Override
    public long getTimeMS() {
        return stime;
    }

    @Override
    public long getTimeNS() {
        return getTimeMS()*1000000L+getAddedNanos();
    }                

    @Override
    public ValueItem getValue() {
        return value;
    }

    @Override
    public long getStatus() {
        return status;
    }

    @Override
    public int getManager() {
        return manager;
    }

    @Override
    public int getUser() {
        return user;
    }
    
}
