/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.base;

import java.util.Date;

import at.rocworks.oa4j.var.BitVar;
import at.rocworks.oa4j.var.DpIdentifierVar;
import at.rocworks.oa4j.var.IntegerVar;
import at.rocworks.oa4j.var.TimeVar;
import at.rocworks.oa4j.var.FloatVar;
import at.rocworks.oa4j.var.TextVar;
import at.rocworks.oa4j.var.Variable;
import at.rocworks.oa4j.var.VariableType;

/**
 *
 * @author vogler
 */
public class JDpVCItem {
    private final DpIdentifierVar dpid;
    private final Variable var;
            
    public JDpVCItem(DpIdentifierVar dpid, Variable var) {
        this.dpid = dpid;
        this.var = var;
    }

    public DpIdentifierVar getDpIdentifier() {
        return dpid;
    }

    public String getDpName() {
        return dpid.getName();
    }

    public Variable getVariable() {
        return var;
    }

    public Object getValueObject() {
        return var.getValueObject();
    }
    
    public String getValueClassName() {
        return var.getValueClassName();
    }
    
    public VariableType isA() {
        return var.isA();
    }
    
    public int getVariableTypeAsNr() {
        return var.getVariableTypeAsNr();
    }

    @Override
    public String toString() {
        return this.dpid + ": " + this.var + " [" + this.var.isA()+"]";
    }
}
