/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.query;

import java.io.Serializable;
import java.util.Date;
import at.rocworks.oa4j.var.DynVar;
import at.rocworks.oa4j.var.Variable;

/**
 *
 * @author vogler
 */
public class DpGetPeriodResultJavaTuple implements Serializable {
    
    public final DynVar mt = new DynVar(); // dp/times
    public final DynVar mv = new DynVar(); // dp/values        
    
    public void addVariable(Date ts, Variable value) {
        synchronized (this) {       
            mt.add(Variable.newTimeVar(ts.getTime()));
            mv.add(value);            
        }
    }        
}
