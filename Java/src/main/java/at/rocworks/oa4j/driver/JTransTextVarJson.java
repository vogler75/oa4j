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
public class JTransTextVarJson extends JTransTextVar {
    private static final int SIZE=4096;
    
    public JTransTextVarJson(String name, int type) {
        super(name, type, SIZE);           
    }
    
    @Override
    protected byte[] toPeriph_(String val) { return toPeriph(val); }    
    public static byte[] toPeriph(String val) {
        JSONObject json = new JSONObject();
        json.put("Value", val);
        return json.toJSONString().getBytes();
    }
    
    @Override
    protected String toVal_(byte[] data) { return toVal(data); }    
    public static String toVal(byte[] data) throws IllegalArgumentException {
        JSONObject json = (JSONObject)JSONValue.parse(new String(data));
        Object val = json.get("Value");
        if ( val instanceof String ) 
            return (String)val;
        else {
            throw new IllegalArgumentException("unhandled value type " + val.getClass().getName()); 
        }            
    }               
}
