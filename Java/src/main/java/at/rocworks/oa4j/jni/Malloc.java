/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.jni;

/**
 *
 * @author vogler
 */
public abstract class Malloc {    
    protected volatile long cptr;    
    
    public void setPointer(long cptr) {
        this.cptr = cptr;
    }
    
    public long getPointer() {
        return cptr;
    }
    
    public void newMemory() {
        this.cptr = malloc();
    }
    
    public void delMemory() {
        free(cptr);
    }
         
    protected abstract long malloc();
    protected abstract void free(long cptr);
}
