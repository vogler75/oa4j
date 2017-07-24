package at.rocworks.oa4j.logger.dbs;

import at.rocworks.oa4j.base.JDebug;

import at.rocworks.oa4j.logger.data.base.AlertItem;
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

import com.espertech.esper.client.*;

import java.util.Date;
import java.util.Set;
import java.util.logging.Level;

/**
 *
 * @author vogler
 */
public class NoSQLEsper extends NoSQLServer {

    EPServiceProvider cep;
    EPRuntime runtime;
    
    public static NoSQLServer createServer(NoSQLSettings srvcfg, String srvprefix) {
        JDebug.out.log(Level.CONFIG, "{0}.type: {1} \n{2}", 
                new Object[]{srvprefix, NoSQLEsper.class.getName(), srvcfg});
        return new NoSQLEsper(srvcfg);
    }    
            
    public NoSQLEsper(NoSQLSettings settings) {
        super(settings);
        
        Configuration cepConfig = new Configuration();
        cepConfig.addEventType("Event",NoSQLEsperEvent.class.getName());
 
        cep=EPServiceProviderManager.getProvider("WinCCOA", cepConfig);
        runtime = cep.getEPRuntime();        
    }

    @Override
    public boolean dpGetPeriod(Date t1, Date t2, Dp dp, Set<String> configs, DpGetPeriodResult result) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int storeData(DataList list) {
        boolean ev=false;
        boolean al=false;
        
        for ( int i=0; i<=list.getHighWaterMark(); i++ ) {
            DataItem item = list.getItem(i);
            if ( item != null ) {
                if ( item instanceof EventItem )
                    ev=true;
                else if ( item instanceof AlertItem)
                    al=true;
            }
        }
        
        int ret;
        if ( (!ev || (ret=storeDataEvents(list))==INoSQLInterface.OK) &&
             (!al || (ret=storeDataAlerts(list))==INoSQLInterface.OK) )
            return INoSQLInterface.OK;
        else
            return ret;            
    }
    
    public int storeDataEvents(DataList events) {
        Date t1 = new Date();        
        
        DataItem item;             
        for (int i = 0; i <= events.getHighWaterMark()&& (item = events.getItem(i)) != null; i++) {           
            if ( !(item instanceof EventItem) ) continue;
            //JDebug.out.info(((EventItem)item).toJSONObject().toJSONString());
            runtime.sendEvent(new NoSQLEsperEvent((EventItem)item));
        }
                    
        Date t2 = new Date();
        addServerStats(events.getHighWaterMark(), t2.getTime()-t1.getTime());               
        
        return INoSQLInterface.OK;
    }
    
    private int storeDataAlerts(DataList alerts) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.        
    }    
}
