/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.data.types.api;

import java.util.Date;
import java.util.logging.Level;

import at.rocworks.oa4j.logger.data.base.ValueItem;
import at.rocworks.oa4j.var.BitVar;
import at.rocworks.oa4j.var.CharVar;
import at.rocworks.oa4j.var.Variable;
import at.rocworks.oa4j.var.VariableType;
import at.rocworks.oa4j.var.FloatVar;
import at.rocworks.oa4j.var.IntegerVar;
import at.rocworks.oa4j.var.TextVar;
import at.rocworks.oa4j.var.TimeVar;
import at.rocworks.oa4j.var.UIntegerVar;
import at.rocworks.oa4j.base.JDebug;
import java.io.Serializable;

/**
 *
 * @author vogler
 */
public class ValueItemAPI extends ValueItem implements Serializable {
    private final Variable var;       
    
    public ValueItemAPI(Variable val) {
        this.var=val;
    }        

    @Override
    public VariableType getVariableType() {
        return var.isA();
    }    
    
    @Override
    public String getString() {
        switch (var.isA()) {
            case TextVar:
                return ((TextVar) var).getValue();
            case CharVar:
                return ((CharVar) var).getValue().toString();
            default:
                return null;
        }
    }

    @Override
    public String getChar() {
        return var.isA()==VariableType.CharVar ? ((CharVar)var).getValue().toString() : null;        
    }

    @Override
    public Long getLong() {
        switch (var.isA()) {
            case IntegerVar:
                return ((IntegerVar)var).getValue().longValue();
            case UIntegerVar:
                return ((UIntegerVar)var).getValue().longValue();
            default:
                JDebug.out.log(Level.SEVERE, "ValueItemAPI: unknown type {0}", var.isA());
                return null;
        }
    }

    @Override
    public Double getDouble() {
        switch ( var.isA()) {
            case FloatVar: return ((FloatVar)var).getValue(); 
            case IntegerVar: 
            case UIntegerVar: return ((IntegerVar)var).getValue().doubleValue();
            case BitVar: return ((BitVar)var).getValue() ? 1.0 : 0.0;
            case TimeVar: return (double)((TimeVar)var).getValue().getTime();
            default: return null;            
        }
    }

    @Override
    public Boolean getBoolean() {
        return var.isA()==VariableType.BitVar ? ((BitVar)var).getValue() : null;
    }

    @Override
    public Date getTime() {
        return var.isA()==VariableType.TimeVar ? ((TimeVar)var).getValue() : null;
    }

    @Override
    public Long getTimeMS() {
        return getTime() != null ? getTime().getTime() : null;
    }    
    
    @Override
    public Object getValueObject() {
        return var;
    }
}
