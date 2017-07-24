/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.dbs;

import at.rocworks.oa4j.base.JDebug;

import at.rocworks.oa4j.logger.data.Metrics;
import at.rocworks.oa4j.var.Bit32Var;
import at.rocworks.oa4j.var.Bit64Var;
import at.rocworks.oa4j.var.DynVar;
import at.rocworks.oa4j.var.TimeVar;
import at.rocworks.oa4j.var.Variable;
import at.rocworks.oa4j.var.VariableType;

import at.rocworks.oa4j.logger.base.INoSQLInterface;
import at.rocworks.oa4j.logger.base.NoSQLServer;
import at.rocworks.oa4j.logger.base.NoSQLSettings;
import at.rocworks.oa4j.logger.data.base.DataItem;
import at.rocworks.oa4j.logger.data.lists.DataList;
import at.rocworks.oa4j.logger.data.Dp;
import at.rocworks.oa4j.logger.data.DpAttr;
import at.rocworks.oa4j.logger.data.base.EventItem;
import at.rocworks.oa4j.logger.data.base.ValueItem;
import at.rocworks.oa4j.logger.query.DpGetPeriodResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import org.apache.commons.dbcp2.BasicDataSource;
import java.sql.BatchUpdateException;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author vogler
 */
public abstract class NoSQLJDBC extends NoSQLServer {
       
    protected BasicDataSource dataSourceWrite;
    protected BasicDataSource dataSourceQuery;
        
    private final String url; 
    
    private final String driver;
    private final String username;
    private final String password;
    
    protected final String archive;    
    
    protected String sqlInsertStmt;
    protected String sqlSelectStmt;
    
    Calendar cal;
    
    HashMap<DpAttr, String> attrMap = new HashMap<DpAttr, String>(){{
         put(DpAttr.Value, "value_number AS VN,value_string AS VS,value_timestamp AS VT");            
         put(DpAttr.Status, "status AS ST"); 
         put(DpAttr.Status64, "status AS ST64");          
         put(DpAttr.Manager, "manager AS M");          
         put(DpAttr.User, "user_ AS U"); 
     }};        
    
    public NoSQLJDBC(NoSQLSettings settings, String driver, String url, String username, String password, String archive) {
        super(settings);
        this.driver = driver;
        this.url = url;      

        this.username = username;
        this.password = password;
        this.archive = archive;
        
        this.cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        
        // default insert
        this.sqlInsertStmt 
                = "INSERT INTO " + archive + "HISTORY "
                + "(tag, ts, value_number, value_string, value_timestamp, status, manager, user_)"
                + "VALUES (?,?,?,?,?,?,?,?)";  
        
        // default select
        this.sqlSelectStmt
                = "SELECT %s FROM " + archive +"HISTORY "
                +" WHERE tag=? AND ts>=? AND ts<=?";  
        
        try {
            Class.forName(driver);
            dataSourceWrite = new BasicDataSource();
            initStorage(dataSourceWrite, this.settings.getThreads());
            dataSourceQuery = new BasicDataSource();
            initStorage(dataSourceQuery, this.settings.getThreads()*this.settings.getThreads());
        } catch (ClassNotFoundException ex) {
            JDebug.StackTrace(Level.SEVERE, ex);
        }            
        

    }    

    abstract protected void afterInsert(Connection conn) throws SQLException;
    
    protected Object getTagOfDp(Dp dp) {
        return dp.getFQN();
    }          
    
    private void initStorage(BasicDataSource dataSource, int threads) {
        JDebug.out.info("jdbc init storage...");
                   
        dataSource.setDriverClassName(driver);        
        dataSource.setUrl(this.url);
        dataSource.setMaxTotal(threads);
        dataSource.setUsername(username);
        dataSource.setPassword(password);        
        
        JDebug.out.info("jdbc get connection...");

        // open at least one connection to prevent delay on writing/reading
        Connection conn;
        try {
            conn = dataSource.getConnection();
            conn.close();            
        } catch (SQLException ex) {
            JDebug.StackTrace(Level.SEVERE, ex);
        }
        
        JDebug.out.info("jdbc init storage...done");
    }        
     
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
                    
                    //JDebug.out.log(Level.FINE, "{0}:{1}/{2} [{3}]", new Object[] {i, element_id.toString(), ts.toString(), item.toString()});
                    
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
//                        JDebug.out.log(Level.INFO, "{0}", item.toJSONObject());
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
    
