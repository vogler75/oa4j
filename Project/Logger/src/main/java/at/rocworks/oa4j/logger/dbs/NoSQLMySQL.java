/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.dbs;

import at.rocworks.oa4j.base.JDebug;

import at.rocworks.oa4j.logger.base.INoSQLInterface;
import at.rocworks.oa4j.logger.base.NoSQLServer;
import at.rocworks.oa4j.logger.base.NoSQLSettings;
import at.rocworks.oa4j.logger.data.base.DataItem;
import at.rocworks.oa4j.logger.data.base.EventItem;
import at.rocworks.oa4j.logger.data.base.ValueItem;
import at.rocworks.oa4j.logger.data.lists.DataList;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.logging.Level;

/**
 *
 * @author vogler
 */
public class NoSQLMySQL extends NoSQLJDBC {
    /*
    mysqld --defaults-file="C:\ProgramData\MySQL\MySQL Server 5.7\my.ini" --initialize --user="network service"
    mysql> alter user user() identified by 'manager';
    
    CREATE TABLE EVENTHISTORY (
	tag VARCHAR(512) NOT NULL,
	ts TIMESTAMP NOT NULL,
    //  ts TIMESTAMP(3) NOT NULL, // with nanos, available in mysql version >5.5!
        ns INT NOT NULL DEFAULT 0,
	value_number DECIMAL NULL,
	value_string TEXT NULL,
	value_timestamp TIMESTAMP NULL,
        status BIGINT NULL,
        manager INT NULL,
        user_ INT NULL,
	primary key (tag, ts, ms)
    );        
    CREATE INDEX EVENTHISTORY_TS on EVENTHISTORY(TS);
    */    

    public static NoSQLServer createServer(NoSQLSettings srvcfg, String srvprefix) {
        String url = srvcfg.getStringProperty(srvprefix, "url", "jdbc:mysql://localhost/pvss");
        String usr = srvcfg.getStringProperty(srvprefix, "username", "pvssrdb");
        String pwd = srvcfg.getStringProperty(srvprefix, "password", "manager");
        String grp = srvcfg.getStringProperty(srvprefix, "archive", "EVENT");
        JDebug.out.log(Level.CONFIG, "{0}.type: {1}\n{2}\nurl: {3}",
                new Object[]{srvprefix, NoSQLMySQL.class.getName(), srvcfg, url});
        return new NoSQLMySQL(srvcfg, url, usr, pwd, grp);
    }    
    
    public NoSQLMySQL(NoSQLSettings settings, String url, String username, String password, String archive) {
        super(settings, "com.mysql.jdbc.Driver", url, username, password, archive);

        // default insert
        this.sqlInsertStmt 
                = "INSERT INTO " + archive + "HISTORY "
                + "(tag, ts, value_number, value_string, value_timestamp, status, manager, user_, ns)"
                + "VALUES (?,?,?,?,?,?,?,?,?) ";
        
        // default select
        this.sqlSelectStmt
                = "SELECT %s FROM " + archive +"HISTORY "
                +" WHERE tag=? AND ts>=? AND ts<=?";         
    }

    @Override
    protected void afterInsert(Connection conn) throws SQLException {
    }
    
    @Override
    public int storeData(DataList list) {
        try {
            Connection conn = dataSourceWrite.getConnection();
            if ( conn != null )
            {
                int i;
                DataItem item;
                EventItem event;
                Object tag;
                
                conn.setAutoCommit(false);
                PreparedStatement stmt;                

                Date t1 = new Date();
                
                stmt = conn.prepareStatement(sqlInsertStmt);
                for (i = 0; i <= list.getHighWaterMark() && (item = list.getItem(i)) != null; i++) {
                    if ( !(item instanceof EventItem) ) continue;                    
                    event=(EventItem)item;
                    ValueItem val = event.getValue();
                    
                    tag = this.getTagOfDp(event.getDp());                    
                    if (tag == null) continue;
                    
                    if ( tag instanceof Long )
                        stmt.setLong(1, (Long)tag);
                    else if ( tag instanceof String )
                        stmt.setString(1, (String)tag);
                    
                    java.sql.Timestamp ts = new java.sql.Timestamp(event.getTimeMS()); 
                    ts.setNanos(event.getNanos());                      
                    stmt.setTimestamp(2, ts, cal);
                                                            
                    Double dval = val.getDouble();
                    if (dval != null) {
                        stmt.setDouble(3, dval);
                    } else {
                        stmt.setNull(3, Types.DOUBLE);
                    }
                    
                    // value_string                    
                    stmt.setString(4, val.getString());
                    
                    // value_timestamp
                    if ( val.getTimeMS() != null ) 
                        stmt.setTimestamp(5, new java.sql.Timestamp(val.getTimeMS()), cal);
                    else
                        stmt.setNull(5, Types.TIMESTAMP);
                    
                    
                    // status, manager, user
                    if ( event.hasAttributes() ) {
                        stmt.setLong(6, event.getStatus());
                        stmt.setInt(7, event.getManager());
                        stmt.setInt(8, event.getUser());
                    } else {
                        stmt.setNull(6, Types.INTEGER);
                        stmt.setNull(7, Types.INTEGER);
                        stmt.setNull(8, Types.INTEGER);
                    }
                    
                    stmt.setInt(9, event.getNanos()); // nanos
                    
                    //Debug.out.log(Level.FINE, "{0}:{1}/{2} [{3}]", new Object[] {i, element_id.toString(), ts.toString(), item.toString()});
                    
                    stmt.addBatch();                  
                }
                try {
                    stmt.executeBatch(); // TODO check result? int[] res =
                } catch (BatchUpdateException ex) {
                    JDebug.out.log(Level.SEVERE, "Batch exception {0} update count {1}.", new Object[]{ex.getErrorCode(), ex.getUpdateCounts().length});
                    JDebug.StackTrace(Level.SEVERE, ex);
                } catch (SQLException ex) {
                    SQLException current = ex;
                    do {
                        JDebug.out.log(Level.SEVERE, "SQL exception {0}.", new Object[]{ex.getErrorCode()});
                        JDebug.StackTrace(Level.SEVERE, current);
                    } while ((current = current.getNextException()) != null);                    
//                    for (i = 0; i <= list.getHighWaterMark() && (item = list.getItem(i)) != null; i++) {
//                        Debug.out.log(Level.INFO, "{0}", item.toJSONObject());
//                    }
                }
                Date t2 = new Date();
                stmt.close();
                
                afterInsert(conn);
                
                conn.commit();
                conn.close();
                addServerStats(list.getHighWaterMark(), t2.getTime()-t1.getTime());                
                return INoSQLInterface.OK;
            } else {
                JDebug.StackTrace(Level.SEVERE, "no connection!");
                return INoSQLInterface.ERR_REPEATABLE;
            }            
        } catch (Exception ex) {
            JDebug.StackTrace(Level.SEVERE, ex);
            return INoSQLInterface.ERR_REPEATABLE;            
        }
    }        
    
}
