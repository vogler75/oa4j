package at.rocworks.oa4j.logger.data.types.json;

import at.rocworks.oa4j.logger.data.base.ValueItem;
import at.rocworks.oa4j.var.TimeVar;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import at.rocworks.oa4j.var.VariableType;
import at.rocworks.oa4j.base.JDebug;

import org.json.simple.JSONObject;

public class ValueItemJSON extends ValueItem {

    private final SimpleDateFormat fmt = new SimpleDateFormat(TimeVar.FMT_DATE_JS_MS);    
    
    private final JSONObject obj;

    public ValueItemJSON(JSONObject obj) {
        this.obj = obj;
    }
    
    private String getTypeString() {
        String type=obj.containsKey("type") ? (String)obj.get("type") : "";
        return type!=null ? type : "";
    }

    @Override
    public VariableType getVariableType() {
        String type=getTypeString();
        
        switch (type) {
            case "string":
                return VariableType.TextVar;
            case "char":
                return VariableType.CharVar;
            case "int":
                return VariableType.IntegerVar;
            case "unsigned":
                return VariableType.UIntegerVar;
            case "bool":
                return VariableType.BitVar;
            case "float":
                return VariableType.FloatVar;
            case "time":
                return VariableType.TimeVar;
            default:
                return VariableType.Unknown;
        }   
    }

    @Override
    public String getString() {
        return (String) obj.get("string");
    }

    @Override
    public String getChar() {
        return (String) obj.get("char");
    }

    @Override
    public Long getLong() {
        return (Long) obj.get(getTypeString());
    }

    @Override
    public Double getDouble() {
        switch ( getTypeString() ) {
            case "float": {
                Object v = obj.get("float");
                return v == null ? null : v instanceof Long ? ((Long)v).doubleValue() : (Double)v;
            }
            case "int": {
                Object v = obj.get("int");
                return v == null ? null : v instanceof Long ? ((Long)v).doubleValue() : (Integer)v;               
            }
            default: 
                return null;
        }
    }

    @Override
    public Boolean getBoolean() {
        return (Boolean) obj.get("bool");
    }

    @Override
    public Date getTime() {
        try {
            Object v = obj.get("time");            
            return v==null ? null : fmt.parse((String) obj.get("time"));
        } catch (java.text.ParseException ex) {
            JDebug.out.log(Level.SEVERE, ex.toString());
            return null;
        }
    }

    @Override
    public Long getTimeMS() {
        Date v = getTime();
        return v==null ? null : v.getTime();
    }
    
    @Override
    public Object getValueObject() {
        return obj;
    }
}
