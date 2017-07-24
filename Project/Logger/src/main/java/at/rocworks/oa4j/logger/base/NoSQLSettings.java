package at.rocworks.oa4j.logger.base;

import java.util.Properties; 

public class NoSQLSettings implements Cloneable {

    public enum DistributionType { 
        ROBIN, PINNED
    }
    
    //public static final String ROBIN = "Robin";
    //public static final String PINNED = "Pinned";

    private final Properties props;
    private String path;    
       
    private int threads = 0;
    private int blocksize = 8;
    private int flushinterval = 1000;
    private int maxmemblocks = 16;
    private int buffertodisk = 0;
    private String bufferclass = "at.rocworks.oc4j.logger.queues.BufferQueueFlatfiles";
    private String bufferpath = "buffer";
    
    private DistributionType distribution = DistributionType.ROBIN;    

    private boolean redundancy = false;    
   
    public NoSQLSettings(String path, int threads, int blocksize, int flushinterval, int maxmemblocks, int buffertodisk, String bufferclass, String bufferpath, String distribution, boolean redundancy) {
        this.path=path;
        this.props = new Properties();       
        this.props.put(path+".threads", threads);
        this.props.put(path+".blocksize", blocksize);
        this.props.put(path+".flushinterval", flushinterval);
        this.props.put(path+".maxmemblocks", maxmemblocks);
        this.props.put(path+".buffertodisk", buffertodisk);
        this.props.put(path+".bufferclass", bufferclass);        
        this.props.put(path+".bufferpath", bufferpath);        
        this.props.put(path+".distribution", distribution);        
        this.props.put(path+".redundancy", redundancy);
        readProperties(path);
    }    
    
    public NoSQLSettings(Properties props, String path) {
        this.props = props;
        readProperties(path);
    }

    public NoSQLSettings cloneWithNewPath(String path) throws CloneNotSupportedException {
        NoSQLSettings c = clone();
        c.readProperties(path);
        return c;
    }
    
    @Override
    public NoSQLSettings clone() throws CloneNotSupportedException {
        return (NoSQLSettings) super.clone();
    }

    @Override
    public String toString() {
        return "threads: " + getThreads() + "\n"
                + "blocksize: " + getBlocksize() + "\n"
                + "flushinterval: " + getFlushinterval() + "\n"
                + "maxmemblocks: " + getMaxmemblocks() + "\n"
                + "buffertodisk: " + getBuffertodisk() + "\n"
                + "bufferpath: " + getBufferpath() + "\n"                
                + "bufferclass: " + getBufferclass() + "\n"
                + "distribution: " + getDistribution() + "\n"
                + "redundancy: " + redundancy;
    }

    public String getName() {
        return path;
    }    
        
    public String getShortName() {
        String[] parts = path.split("\\."); // <dbname>.server.<nr>
        if ( parts.length == 3 ) return "S"+parts[2];
        else return path;
    }    

    public boolean isRedu() {
        return redundancy;
    }

    private void readProperties(String path) {
        if ( path.equals("") )
            path = this.path;
        else
            this.path = path;
        
       
        threads = getIntProperty(path, "threads", getThreads());
        if (getThreads() <= 0) {
            threads = Runtime.getRuntime().availableProcessors()+getThreads(); 
            if ( getThreads() < 1 ) threads=1;
        } 
        
        blocksize = getIntProperty(path, "blocksize", getBlocksize());
        if (getBlocksize() <= 0) {
            blocksize = 1;
        }

        flushinterval = getIntProperty(path, "flushinterval", getFlushinterval());
        if (getFlushinterval() < -1) {
            flushinterval = -1; // flush immediate
        }
        maxmemblocks = getIntProperty(path, "maxmemblocks", getMaxmemblocks());
        if (getMaxmemblocks() < 0) {
            maxmemblocks = -1; // infinite
        }
        buffertodisk = getIntProperty(path, "buffertodisk", getBuffertodisk());
        bufferpath = getStringProperty(path, "bufferpath", getBufferpath());        
        bufferclass = getStringProperty(path, "bufferclass", getBufferclass());        
        
        String distributionName = getStringProperty(path, "distribution", "");
        switch ( distributionName.toUpperCase()) {
            case "ROBIN": distribution=DistributionType.ROBIN; break;
            case "PINNED": distribution=DistributionType.PINNED; break;            
        }

        redundancy = getBoolProperty(path, "redundancy", redundancy);
    }

    public int getIntProperty(String path, String name, int defaultValue) {
        return Integer.parseInt(props.getProperty((path.equals("") ? this.path : path) + "." + name, Integer.toString(defaultValue)));
    }

    public boolean getBoolProperty(String path, String name, boolean defaultValue) {
        return (props.getProperty((path.equals("") ? this.path : path) + "." + name, defaultValue ? "true" : "false")).equals("true");
    }

    public String getStringProperty(String path, String name, String defaultValue) {
        return props.getProperty((path.equals("") ? this.path : path) + "." + name, defaultValue);
    }

    /**
     * @return the threads
     */
    public int getThreads() {
        return threads;
    }

    /**
     * @return the blocksize
     */
    public int getBlocksize() {
        return blocksize;
    }

    /**
     * @return the flushinterval
     */
    public int getFlushinterval() {
        return flushinterval;
    }

    /**
     * @return the maxmemblocks
     */
    public int getMaxmemblocks() {
        return maxmemblocks;
    }

    /**
     * @return the buffertodisk
     */
    public int getBuffertodisk() {
        return buffertodisk;
    }

    /**
     * @return the distribution
     */
    public DistributionType getDistribution() {
        return distribution;
    }
    
    /**
     * @return the bufferclass
     */
    public String getBufferclass() {
        return bufferclass;
    }  
    
    /**
     * @return the bufferclass
     */
    public String getBufferpath() {
        return bufferpath;
    }            
}
