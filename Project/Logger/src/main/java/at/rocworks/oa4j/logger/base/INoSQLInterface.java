package at.rocworks.oa4j.logger.base;

public interface INoSQLInterface extends IDataCollector, IDataReader {

    public static final int OK = 0;
    public static final int ERR_REPEATABLE = -1;
    public static final int ERR_UNRECOVERABLE = -2;

}
