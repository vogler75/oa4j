/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.driver;

import at.rocworks.oa4j.jni.Transformation;
import at.rocworks.oa4j.var.VariableType;

/**
 *
 * @author vogler
 */
public abstract class JTransBaseVar extends Transformation {
    private final int itemSize;
    private final VariableType varType;
    
    public JTransBaseVar(String name, int type, VariableType varType, int itemSize) {
        super(name, type);
        this.varType = varType;
        this.itemSize = itemSize;
        //JDebug.out.log(Level.INFO, "JTransformationBaseVar: name={0} type={1} var={2} size={3}", new Object[]{name, type, varType, itemSize});
        //JDebug.sleep(100);
    }       

    @Override
    public int itemSize() {
        //JDebug.out.log(Level.INFO, "itemSize");
        //JDebug.sleep(100);
        return itemSize;
    }        

    @Override
    public int getVariableTypeAsInt() {
//        JDebug.out.log(Level.INFO, "getVariableTypeAsInt");
//        JDebug.sleep(100);
        return varType.value;
    }    
    
    public VariableType getVariableType() {
        return varType;
    }
    
    @Override
    public void delete() {
        //JDebug.out.log(Level.INFO, "delete {0}", getName());
    }
}
