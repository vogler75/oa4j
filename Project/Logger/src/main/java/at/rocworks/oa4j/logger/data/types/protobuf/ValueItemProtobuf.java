/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.data.types.protobuf;

import at.rocworks.oa4j.base.JDebug;
import at.rocworks.oa4j.var.VariableType;
import at.rocworks.oa4j.logger.data.base.ValueItem;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

/**
 *
 * @author vogler
 */
public class ValueItemProtobuf extends ValueItem {

    DpeValueProtos.DpeValueList.DpeValue obj;
    
    private final SimpleDateFormat fmt = new SimpleDateFormat(EventItemProtobuf.FMT_TIME);        
    
    public ValueItemProtobuf(DpeValueProtos.DpeValueList.DpeValue item) {
        this.obj=item;
    }
    
    @Override
    public VariableType getVariableType() {
        switch ( obj.getMetadata().getValueType() ) {  
            case BIT32: 
                return VariableType.Bit32Var;
            case BIT64: 
                return VariableType.Bit64Var;
            case BOOL:
                return VariableType.BitVar;
            case CHAR:
                return VariableType.CharVar;
            case DP_IDEN:
                return VariableType.DpIdentifierVar;
            case FLOAT_DOUBLE:
                return VariableType.FloatVar;
            case INT32:
                return VariableType.IntegerVar;
            case LANG_STRING:
                return VariableType.LangTextVar;
            case TEXT:
                return VariableType.TextVar;
            case TIME:
                return VariableType.TimeVar;
            case UINT32:
                return VariableType.UIntegerVar;
            default:
                throw new UnsupportedOperationException("Value Type not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }        
    }

    @Override
    public String getString() {
        switch ( obj.getMetadata().getValueType() ) {  
            case TEXT: return obj.getOriginalValueText();
            case CHAR: return String.valueOf(obj.getOriginalValueChar());
            default: return null;
        }            
    }

    @Override
    public String getChar() {
        return obj.hasOriginalValueChar() ? String.valueOf(obj.getOriginalValueChar()) : null;
    }

    @Override
    public Long getLong() {
        switch ( obj.getMetadata().getValueType() ) {  
            case INT32: return Long.valueOf(obj.getOriginalValueInt32());
            case UINT32: return Long.valueOf(obj.getOriginalValueUInt32());
            case BIT32: return Long.valueOf(obj.getOriginalValueBit32());
            case BIT64: return obj.getOriginalValueBit64();
            default: return null;
        }                   
    }

    @Override
    public Double getDouble() {
        switch ( obj.getMetadata().getValueType() ) {
            case FLOAT_DOUBLE: return obj.getOriginalValueFloatDouble();
            case INT32: return Double.valueOf(obj.getOriginalValueInt32());
            case UINT32: return Double.valueOf(obj.getOriginalValueUInt32());
            case BOOL: return obj.getOriginalValueBool() ? 1.0 : 0.0;
            case TIME: return Double.valueOf(this.getTime().getTime());
            default: return null;            
        }        
    }

    @Override
    public Boolean getBoolean() {
        return obj.hasOriginalValueBool() ? obj.getOriginalValueBool() : null;
    }

    @Override
    public Date getTime() {
        try {
            return obj.hasOriginalValueTime() ? fmt.parse(obj.getOriginalValueTime()) : null;
        } catch (ParseException ex) {
            JDebug.StackTrace(Level.SEVERE, ex);
            return null;
        }
    }

    @Override
    public Long getTimeMS() {
        return getTime().getTime()*1000;
    }
    
    @Override
    public Object getValueObject() {
        return obj;
    }    
}
