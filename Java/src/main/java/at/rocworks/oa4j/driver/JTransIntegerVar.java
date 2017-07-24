/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.driver;

import at.rocworks.oa4j.base.JDebug;
import at.rocworks.oa4j.var.IntegerVar;
import at.rocworks.oa4j.var.Variable;
import at.rocworks.oa4j.var.VariableType;

import java.nio.ByteBuffer;
import java.util.logging.Level;

/**
 *
 * @author vogler
 */
public class JTransIntegerVar extends JTransBaseVar {            
        
    public JTransIntegerVar(String name, int type) {
        super(name, type, VariableType.IntegerVar, Integer.SIZE);
    }               
    
    public JTransIntegerVar(String name, int type, int size) {
        super(name, type, VariableType.IntegerVar, size);           
    }       

    protected byte[] toPeriph_(Integer val) { return toPeriph(val); }
    public static byte[] toPeriph(Integer val) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.SIZE);
        buffer.putInt(val);
        return buffer.array();        
    }
    
    protected Integer toVal_(byte[] data) { return toVal(data); }
    public static Integer toVal(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data); // big-endian by default
        return buffer.getInt();        
    }    
    
    @Override
    public byte[] toPeriph(int dlen, Variable var, int subix) {
        try {
//            JDebug.out.log(Level.INFO, "toPeriph: dlen={0} var={1} subindex={2}", new Object[]{dlen, var.formatValue(), subix});
            //JDebug.sleep(100);
            if ( var.getIntegerVar() == null ) {
                JDebug.out.log(Level.WARNING, "toPeriph: Variable has no {0} value!", new Object[]{getVariableType().toString()});
                return null;
            } else {
                Integer val = var.getIntegerVar().getValue();
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
            Integer val = toVal_(data);
            IntegerVar var = new IntegerVar(val);
//            JDebug.out.log(Level.INFO, "toVar: data={0}", val);
            return var;
        } catch ( Exception ex) {
            JDebug.StackTrace(Level.SEVERE, ex);
            return null;
        }        
    }
}
