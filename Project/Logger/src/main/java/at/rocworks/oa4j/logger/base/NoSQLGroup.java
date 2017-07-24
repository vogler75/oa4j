package at.rocworks.oa4j.logger.base;

import at.rocworks.oa4j.logger.data.Metrics;
import at.rocworks.oa4j.logger.query.DpGetPeriodParameter;
import at.rocworks.oa4j.logger.query.DpGetPeriodResult;
import at.rocworks.oa4j.base.JDebug;

import java.util.logging.Level;

public abstract class NoSQLGroup implements IDataReader, IDataCollector {

    protected volatile int currentServer;
    protected NoSQLServer[] noSQLServer;
    protected int nrOfServers;

    private final String groupName;    

    public String getGroupName() {
        return groupName;
    }

    public NoSQLGroup(String name, int servers) {
        this.groupName = name;
        this.nrOfServers = servers;
        this.noSQLServer = new NoSQLServer[servers];
        this.currentServer = servers > 0 ? 0 : -1;
    }

    public void setNoSQLServer(int nr, NoSQLServer noSQLServer) {
        this.noSQLServer[nr] = noSQLServer;
    }
    
    public void startNoSQLServers() {
        for (NoSQLServer server : noSQLServer) {            
            if ( server != null ) {
                JDebug.out.log(Level.INFO, "start {0}", server.getName());
                server.startThreads();
            }
        }
    }

    protected synchronized int nextServer() {
        if (noSQLServer.length > 1) {
            int k = currentServer; 
            for (int i = 0; i < noSQLServer.length; i++) {
                if (++k >= noSQLServer.length) {
                    k = 0;
                }
                if (noSQLServer[k] != null) {
                    break;
                }
            }
            currentServer = k;
        }
        return currentServer;
    }

    @Override
    public boolean dpGetPeriod(DpGetPeriodParameter param, DpGetPeriodResult result) {
        nextServer();
        return currentServer >= 0 ? noSQLServer[currentServer].dpGetPeriod(param, result) : false;
    }

    public void setActive() {
        for (NoSQLServer s : noSQLServer) {
            if (s != null) {
                s.setActive();
            }
        }
    }

    public void setPassive() {
        for (NoSQLServer s : noSQLServer) {
            if (s != null) {
                s.setPassive();
            }
        }
    }
    
    public Metrics getStats() {
        Metrics stats = new Metrics(getGroupName());
        for (NoSQLServer server : this.noSQLServer) {
            stats.put(server.getStats());            
        }
        return stats;
    }
}
