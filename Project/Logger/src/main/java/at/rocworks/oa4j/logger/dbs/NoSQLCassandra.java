/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.dbs;

import at.rocworks.oa4j.logger.base.INoSQLInterface;
import at.rocworks.oa4j.logger.base.NoSQLServer;
import at.rocworks.oa4j.logger.base.NoSQLSettings;
import at.rocworks.oa4j.logger.data.Dp;
import at.rocworks.oa4j.logger.data.base.DataItem;
import at.rocworks.oa4j.logger.data.base.EventItem;
import at.rocworks.oa4j.logger.data.base.ValueItem;
import at.rocworks.oa4j.logger.data.lists.DataList;
import at.rocworks.oa4j.logger.query.DpGetPeriodResult;
import at.rocworks.oa4j.base.JDebug;
import at.rocworks.oa4j.var.Bit32Var;
import at.rocworks.oa4j.var.Bit64Var;
import at.rocworks.oa4j.var.TimeVar;
import com.datastax.driver.core.*;
import com.datastax.driver.core.Cluster.Builder;

import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;

/**
 *
 * @author vogler
 */

public class NoSQLCassandra extends NoSQLServer {
     
    /*
    drop keyspace pvss;
    nodetool clearsnapshot all
    create keyspace pvss with replication = {'class':'SimpleStrategy', 'replication_factor':1};
    use pvss;
    CREATE TABLE eventhistory(
      tag                 VARCHAR,
      ts                  TIMESTAMP,
      value_number        DECIMAL,
      value_string        VARCHAR,
      value_timestamp     TIMESTAMP,  
      status              BIGINT,
      manager             INT,
      user_               INT,
      PRIMARY KEY (tag, ts));
    */
    
    
    
    private final List<String>hosts;
    private final String keystore;
    private final String archive;
    private final int maxFutures;
    
    private Cluster cluster;
    
    private final String sqlInsert;
    private final String sqlSelect;  
    
    private PreparedStatement stmtInsert;
    private PreparedStatement stmtSelect;    

    public static NoSQLServer createServer(NoSQLSettings srvcfg, String srvprefix) {
        String hosts = srvcfg.getStringProperty(srvprefix, "hosts", "localhost");
        String keystore = srvcfg.getStringProperty(srvprefix, "keystore", "pvss");
        String grp = srvcfg.getStringProperty(srvprefix, "archive", "EVENT");
        int futures = srvcfg.getIntProperty(srvprefix, "futures", 0);
        JDebug.out.log(Level.CONFIG, "{0}.type: {1} futures: {2}\n{3}\nhosts: {4}\nkeystore: {5}",
                new Object[]{srvprefix, NoSQLCassandra.class.getName(), futures, srvcfg, hosts, keystore});
        return new NoSQLCassandra(srvcfg, hosts, keystore, grp, futures);
    }    
    
    public NoSQLCassandra(NoSQLSettings settings, String hosts, String keystore, String archive, int futures) {
        super(settings);
        
        this.hosts = Arrays.asList(hosts.split(","));
        this.keystore = keystore;
        this.archive = archive;
        this.maxFutures = futures;
                
        // default insert
        this.sqlInsert 
                = "INSERT INTO " + archive + "HISTORY "
                + "(tag, ts, value_number, value_string, value_timestamp, status, manager, user_)"
                + "VALUES (?,?,?,?,?,?,?,?)";  
        
        // default select
        this.sqlSelect
                = "SELECT "
                + "ts AS TS, "
                + "value_number AS VN, "
                + "value_string AS VS, "
                + "value_timestamp AS VT, "
                + "status AS S, "
                + "manager AS M, "
                + "user_ AS U "
                + "FROM " + archive +"HISTORY "
                +" WHERE tag=? AND ts>=? AND ts<=?";          
        
        initStorage();
    }        
    
