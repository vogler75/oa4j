package at.rocworks.oa4j.logger.base;

import at.rocworks.oa4j.logger.data.Metrics;
import at.rocworks.oa4j.logger.data.base.DataItem;
import at.rocworks.oa4j.logger.data.lists.IDataListImmutable;
import at.rocworks.oa4j.logger.query.DpGetPeriodParameter;
import at.rocworks.oa4j.logger.query.DpGetPeriodResult;
import at.rocworks.oa4j.base.JDebug;

import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;

public class NoSQLLogger implements INoSQLInterface {

    private final ArrayList<NoSQLGroup> noSQLGroups;
    private volatile int readGroupNr = -1;

    private volatile long eventCounter = 0;

    private long getEventCounter() {
        long events = this.eventCounter;
        this.eventCounter = 0;
        return events;
    }

    private void incEventCounter(long events) {
        this.eventCounter += events;
    }

    public NoSQLLogger() {
        super();
        this.noSQLGroups = new ArrayList<>();
    }
    
    public boolean setReadGroup(int groupNr) {
        if ( groupNr >= 0 && groupNr < noSQLGroups.size() ) {
            this.readGroupNr=groupNr;
            return true;
        } else {
            return false;
        }
    }
    
    public boolean setReadGroup(String groupName) {
        int id=-1;
        for ( int i=0; i<noSQLGroups.size(); i++ )
            if (noSQLGroups.get(i) != null && noSQLGroups.get(i).getGroupName().equals(groupName)) 
                id=i;
        return NoSQLLogger.this.setReadGroup(id);
    }    
    
    public int getReadGroupNr() {
        return this.readGroupNr; 
    }

    public NoSQLGroup getNoSQLGroup(int nr) {
        return nr < this.noSQLGroups.size() ? this.noSQLGroups.get(nr) : null;
    }
    
    public int getNoSQLGroupCount() {
        return this.noSQLGroups.size();
    }

//    public void setNoSQLGroup(int nr, NoSQLGroup noSQLGroup) {
//        this.noSQLGroups[nr] = noSQLGroup;
//    }
    
    public void addNoSQLGroup(NoSQLGroup noSQLGroup) {
        this.noSQLGroups.add(noSQLGroup);
    }    
    
    private Date t1 = new Date();
    public Metrics getStats() {
        Metrics stats = new Metrics(this.getClass().getSimpleName());
        
        long msec = new Date().getTime() - t1.getTime();        
        long counter = getEventCounter();
        double vsec = Double.valueOf(Math.round((double) counter / (msec / 1000.0)));
        t1 = new Date();            
        stats.put("vsec", vsec);
        
        for (NoSQLGroup noSQLGroup : this.noSQLGroups) {
            if ( noSQLGroup != null )
                stats.put(noSQLGroup.getStats());
        }
        return stats;
    }

    @Override
    public int collectData(IDataListImmutable list) {    
        incEventCounter(list.getHighWaterMark()+1);        
        int ok = 0;        
        for (NoSQLGroup g : noSQLGroups) {
            if (g != null && g.collectData(list) == INoSQLInterface.OK) {
                ok++;
            }
        }
        return ok > 0 ? INoSQLInterface.OK : INoSQLInterface.ERR_UNRECOVERABLE;
    }
    

    @Override
    public int collectData(DataItem item) {
        incEventCounter(1);        
        int ok = 0;
        for (NoSQLGroup g : noSQLGroups) {
            if (g != null && g.collectData(item) == INoSQLInterface.OK) {                
                ok++;
            }
        }
        return ok > 0 ? INoSQLInterface.OK : INoSQLInterface.ERR_UNRECOVERABLE;        
    }    

    @Override
    public boolean dpGetPeriod(DpGetPeriodParameter p, DpGetPeriodResult r) {
        final int groupNr = this.getReadGroupNr();
        r.setGroupName(noSQLGroups.get(groupNr).getGroupName());
        return noSQLGroups.get(groupNr).dpGetPeriod(p, r);
    }

    public void setActive() {
        for (NoSQLGroup g : noSQLGroups) {
            if (g != null) {
                g.setActive();
            }
        }
    }

    public void setPassive() {
        for (NoSQLGroup g : noSQLGroups) {
            if (g != null) {
                g.setPassive();
            }
        }
    }
    
    public void startNoSQLGroups() {
        for (NoSQLGroup g : noSQLGroups) {
            if (g != null) {
                JDebug.out.log(Level.INFO, "start {0}", g.getGroupName());
                g.startNoSQLServers();
            }
        }
    }

}
