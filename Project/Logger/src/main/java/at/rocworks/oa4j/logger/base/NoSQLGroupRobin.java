package at.rocworks.oa4j.logger.base;

import at.rocworks.oa4j.logger.data.base.DataItem;
import at.rocworks.oa4j.logger.data.lists.IDataListImmutable;

public class NoSQLGroupRobin extends NoSQLGroup { 

    public NoSQLGroupRobin(String name, int servers) {
        super(name, servers);
    }

    @Override
    public int collectData(IDataListImmutable list) {
        nextServer();
        if ( currentServer >= 0 ) {
            noSQLServer[currentServer].getWorker().collectData(list);
            return INoSQLInterface.OK;
        } else {
            return INoSQLInterface.ERR_UNRECOVERABLE;
        }
    }

    @Override
    public int collectData(DataItem item) {
        nextServer();
        if ( currentServer >= 0 ) {
            noSQLServer[currentServer].getWorker().collectData(item);
            return INoSQLInterface.OK;
        } else {
            return INoSQLInterface.ERR_UNRECOVERABLE;
        }
    }

}
