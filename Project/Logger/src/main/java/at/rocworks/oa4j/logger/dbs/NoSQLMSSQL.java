package at.rocworks.oa4j.logger.dbs;

import at.rocworks.oa4j.base.JDebug;
import at.rocworks.oa4j.logger.base.NoSQLServer;
import at.rocworks.oa4j.logger.base.NoSQLSettings;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 *
 * @author vogler
 */
public class NoSQLMSSQL extends NoSQLJDBC  {
    /*
    CREATE TABLE EVENTHISTORY (
	tag [varchar](512) NOT NULL,
	ts [datetime2](7) NOT NULL,
	value_number [numeric](18, 0) NULL,
	value_string [varchar](max) NULL,
	value_timestamp [datetime2](7) NULL,
        status bigint NULL,
        manager int NULL,
        user_ int NULL
	primary key (tag, ts)
    );
    CREATE INDEX ix_eventhistory_ts ON eventhistory(ts);
    */
    
    public static NoSQLServer creaetServer(NoSQLSettings srvcfg, String srvprefix) {
        String url = srvcfg.getStringProperty(srvprefix, "url", "jdbc:sqlserver://localhost:1433;databaseName=pvss;");
        String usr = srvcfg.getStringProperty(srvprefix, "username", "pvssrdb");
        String pwd = srvcfg.getStringProperty(srvprefix, "password", "manager");
        String grp = srvcfg.getStringProperty(srvprefix, "archive", "EVENT");
        JDebug.out.log(Level.CONFIG, "{0}.type: {1}\n{2}\nurl: {3}",
                new Object[]{srvprefix, NoSQLMSSQL.class.getName(), srvcfg, url});
        return new NoSQLMSSQL(srvcfg, url, usr, pwd, grp);
    }    
    
    public NoSQLMSSQL(NoSQLSettings settings, String url, String username, String password, String archive) {
        //jdbc:sqlserver://localhost:1433;databaseName=pvss;
        super(settings, "com.microsoft.sqlserver.jdbc.SQLServerDriver", url, username, password, archive);

        this.sqlInsertStmt
                = "INSERT INTO " + archive + "HISTORY WITH (ROWLOCK) "
                + "(tag, ts, value_number, value_string, value_timestamp, status, manager, user_)"
                + "VALUES (?,?,?,?,?,?,?,?)";

        this.sqlSelectStmt
                = "SELECT %s FROM " + archive + "HISTORY WITH (NOLOCK) "
                + " WHERE tag=? AND ts>=? AND ts<=?";
    }            

    @Override
    protected void afterInsert(Connection conn) throws SQLException {    
    }        
}