    @Override
    public Metrics getStats() {
        Metrics stats = super.getStats();
        if ( this.dataSourceWrite != null ) {
            stats.put(new Metrics("jdbc")
                    .put("actv", Double.valueOf(this.dataSourceWrite.getNumActive()))
                    .put("idle", Double.valueOf(this.dataSourceWrite.getNumIdle()))
            );
        }
        return stats;
    }    
    
    @Override
    public boolean dpGetPeriod(Date t1, Date t2, Dp dp, Set<String> configs, DpGetPeriodResult result) {
        JDebug.out.log(Level.INFO, "dpGetPeriod: {0}-{1} {2} {3}", new Object[]{t1, t2, dp.toString(), configs.toString()});
        try {
            Connection conn = dataSourceQuery.getConnection();            
            if ( conn != null )
            {                                
                // select columns                
                ArrayList<Dp> dps = createDpConfigAttrList(dp, configs);                               
                if (dps.isEmpty()) {
                    JDebug.out.warning("dpGetPeriod without any valid config.");
                    return false;
                }        

                StringBuilder columns = new StringBuilder();
                dps.forEach((Dp x)->{
                    String c=attrMap.get(x.getAttribute());
                    if ( c!=null ) columns.append(c).append(",");
                });  
                
                // add the timestamp
                columns.append("ts AS TS");
                
                // datapoint element_id
                Object tag = this.getTagOfDp(dp);                    
                if (tag == null) {
                    JDebug.out.log(Level.SEVERE, "dpGetPeriod with invalid datapoint {0}", new Object[] {dp.toString()});
                    return false;
                }                                
                
                // build sql statement
                String sql = String.format(this.sqlSelectStmt, columns);                
                
                // query data
                int records=0;
                JDebug.out.log(Level.FINE, "dpGetPeriod SQL={0}", sql);
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {                    
                    // tag
                    if ( tag instanceof Long )
                        stmt.setLong(1, (Long)tag);
                    else if ( tag instanceof String ) {
                        stmt.setString(1, (String)tag);
                    }
                    
                    // timerange
                    stmt.setTimestamp(2, new java.sql.Timestamp(t1.getTime()), cal);
                    stmt.setTimestamp(3, new java.sql.Timestamp(t2.getTime()), cal);
                    
                    // execute
                    //stmt.setFetchSize(1000);
                    ResultSet rs = stmt.executeQuery();    
                    //ResultSetMetaData md = rs.getMetaData();
                    
                    Date ts;
                    Object value;
                    while (rs.next()) {
                        records++;                        
                        int c=0;                        
                        ts=rs.getTimestamp("TS", cal);
                        for ( int i=0; i<dps.size(); i++) {                            
                            switch (dps.get(i).getAttribute()) {
                                case Value:
                                    // value_number
                                    value = rs.getObject(++c);
                                    if ( value != null )
                                        result.addValue(dps.get(i), ts, value);
                                    // value_string
                                    value = rs.getObject(++c);
                                    if ( value != null )
                                        result.addValue(dps.get(i), ts, value);
                                    // value_timestamp
                                    value = rs.getObject(++c);
                                    if ( value != null )
                                        result.addValue(dps.get(i), ts, value);
                                    break;                                   
                                case Status:
                                    value = rs.getObject(++c);
                                    result.addVariable(dps.get(i), ts, new Bit32Var(value)); 
                                    break;                                    
                                case Status64:
                                    value = rs.getObject(++c);
                                    result.addVariable(dps.get(i), ts, new Bit64Var(value));
                                    break;
                                case Manager:
                                case User:                                    
                                    value = rs.getObject(++c);
                                    result.addValue(dps.get(i), ts, value);
                                    break;
                                case Stime:
                                    value = ts;                                    
                                    result.addValue(dps.get(i), ts, value);
                                    break;                                    
                                default: 
                                    c++;
                                    JDebug.out.log(Level.SEVERE, "unhandeled config {0}", dps.get(i).getAttribute());
                            }
                        }                            
                    }
                }
                JDebug.out.log(Level.FINE, "dpGetPeriod: {0} records", records);
                conn.close();
                return true;                
            } else {
                JDebug.StackTrace(Level.SEVERE, "no connection!");
            }            
        } catch (Exception ex) {
            JDebug.StackTrace(Level.SEVERE, ex);
        }
        return false;
    }    
}
