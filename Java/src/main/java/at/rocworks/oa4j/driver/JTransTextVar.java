/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.driver;

import at.rocworks.oa4j.var.VariableType;
import at.rocworks.oa4j.var.TextVar;
import at.rocworks.oa4j.var.Variable;
import at.rocworks.oa4j.base.JDebug;

import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

/**
 *
 * @author vogler
 */
public class JTransTextVar extends JTransBaseVar {

    private final TextVar var;
    private String val;    
    
    public JTransTextVar(String name, int type) {
        this(name, type, 4096);
    }    
    
    public JTransTextVar(String name, int type, int size) {
        super(name, type, VariableType.TextVar, size);
        var = new TextVar("");
        val = "";
    }    

    protected byte[] toPeriph_(String val) { return toPeriph(val); }
    public static byte[] toPeriph(String val) {
        return val.getBytes(StandardCharsets.UTF_8);        
    }
    
    protected String toVal_(byte[] data) { return toVal(data); }
    public static String toVal(byte[] data) {
       return new String(data, StandardCharsets.UTF_8); 
    }    
    
    @Override
    public byte[] toPeriph(int blen, Variable var, int subix) {
        try {
//            JDebug.out.log(Level.INFO, "toPeriph: dlen={0} var={1} subindex={2}", new Object[]{blen, var.formatValue(), subix});
//            JDebug.sleep(100);
            String text=null;
            if ( var.getTextVar()!=null ) {
                text = var.getTextVar().getValue();
            } else if (var.getFloatVar()!=null) {
                text = var.getFloatVar().formatValue();
            } else if (var.getIntegerVar()!=null) {
                text = var.getIntegerVar().formatValue();
            } else if (var.getLongVar()!=null) {
                text = var.getLongVar().formatValue();                
            } else if (var.getTimeVar()!=null) {
                text = var.getTimeVar().formatValue();
            }
            
            if (text == null) {
                JDebug.out.log(Level.WARNING, "toPeriph: Variable has no {0} value!", new Object[]{getVariableType().toString()});
                return null;
            } else if ( text.length() > itemSize() ) {
                JDebug.out.log(Level.WARNING, "toPeriph: Variable size is to big {0}/{1}!", new Object[]{var.getTextVar().getValue().length(), itemSize()});
                return null;
            } else {                
                val = text;
                return toPeriph_(val);              
            }
        } catch ( Exception ex) {
            JDebug.StackTrace(Level.SEVERE, ex);
            return null;
        }    
    }
    
    @Override
    public Variable toVar(byte[] data, int dlen, int subix) {
       try {
            //JDebug.out.log(Level.INFO, "toVar: data={0} dlen={1} subindex={2}", new Object[]{data, dlen, subix});
            if ( data.length > itemSize() ) {
                JDebug.out.log(Level.WARNING, "toVar: data size is to big {0}/{1}!", new Object[]{data.length, itemSize()});
                return null;
            } else {
                var.setValue(toVal_(data));       
                return var;
            }
        } catch ( Exception ex) {
            JDebug.StackTrace(Level.SEVERE, ex);
            return null;
        }        
    }
    
}