    private void initStorage() {
        JDebug.out.info("cassandra init storage...");
        
        PoolingOptions poolingOptions = new PoolingOptions();
        poolingOptions
            .setCoreConnectionsPerHost(HostDistance.LOCAL, settings.getThreads())
            .setMaxConnectionsPerHost(HostDistance.LOCAL, settings.getThreads()*2);
        
        final Builder builder = Cluster.builder();        
        hosts.forEach((String host)->builder.addContactPoint(host));
        builder.withPoolingOptions(poolingOptions);
        cluster = builder.build();
        
        JDebug.out.info("cassandra get connection...");

        // open at least one connection to prevent delay on writing/reading
        try (Session conn = cluster.connect(keystore)) {
            stmtInsert = conn.prepare(this.sqlInsert);
            stmtSelect = conn.prepare(this.sqlSelect);
        }                    
        
        JDebug.out.info("cassandra init storage...done");
    }         
    
    protected String getTagOfDp(Dp dp) {
        return dp.getFQN();
    }            

    public int storeData(DataList events) {
        Date t1 = new Date();
        
        try {
            Session conn = cluster.connect(keystore);
            if ( conn != null )
            {
                int i;
                DataItem item;
                EventItem event;
                Object tag;
                                
                //BatchStatement batch = new BatchStatement(BatchStatement.Type.UNLOGGED);                    
                List<ResultSetFuture> futures = new LinkedList<>();
                
                for (i = 0; i < events.size() && (item = events.getItem(i)) != null; i++) {
                    if ( !(item instanceof EventItem) ) continue;                    
                    event=(EventItem)item;
                    
                    BoundStatement bound = new BoundStatement(stmtInsert);            
                    bound.setConsistencyLevel(ConsistencyLevel.ANY);

                    ValueItem val = event.getValue();
                    
                    tag = getTagOfDp(event.getDp());                    
                    if (tag == null) continue;
                    
                    // storing id's or element-names?
                    if ( tag instanceof Long )
                        bound.setLong(0, (Long)tag);
                    else if ( tag instanceof String )
                        bound.setString(0, (String)tag);
                    
                    bound.setTimestamp(1, event.getDate());
                                        
                    Double dval = val.getDouble();
                    if (dval != null) {
                        bound.setDecimal(2, BigDecimal.valueOf(dval));
                    } else {
                        //bound.setToNull(2);
                        bound.unset(2);
                    }
                    
                    // value_string    
                    if ( val.getString() != null )
                        bound.setString(3, val.getString());
                    else
                        bound.unset(3);
                    
                    // value_timestamp
                    if ( val.getTime() != null ) 
                        bound.setTimestamp(4, val.getTime());
                    else {
                        //bound.setToNull(4);
                        bound.unset(4);
                    }
                    
                    // status, manager, user
                    if ( event.hasAttributes() ) {
                        bound.setLong(5, event.getStatus());
                        bound.setInt(6, event.getManager());
                        bound.setInt(7, event.getUser());
                    } else {
//                        bound.setToNull(5);
//                        bound.setToNull(6);
//                        bound.setToNull(7);
                        bound.unset(5);
                        bound.unset(6);
                        bound.unset(7);
                    }
                    
                    // batch insert
                    //batch.add(bound); 
                    
                    // future insert
                    futures.add(conn.executeAsync(bound));
                    
                    // future insert - wait until all futures are complete
                    if ( this.maxFutures>0 && futures.size()>this.maxFutures ) {
                        while ( futures.size() > this.maxFutures/2 ) {
                            futures.get(0).getUninterruptibly();
                            // TODO check result?
                            futures.remove(0);
                        }                    
                    }
                    
                    // single insert
                    //conn.execute(bound);

                    //JDebug.out.log(Level.FINE, "{0}:{1}/{2} [{3}]", new Object[] {i, element_id.toString(), ts.toString(), item.toString()});
                }
                
                // future insert - wait until all futures are complete
                while (futures.size()>0) {
                    futures.get(0).getUninterruptibly();
                    // TODO check result?
                    futures.remove(0);
                }
                
//                // batch insert  
//                try {
//                    conn.execute(batch);
//                    batch.clear();
//                } catch (Exception ex) {
//                    JDebug.StackTrace(Level.SEVERE, ex);
//                }

                conn.close();
                Date t2 = new Date();                
                addServerStats(events.getHighWaterMark(), t2.getTime()-t1.getTime());                
                return INoSQLInterface.OK;                
            } else {
                JDebug.StackTrace(Level.SEVERE, "no connection!");
                return INoSQLInterface.ERR_REPEATABLE;
            }            
        } catch (Exception ex) {
            JDebug.StackTrace(Level.SEVERE, ex);
            return INoSQLInterface.ERR_UNRECOVERABLE;            
        }
    }
    
