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
public class NoSQLPostgres extends NoSQLJDBC {
    /*
    postgres=# create database pvss;
    CREATE DATABASE
    postgres=# create user pvssrdb with password 'manager';
    CREATE ROLE
    postgres=# grant all privileges on database pvss to pvssrdb;
    GRANT
    
    \c pvss
    
    
    CREATE TABLE EVENTHISTORY (
	tag VARCHAR(512) NOT NULL,
	ts TIMESTAMP(3) NOT NULL,
	value_number DECIMAL NULL,
	value_string TEXT NULL,
	value_timestamp TIMESTAMP(3) NULL,
        status BIGINT NULL,
        manager INT NULL,
        user_ INT NULL,
	primary key (tag, ts)
    );
    create index ix_eventhistory_ts on eventhistory(ts);
    
    grant all on eventhistory to pvssrdb;
    */
    
    public static NoSQLServer createServer(NoSQLSettings srvcfg, String srvprefix) {
        String url = srvcfg.getStringProperty(srvprefix, "url", "jdbc:postgres://localhost/pvss");
        String usr = srvcfg.getStringProperty(srvprefix, "username", "pvssrdb");
        String pwd = srvcfg.getStringProperty(srvprefix, "password", "manager");
        String grp = srvcfg.getStringProperty(srvprefix, "archive", "EVENT");
        JDebug.out.log(Level.CONFIG, "{0}.type: {1}\n{2}\nurl: {3}", new Object[]{srvprefix, NoSQLPostgres.class.getName(), srvcfg, url});
        return new NoSQLPostgres(srvcfg, url, usr, pwd, grp);
    }   
    
    public NoSQLPostgres(NoSQLSettings settings, String url, String username, String password, String archive) {
        super(settings, "org.postgresql.Driver", url, username, password, archive);
        this.sqlInsertStmt= "INSERT INTO " + archive + "HISTORY "
                + "(tag, ts, value_number, value_string, value_timestamp, status, manager, user_)"
                + "VALUES (?,?,?,?,?,?,?,?) "
                + "ON CONFLICT ON CONSTRAINT " + archive + "HISTORY_PKEY "
                + "DO NOTHING";  
    }

    @Override
    protected void afterInsert(Connection conn) throws SQLException {        
    }
    
}
