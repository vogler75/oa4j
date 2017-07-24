package at.rocworks.oa4j.logger.query;

import java.io.Serializable;
import java.util.Date;
import at.rocworks.oa4j.var.Variable;

import at.rocworks.oa4j.logger.data.Dp;

public abstract class DpGetPeriodResult implements Serializable {

    protected int id;    
    
    protected volatile int size;
    protected int errorCode;    
    
    protected int chunkNumber;
    protected boolean lastChunk;    
    
    protected final int minChunkSize = 2048;
    protected final int maxChunkSize = 4096;
    
    private String groupName =  "";

    public DpGetPeriodResult() {
        this.id = 0;
        
        this.size = 0;
        this.errorCode = 0;                
        
        this.chunkNumber = 0;
        this.lastChunk = false;        
    }
    
    public DpGetPeriodResult(DpGetPeriodResult copy) {
        this.id=copy.id;
        
        this.size=copy.size;
        this.errorCode=copy.errorCode;
        
        this.chunkNumber=copy.chunkNumber;
        this.lastChunk=copy.lastChunk;
        
        this.groupName=copy.groupName;
    }    
    
    abstract public DpGetPeriodResult getChunk();    

    abstract public byte[] getBytes();

    abstract public void addValue(Dp dp, Date ts, Object value);

    abstract public void addVariable(Dp dp, Date ts, Variable value);
    
    public int getSize() {
        return this.size;
    }
    
    public boolean isLast() {
        return this.lastChunk;
    }
    
    public void setLast() {
        synchronized (this) {
            this.lastChunk=true;
        }
    }
    
    public void setError(int error) {
        this.errorCode=error;
    }
    
    public int getError() {
        return this.errorCode;
    }
    
    public void setId(int id) {
        this.id=id;
    }
    
    public int getId() {
        return this.id;
    }    
    
    public int getChunkNr() {
        return this.chunkNumber;
    }
    
    public void setGroupName(String group) {
        this.groupName=group;
    }
    
    public String getGroupName() {
        return this.groupName;
    }
}
