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
public class JTransIntegerVarJson extends JTransIntegerVar {
    private static final int SIZE=1024;
    
    public JTransIntegerVarJson(String name, int type) {
        super(name, type, SIZE);           
    }
    
    @Override
    protected byte[] toPeriph_(Integer val) { return toPeriph(val); }    
    public static byte[] toPeriph(Integer val) {
        JSONObject json = new JSONObject();
        json.put("Value", val);
        return json.toJSONString().getBytes();
    }
    
    @Override
    protected Integer toVal_(byte[] data) { return toVal(data); }    
    public static Integer toVal(byte[] data) throws IllegalArgumentException {
        JSONObject json = (JSONObject)JSONValue.parse(new String(data));
        Object val = json.get("Value");
        if ( val instanceof Long )
            return ((Long)val).intValue();
        else {
            throw new IllegalArgumentException("unhandled value type " + val.getClass().getName()); 
        }
    }           
}
