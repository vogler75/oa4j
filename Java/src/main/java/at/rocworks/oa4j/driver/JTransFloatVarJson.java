/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.driver;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 *
 * @author vogler
 */
public class JTransFloatVarJson extends JTransFloatVar {

    private static final int SIZE=1024;
    
    public JTransFloatVarJson(String name, int type) {
        super(name, type, SIZE);           
    }
    
    @Override
    protected byte[] toPeriph_(Double val) { return toPeriph(val); }    
    public static byte[] toPeriph(Double val) {
        JSONObject json = new JSONObject();
        json.put("Value", val);
        return json.toJSONString().getBytes();
    }
    
    @Override
    protected Double toVal_(byte[] data) { return toVal(data); }    
    public static Double toVal(byte[] data) throws IllegalArgumentException {
        JSONObject json = (JSONObject)JSONValue.parse(new String(data));
        Object val = json.get("Value");        
        if (val == null) {
            throw new IllegalArgumentException("no key \"Value\" in json object!");             
        } else if (val instanceof Double) {
            return (Double)val;
        } else if (val instanceof Long) { // if there is no dot in the string
            return ((Long)val).doubleValue();
        }
        else {
            throw new IllegalArgumentException("unhandled value type " + val.getClass().getName()); 
        }        
    }       
}
