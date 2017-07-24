package at.rocworks.oa4j.logger.base;

import at.rocworks.oa4j.logger.data.Metrics;
import at.rocworks.oa4j.logger.queues.BufferQueue;
import java.util.logging.Level;


import at.rocworks.oa4j.logger.query.DpGetPeriodParameter;
import at.rocworks.oa4j.logger.query.DpGetPeriodResult;
import at.rocworks.oa4j.logger.data.Dp;
import at.rocworks.oa4j.logger.data.DpAttr;
import at.rocworks.oa4j.logger.queues.IBufferQueue;
import at.rocworks.oa4j.base.JDebug;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//-----------------------------------------------------------------------------------------
public abstract class NoSQLServer implements IDataWriter, IDataReader {

    protected final int QUERY_MAX_RECORDS = 100000; 
    
    private final WorkerThread[] threads;
    private int currentThread = 0;

    protected NoSQLSettings settings;
    protected volatile boolean active = true;
    
    private int recordsWriteCount = 0;
    private long recordsWriteMSec = 0;      
    
    public NoSQLServer(NoSQLSettings settings) {
        super();
        this.settings = settings;
        this.threads = new WorkerThread[settings.getThreads()];
    }
    
    public void startThreads() {        
        for (int i = 0; i < this.threads.length; i++) {  
            String tname=this.getName()+".T"+i;
            IBufferQueue queue = null;
            if ( settings.getBuffertodisk() > 0 ) {
                String clazzName = settings.getBufferclass();
                try {
                    Class<?> clazz = Class.forName(clazzName);
                    Constructor<?> cons = clazz.getConstructor(String.class, String.class);
                    // TODO check if path (getBufferPath) exists!
                    Object obj = cons.newInstance(tname, settings.getBufferpath());
                    if ( obj instanceof IBufferQueue ) {
                        queue = new BufferQueue(tname, settings.getMaxmemblocks(), (IBufferQueue)obj);
                    } else {
                        JDebug.out.log(Level.SEVERE, "Class {0} does not implement interface {1}!", new Object[]{clazzName, IBufferQueue.class.getName()});
                    }                
                } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    JDebug.StackTrace(Level.SEVERE, ex);
                } catch (NoClassDefFoundError err) {
                    JDebug.out.log(Level.SEVERE, "Class {0} not defined!", clazzName);
                }
            }
            
            if ( queue == null ) {
                queue = new BufferQueue(tname, settings.getMaxmemblocks());
            }
            JDebug.out.log(Level.INFO, "start {0} {1}", new Object[]{i, this.getName()});
            this.threads[i] = new WorkerThread(i, this, queue);
            this.threads[i].start();
        }        
    }      
    
    @Override
    public long getQueuesize() {
        long size=0;
        for (WorkerThread thread : threads) {
            if (thread != null) {
                size += thread.getQueuesize();
            }
        }
        return size;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void setActive() {
        if (!active) {
            JDebug.out.log(Level.WARNING, "{0} => ACTIVE", getName());
        }
        active = true;
    }

    public void setPassive() {
        if (active) {
            JDebug.out.log(Level.WARNING, "{0} => PASSIVE", getName());
        }
        active = false;
    }

    @Override
    public String getName() {
        return settings.getName();
    }
    
    public String getShortName() {
        return settings.getShortName();
    }
    
    @Override
    public int getFlushinterval() {
        return settings.getFlushinterval();
    }        
    @Override
    public int getBlocksize() {
        return settings.getBlocksize();
    }

    public int getNrOfThreads() {
        return settings.getThreads();
    }           
     
    protected synchronized int nextThread() {
        if (++currentThread >= threads.length) {
            currentThread = 0;
        }
        return currentThread;
    }
    
    public WorkerThread getWorker() {
        return threads[nextThread()];
    }
    
    public WorkerThread getWorker(int threadNr) {
        return threads[threadNr];
    }
    
    public void addServerStats(int records, long msec) {
        synchronized ( this ) {
            this.recordsWriteCount+=records;
            this.recordsWriteMSec+=msec;
        }
    }    
    
    private Date t1 = new Date();
    public Metrics getStats() {
        Metrics stats = new Metrics(getShortName());
        synchronized ( this ) {            
            stats.put("msec", (double)this.recordsWriteMSec);
            stats.put("recs", (double)this.recordsWriteCount);
            stats.put("qlen", (double)this.getQueuesize());
            
            long msec = new Date().getTime() - t1.getTime();        
            double vsec = Double.valueOf(Math.round((double)this.recordsWriteCount / (msec / 1000.0)));
            t1 =  new Date();            
            stats.put("vsec", vsec);            
            
            this.recordsWriteMSec=0;            
            this.recordsWriteCount=0;
        }
        for (WorkerThread thread : this.threads) {
            stats.put(thread.getStats());
        }
        return stats;
    }
    
    @Override
    public boolean dpGetPeriod(DpGetPeriodParameter param, DpGetPeriodResult result) {
        // Execute queries
        HashMap<Dp, Set<String>> dpConfigs = createDpConfigAttrMap(param.dps);
        int errors = dpConfigs.keySet().parallelStream()
                .mapToInt(dp -> {
                    return dpGetPeriod(param.t1, param.t2, dp, dpConfigs.get(dp), result) ? 0 : 1;
                })
                .sum(); // errors 

        result.setError(errors);
        result.setLast();                
        return true;          
    }       
    
    public abstract boolean dpGetPeriod(Date t1, Date t2, Dp dp, Set<String> configs, DpGetPeriodResult result);
    
    protected HashMap<Dp, Set<String>> createDpConfigAttrMap(List<Dp> dps) {
        // Convert list of dp configs to a set of dps + set of configs
        HashMap<Dp, Set<String>> dpConfigs = new HashMap<>();
        dps.stream()
                //.sorted((e1, e2) -> e1.getFQN().compareTo(e2.getFQN()))
                .forEach((dp)->{
                    Set<String> configs;
                    Dp dpEl = new Dp(dp.getSysDpEl());
                    if ((configs=dpConfigs.get(dpEl))==null) {
                        configs = new HashSet<>();
                        configs.add(dp.getConfig());
                        dpConfigs.put(dpEl, configs);
                    } else {
                        configs.add(dp.getConfig());
                    }
                });
        return dpConfigs;
    }    
    
    protected Set<DpAttr> createAttrSet(List<Dp> dps) {
        // Convert list of dp configs to a set of dps + set of configs
        Set<DpAttr> dpAttrs = new HashSet<>();
        dps.stream().forEach((dp)->dpAttrs.add(dp.getAttribute()));
        return dpAttrs;
    }        
        
    protected ArrayList<Dp> createDpConfigAttrList(Dp dp, Set<String> configs) {
        ArrayList<Dp> result = new ArrayList<>();                
        configs.forEach(config->{
            switch (config) {
                case "_offline.._value":
                    result.add(new Dp(dp.getSysDpEl() + ":_offline.._value", DpAttr.Value));
                    break;
                case "_offline.._stime":
                    result.add(new Dp(dp.getSysDpEl() + ":_offline.._stime", DpAttr.Stime));
                    break;
                case "_offline.._status":
                    result.add(new Dp(dp.getSysDpEl() + ":_offline.._status", DpAttr.Status));
                    break;
                case "_offline.._status64":
                    result.add(new Dp(dp.getSysDpEl() + ":_offline.._status64", DpAttr.Status64));
                    break;                            
                case "_offline.._manager":
                    result.add(new Dp(dp.getSysDpEl() + ":_offline.._manager", DpAttr.Manager));
                    break;
                case "_offline.._user":
                    result.add(new Dp(dp.getSysDpEl() + ":_offline.._user", DpAttr.User));
                    break;
            }
        });
        return result;        
    }        
}