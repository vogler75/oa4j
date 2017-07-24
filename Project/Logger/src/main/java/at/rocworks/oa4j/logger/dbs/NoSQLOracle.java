/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
public class NoSQLOracle extends NoSQLJDBC {

    /*
    CREATE TABLE EVENTLOG (
	tag varchar2(512) NOT NULL,
	ts timestamp NOT NULL,
	value_number number NULL,
	value_string varchar2(2000) NULL,
	value_timestamp timestamp NULL,
        status int NULL,
        manager int NULL,
        user_ int NULL,
	primary key (tag, ts) using index tablespace eventidx
    ) TABLESPACE EVENTLOG;        
    */    
    
    private NoSQLServer createOracle(NoSQLSettings srvcfg, String srvprefix) {
        String url = srvcfg.getStringProperty(srvprefix, "url", "@(DESCRIPTION=(ADDRESS=(PROTOCOL=tcp)(HOST=localhost)(PORT=1521))(CONNECT_DATA=(SERVICE_NAME=orcl)))");
        String usr = srvcfg.getStringProperty(srvprefix, "username", "pvssrdb");
        String pwd = srvcfg.getStringProperty(srvprefix, "password", "manager");
        String grp = srvcfg.getStringProperty(srvprefix, "archive", "EVENT");
        JDebug.out.log(Level.CONFIG, "{0}.type: {1}\n{3}\nurl: {4}",
                new Object[]{srvprefix, NoSQLOracle.class.getName(), srvcfg, url});
        return new NoSQLOracle(srvcfg, url, usr, pwd, grp);
    }    
    
    public NoSQLOracle(NoSQLSettings settings, String url, String username, String password, String archive) {
        //jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=tcp)(HOST=database)(PORT=1521))(CONNECT_DATA=(SERVICE_NAME=orcl)))

        super(settings, "oracle.jdbc.driver.OracleDriver", url, username, password, archive);                                       
        
        // default insert
        this.sqlInsertStmt 
                = "INSERT INTO " + archive + "LOG "
                + "(tag, ts, value_number, value_string, value_timestamp, status, manager, user_)"
                + "VALUES (?,?,?,?,?,?,?,?)";  
        
        // default select
        this.sqlSelectStmt
                = "SELECT %s FROM " + archive +"LOG "
                + " WHERE tag=? AND ts>=? AND ts<=?";          
    }     
        
    @Override
    protected void afterInsert(Connection conn) throws SQLException {    
    }  
}
