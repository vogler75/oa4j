/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.query;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;

import at.rocworks.oa4j.logger.data.Dp;
import at.rocworks.oa4j.var.DynVar;
import at.rocworks.oa4j.var.Variable;
import at.rocworks.oa4j.base.JDebug;
import at.rocworks.oa4j.base.JSemaphore;

/**
 *
 * @author vogler
 */
   
public class DpGetPeriodResultJava extends DpGetPeriodResult implements Serializable {    
           
    private HashMap<String, DpGetPeriodResultJavaTuple> data;
    
    private final JSemaphore chunkSize = new JSemaphore();
    
    public DpGetPeriodResultJava() {
        super();
        this.data = new HashMap<>();
    }
    
    public DpGetPeriodResultJava(DpGetPeriodResultJava copy) {
        super(copy); 
        this.data = copy.data;
    }
    
    @Override
    public DpGetPeriodResult getChunk() {
        //JDebug.out.log(Level.INFO, "getChunk await...");
        chunkSize.awaitGT(this.minChunkSize);
        DpGetPeriodResultJava chunk;               
        synchronized (this) {        
            //JDebug.out.log(Level.INFO, "getChunk awaited");
            chunk = new DpGetPeriodResultJava(this);                    
            //JDebug.out.log(Level.INFO, "getChunk got it");
            this.chunkNumber++;                    
            this.data = new HashMap<>();
            
            size=chunkSize.setValue(0);
        }
        return chunk;
    }
        
    @Override
    public void addValue(Dp dp, Date ts, Object value) {
        addVariable(dp, ts, Variable.newVariable(value));
    }
    
    @Override
    public void addVariable(Dp dp, Date ts, Variable value) {
        chunkSize.awaitLTE(this.maxChunkSize);
        
        String fqn = dp.getFQN();
        DpGetPeriodResultJavaTuple d = data.get(fqn);
        if ( d==null ) {
            synchronized (this) {                    
                data.put(fqn, d=new DpGetPeriodResultJavaTuple());
            }
        } 

        d.addVariable(ts, value);
        
        size=chunkSize.addOne();        
    }    
    
    @Override
    public void setLast() {
        super.setLast();
        chunkSize.setValue(Integer.MAX_VALUE);
    }    
    
    public boolean hasDp(String dp) {
        return data.containsKey(dp);
    }

    public DynVar getTimes(String dp) {
        return data.get(dp)!=null ? data.get(dp).mt : null;
    }
    
    public DynVar getValues(String dp) {
        return data.get(dp)!=null ? data.get(dp).mv : null;
    }
  
    @Override
    public byte[] getBytes() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(this);
            return bos.toByteArray();
        } catch (IOException ex) {
            JDebug.StackTrace(Level.SEVERE, ex);
            return new byte[0];
        }
    }
    
    static public DpGetPeriodResultJava createFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                ObjectInput in = new ObjectInputStream(bis)) {
            Object o = in.readObject();
            if (o instanceof DpGetPeriodResultJava) {
                return (DpGetPeriodResultJava) o;
            } else {
                return null;
            }
        }
    }   
}
