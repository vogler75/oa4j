package at.rocworks.oa4j.logger.dbs;

import at.rocworks.oa4j.base.JDebug;

import at.rocworks.oa4j.var.Bit32Var;
import at.rocworks.oa4j.var.Bit64Var;
import at.rocworks.oa4j.var.DynVar;
import at.rocworks.oa4j.var.TimeVar;
import at.rocworks.oa4j.var.Variable;
import at.rocworks.oa4j.var.VariableType;

import at.rocworks.oa4j.logger.base.INoSQLInterface;
import at.rocworks.oa4j.logger.base.NoSQLServer;
import at.rocworks.oa4j.logger.base.NoSQLSettings;
import at.rocworks.oa4j.logger.data.base.DataItem;
import at.rocworks.oa4j.logger.data.lists.DataList;
import at.rocworks.oa4j.logger.data.Dp;
import at.rocworks.oa4j.logger.data.DpAttr;
import at.rocworks.oa4j.logger.data.base.EventItem;
import at.rocworks.oa4j.logger.keys.KeyBuilder;
import at.rocworks.oa4j.logger.query.DpGetPeriodResult;

import java.util.Date;

/**
 *
 * @author vogler
 */
public class NoSQLEsperEvent {
    private final Dp dp;
    private final Date time;
    private final Double value;
    private final Long status;
    private final Integer manager;
    private final Integer user;
    
    public NoSQLEsperEvent(Date time, Dp dp, Double value, Long status, Integer manager, Integer user) {
        this.dp=dp;        
        this.time=time;
        this.value=value;
        this.status=status;
        this.manager=manager;
        this.user=user;
    }
    
    public NoSQLEsperEvent(EventItem event) {
        this(event.getDate(),
                event.getDp(), 
                event.getValue().getDouble(),
                event.getStatus(),
                event.getManager(),
                event.getUser());     
    }
    
    @Override
    public String toString() {
        return getTag()+": "+getValue();
    }
    
    public String getDp() {
        return dp.getDp();
    }

    /**
     * @return the time
     */
    public Date getTime() {
        return time;
    }
    
    public Long getTimestamp() {
        return getTime().getTime();
    }

    /**
     * @return the tag
     */
    public String getTag() {
        return dp.getFQN();
    }
    
    /**
     * @return the value
     */
    public Double getValue() {
        return value;
    }    

    /**
     * @return the manager
     */
    public Integer getManager() {
        return manager;
    }

    /**
     * @return the user
     */
    public Integer getUser() {
        return user;
    }

    /**
     * @return the status
     */
    public Long getStatus() {
        return status;
    }
}
