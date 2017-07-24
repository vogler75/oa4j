/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.driver;

import at.rocworks.oa4j.var.FloatVar;
import at.rocworks.oa4j.var.Variable;
import at.rocworks.oa4j.var.VariableType;
import at.rocworks.oa4j.base.JDebug;
import java.nio.ByteBuffer;
import java.util.logging.Level;

/**
 *
 * @author vogler
 */
public class JTransFloatVar extends JTransBaseVar {
        
    public JTransFloatVar(String name, int type) {
        super(name, type, VariableType.FloatVar, Double.SIZE);           
    }
    
    public JTransFloatVar(String name, int type, int size) {
        super(name, type, VariableType.FloatVar, size);           
    }    
    
    protected byte[] toPeriph_(Double val) { return toPeriph(val); }    
    public static byte[] toPeriph(Double val) {
        ByteBuffer buffer = ByteBuffer.allocate(Double.SIZE);
        buffer.putDouble(val);
        return buffer.array();        
    }
    
    protected Double toVal_(byte[] data) { return toVal(data); }
    public static Double toVal(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data); // big-endian by default
        return buffer.getDouble();        
    }

    @Override
    public byte[] toPeriph(int dlen, Variable var, int subix) {
        try {
            //JDebug.out.log(Level.INFO, "toPeriph: dlen={0} var={1} subindex={2}", new Object[]{dlen, var.formatValue(), subix});
            if ( var.getFloatVar()== null ) {
                JDebug.out.log(Level.WARNING, "toPeriph: Variable has no {0} value!", new Object[]{getVariableType().toString()});
                return null;
            } else {   
                Double val = var.getFloatVar().getValue();
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
//            JDebug.out.log(Level.INFO, "toVar: data={0} dlen={1} subindex={2}", new Object[]{data.toString(), dlen, subix});
//            JDebug.sleep(100);
//            ByteBuffer buffer = ByteBuffer.wrap(data); // big-endian by default
//            Double val = buffer.getDouble();
            Double val = toVal_(data);
            FloatVar var = new FloatVar(val);
//            JDebug.out.log(Level.INFO, "toVar: data={0}", val);
            return var;
        } catch ( Exception ex) {
            JDebug.StackTrace(Level.SEVERE, ex);
            return null;
        }        
    }    
}
