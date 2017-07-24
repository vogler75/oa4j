/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.jni;

import at.rocworks.oa4j.var.TimeVar;

/**
 *
 * @author vogler
 */
public class HWObject {
    public String address;    
    public byte[] data;       
    public TimeVar orgTime;
    public boolean timeOfPeriphFlag;  
    
    public HWObject() {
        this.address=null;
        this.data=null;        
        this.orgTime=null;
        this.timeOfPeriphFlag=false;
    }
    
    public HWObject(String address, byte[] data, TimeVar orgTime, boolean timeOfPeriphFlag) {
        this.address=address;
        this.data=data;        
        this.orgTime=orgTime;     
        this.timeOfPeriphFlag=timeOfPeriphFlag;
    }    
}
