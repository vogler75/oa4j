/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.jni;

import at.rocworks.oa4j.var.Variable;

/**
 *
 * @author vogler
 */
public abstract class Transformation {
    private final String name;
    private final int type;
    
    public Transformation(String name, int type) {
        this.name=name;
        this.type=type;
    }
    
    public String getName() {
        return name;
    }
    
    public int getType() {
        return type;
    }
       
    public abstract int itemSize();            
    public abstract int getVariableTypeAsInt();
    public abstract byte[] toPeriph(int blen, Variable var, int subix);    
    public abstract Variable toVar(byte[] data, int dlen, int subix);
    
    public abstract void delete();
}
