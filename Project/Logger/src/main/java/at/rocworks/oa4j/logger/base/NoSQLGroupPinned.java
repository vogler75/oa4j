package at.rocworks.oa4j.logger.base;

import at.rocworks.oa4j.logger.data.base.DataItem;
import at.rocworks.oa4j.logger.data.lists.DataList;
import at.rocworks.oa4j.logger.data.lists.IDataListImmutable;

public class NoSQLGroupPinned extends NoSQLGroup {

    private int currentThread;
    private int nrOfThreads;
    private final Cache cache;

    public NoSQLGroupPinned(String name, int servers) {
        super(name, servers);
        this.nrOfThreads = 0;
        this.currentThread = -1;
        this.cache = new Cache();
    }

    @Override
    public void setNoSQLServer(int nr, NoSQLServer noSQLServer) {
        if (this.noSQLServer[nr] != null) {
            nrOfThreads -= this.noSQLServer[nr].getNrOfThreads();
        }
        if (noSQLServer != null) {
            nrOfThreads += noSQLServer.getNrOfThreads();
        }
        super.setNoSQLServer(nr, noSQLServer);
    }

    private int nextThread() {
        if (++this.currentThread >= this.nrOfThreads) {
            this.currentThread = 0;
        }
        return this.currentThread;
    }

    @Override
    public int collectData(IDataListImmutable list) {
        if ( this.nrOfThreads==0 )
            return INoSQLInterface.ERR_UNRECOVERABLE;
               
        int i;
        DataItem item;
        DataList[] threadEvents = new DataList[this.nrOfThreads];
        int threadNr;
        
        synchronized (cache) {
            for (i = 0; i <= list.getHighWaterMark(); i++) {                
                if ((item = list.getItem(i)) == null) {
                    continue;
                }
                if ((threadNr = cache.getThreadNr(item)) == -1) {
                    threadNr = nextThread();
                    cache.setThreadNr(item, threadNr);
                }
                if (threadEvents[threadNr] == null) {
                    threadEvents[threadNr] = new DataList(list.size());
                }
                threadEvents[threadNr].addItem(item);
                cache.setTimeMS(item);                
            }
        }
        
        int j, k;
        k = -1;
        for (i = 0; i < nrOfServers; i++) { // TODO optimize without loop
            if (noSQLServer[i] != null) {
                for (j = 0; j < noSQLServer[i].getNrOfThreads(); j++) {                   
                    if (threadEvents[++k] != null) {
                        WorkerThread t = noSQLServer[i].getWorker(j);
                        if ( t != null ) t.collectData(threadEvents[k]);
                        threadEvents[k].clear();
                    }
                }
            }
        }        
        
        return INoSQLInterface.OK;
    }

    @Override
    public int collectData(DataItem item) {
        int threadNr;                
        synchronized ( cache ) {
            if ((threadNr = cache.getThreadNr(item)) == -1) {
                threadNr = nextThread();
                cache.setThreadNr(item, threadNr);
            }
            cache.setTimeMS(item);                        
        }
        
        int i, j, k;
        k = -1;
        for (i = 0; i < this.nrOfServers; i++) { // TODO optimize without loop
            if (this.noSQLServer[i] != null) {
                for (j = 0; j < this.noSQLServer[i].getNrOfThreads(); j++) {                   
                    if (++k == threadNr) {
                        noSQLServer[i].getWorker(j).collectData(item);  
                        break;
                    }
                }
            }
        }        
        
        return INoSQLInterface.OK;
    }
}