    @Override
    public boolean dpGetPeriod(Date t1, Date t2, Dp dp, Set<String> configs, DpGetPeriodResult result) {
        JDebug.out.log(Level.FINE, "dpGetPeriod: {0}-{1} {2} {3}", new Object[]{t1, t2, dp.toString(), configs.toString()});
        try {
            Session conn = cluster.connect(keystore);
            if ( conn != null )
            {                
                // select columns                
                ArrayList<Dp> dps = createDpConfigAttrList(dp, configs);                 
                if (dps.isEmpty()) {
                    JDebug.out.warning("dpGetPeriod without any valid config.");
                    return false;
                }              
                
                // datapoint element_id
                Object element_id = getTagOfDp(dp);                    
                if (element_id == null) {
                    JDebug.out.log(Level.SEVERE, "dpGetPeriod with invalid datapoint {0}", new Object[] {dp.toString()});
                    return false;
                }                                                            
                
                // query data
                int records=0;
                BoundStatement stmt = new BoundStatement(stmtSelect);
                
                // element_id
                if ( element_id instanceof Long )
                    stmt.setLong(0, (Long)element_id);
                else if ( element_id instanceof String ) {
                    stmt.setString(0, (String)element_id);
                    //JDebug.out.info("element_id="+(String)element_id);
                }

                // timerange
//                stmt.setDate(1, LocalDate.fromMillisSinceEpoch(t1.getTime()));
//                stmt.setDate(2, LocalDate.fromMillisSinceEpoch(t2.getTime()));
                stmt.setTimestamp(1, t1);
                stmt.setTimestamp(2, t2);

                // execute   
                ResultSet rs = conn.execute(stmt);
                //ResultSetMetaData md = rs.getMetaData();

                Date ts;
                Object value;
                for (Row row : rs) {
                    records++;   
                    
                    if ( records > QUERY_MAX_RECORDS ) 
                        break;

//                    ts=new Date(row.getDate("TS").getMillisSinceEpoch());
                    ts= row.getTimestamp("TS");
                    for ( int i=0; i<dps.size(); i++) {                            
                        switch (dps.get(i).getAttribute()) {
                            case Value:
                                // value_number
                                value = row.getObject(1);
                                if ( value != null ) {
                                    result.addValue(dps.get(i), ts, value);
                                    break;
                                }
                                // value_string
                                value = row.getObject(2);
                                if ( value != null ) {
                                    result.addValue(dps.get(i), ts, value);
                                    break;
                                }
                                // value_timestamp
                                value = row.getObject(3);
                                if ( value != null ) {
                                    result.addValue(dps.get(i), ts, value);
                                    break;
                                }
                                
                                break;
                            case Stime:
                                result.addVariable(dps.get(i), ts, new TimeVar(ts));
                                break;
                            case Status:
                                value = row.getObject(4);
                                result.addVariable(dps.get(i), ts, new Bit32Var(value));
                                break;                                
                            case Status64:
                                value = row.getObject(4);
                                result.addVariable(dps.get(i), ts, new Bit64Var(value));
                                break;
                            case Manager:
                                value = row.getObject(5);
                                result.addValue(dps.get(i), ts, value);
                                break;
                            case User:
                                value = row.getObject(6);
                                result.addValue(dps.get(i), ts, value);
                                break;                                
                        }
                    }                            
                    //Thread.sleep(1);   
                }
                //Thread.sleep(1000);
                
                //JDebug.out.log(Level.FINE, "dpGetPeriod: {0} records", records);
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
