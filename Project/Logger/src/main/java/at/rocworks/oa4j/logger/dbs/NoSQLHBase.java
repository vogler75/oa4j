package at.rocworks.oa4j.logger.dbs;

import at.rocworks.oa4j.base.JDebug;

import at.rocworks.oa4j.var.Bit32Var;
import at.rocworks.oa4j.var.Bit64Var;
import at.rocworks.oa4j.var.DynVar;
import at.rocworks.oa4j.var.TimeVar;
import at.rocworks.oa4j.var.VariableType;

import at.rocworks.oa4j.logger.base.INoSQLInterface;
import at.rocworks.oa4j.logger.base.NoSQLServer;
import at.rocworks.oa4j.logger.base.NoSQLSettings;
import at.rocworks.oa4j.logger.data.base.AlertItem;
import at.rocworks.oa4j.logger.data.base.DataItem;
import at.rocworks.oa4j.logger.data.lists.DataList;
import at.rocworks.oa4j.logger.data.Dp;
import at.rocworks.oa4j.logger.data.DpAttr;
import at.rocworks.oa4j.logger.data.base.EventItem;
import at.rocworks.oa4j.logger.data.base.ValueItem;
import at.rocworks.oa4j.logger.query.DpGetPeriodResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.json.simple.JSONArray;

/**
 *
 * @author vogler
 * 
 * guava.16.0.1.jar must be used!
 * 
 * hbase shell
 * hbase(main):001:0> create 'eventhistory', 't'
 * 
 */
public class NoSQLHBase extends NoSQLServer {

    private Connection conn;
    private String eventTableName;
    private ArrayList<Table> eventTables = new ArrayList<>();
        
    byte[] colFamily = Bytes.toBytes("t");

    public static NoSQLServer createServer(NoSQLSettings srvcfg, String srvprefix) {
        String hosts = srvcfg.getStringProperty(srvprefix, "hosts", "localhost");        
        int port = srvcfg.getIntProperty(srvprefix, "port", 2181);
        String events = srvcfg.getStringProperty(srvprefix, "events", "eventhistory");
        JDebug.out.log(Level.CONFIG, "{0}.type: {1} \n{2} \nhosts={3} port={4}\nevents={5}",
                new Object[]{srvprefix, NoSQLHBase.class.getName(), srvcfg, hosts, port, events});
        return new NoSQLHBase(srvcfg, hosts, port, events);
    }    
    
    public NoSQLHBase(NoSQLSettings settings, String hosts, int port, String events) {
        super(settings);
        try {            
            initStorage(hosts, port, events, settings.getThreads());
        } catch (Exception ex) {
            JDebug.StackTrace(Level.SEVERE, ex);
        }
    }

    private void initStorage(String hosts, int port, String eventTableName, int threads) throws IOException {
        Configuration config = HBaseConfiguration.create();
        config.set("hbase.zookeeper.property.clientPort", String.valueOf(port));
        config.set("hbase.zookeeper.quorum", hosts);
        conn = ConnectionFactory.createConnection(config);
        
        this.eventTableName=eventTableName;        
        for (int i=0; i<threads; i++) 
            eventTables.add(conn.getTable(TableName.valueOf(eventTableName)));
    }
    
    private byte[] getKey(Dp dp, long ms) {
        return Bytes.toBytes(dp.getSysDpEl()+"@"+ms);
    }
    
    private Date getTS(byte[] key) {
        String s = new String(key);
        String a[] = s.split(Pattern.quote("@"));
        return new Date(Long.parseLong(a[1]));
    }

    @Override
    public boolean dpGetPeriod(Date t1, Date t2, Dp dp, Set<String> configs, DpGetPeriodResult result) {
        JDebug.out.log(Level.FINE, "dpGetPeriod: {0}-{1} {2} {3}", new Object[]{t1, t2, dp.toString(), configs.toString()});
        Date mt1 = new Date(); // Metric Time Start
        try {
            Table table = conn.getTable(TableName.valueOf(eventTableName));
            if ( table != null )
            {                
                // select columns                
                ArrayList<Dp> dps = createDpConfigAttrList(dp, configs);                 
                if (dps.isEmpty()) {
                    JDebug.out.warning("dpGetPeriod without any valid config.");
                    return false;
                }              
                
                // query data
                int records=0;
                Scan scan = new Scan();
                scan.setStartRow(getKey(dp, t1.getTime()));
                scan.setStopRow(getKey(dp, t2.getTime()));
                scan.addFamily(colFamily);
                ResultScanner rs = table.getScanner(scan);
                                
                Date ts;
                Cell cell;
                for (Result row : rs) {
                    records++;   
                    
                    if ( records > QUERY_MAX_RECORDS ) 
                        break;

                    byte[] key = row.getRow();                   
                    ts=getTS(key);
                    
                    for ( int i=0; i<dps.size(); i++) {                            
                        switch (dps.get(i).getAttribute()) {
                            case Value:
                                // value_number
                                
                                cell = row.getColumnLatestCell(colFamily, "number".getBytes());
                                if ( cell != null ) {
                                    double val = Bytes.toDouble(CellUtil.cloneValue(cell));
                                    result.addValue(dps.get(i), ts, val);
                                    break;
                                }
                                // value_string
                                cell = row.getColumnLatestCell(colFamily, "string".getBytes());
                                if ( cell != null ) {
                                    String val = new String(CellUtil.cloneValue(cell));
                                    result.addValue(dps.get(i), ts, val);
                                    break;
                                }
                                // value_timestamp
                                cell = row.getColumnLatestCell(colFamily, "time".getBytes());
                                if ( cell != null ) {
                                    Long val = Bytes.toLong(CellUtil.cloneValue(cell));
                                    result.addValue(dps.get(i), ts, val);
                                    break;
                                }
                                
                                break;
                            case Stime:
                                result.addVariable(dps.get(i), ts, new TimeVar(ts));
                                break;
                            case Status:
                                cell = row.getColumnLatestCell(colFamily, "status".getBytes());
                                if ( cell != null ) {
                                    Long val = Bytes.toLong(CellUtil.cloneValue(cell));
                                    result.addVariable(dps.get(i), ts, new Bit32Var(val));
                                }
                                break;                                
                            case Status64:
                                cell = row.getColumnLatestCell(colFamily, "status".getBytes());
                                if ( cell != null ) {
                                    Long val = Bytes.toLong(CellUtil.cloneValue(cell));
                                    result.addVariable(dps.get(i), ts, new Bit64Var(val));
                                }
                                break;
                            case Manager:
                                cell = row.getColumnLatestCell(colFamily, "manager".getBytes());
                                if ( cell != null ) {
                                    int val = Bytes.toInt(CellUtil.cloneValue(cell));
                                    result.addValue(dps.get(i), ts, val);
                                }
                                break;
                            case User:
                                cell = row.getColumnLatestCell(colFamily, "user".getBytes());
                                if ( cell != null ) {                                    
                                    int val = Bytes.toInt(CellUtil.cloneValue(cell));
                                    result.addValue(dps.get(i), ts, val);
                                }
                                break;                                
                        }
                    }                            
                }
                
                Date mt2 = new Date(); // Metric Time Start
                Long mtd = mt2.getTime()-mt1.getTime(); // Metric Time Duration
                JDebug.out.log(Level.FINE, "dpGetPeriod: {0} records {1} seconds {2}v/s", new Object[]{records, mtd/1000.0, records/(mtd/1000.0)});
                return true;                
            } else {
                JDebug.StackTrace(Level.SEVERE, "no connection!");
            }            
        } catch (Exception ex) {
            JDebug.StackTrace(Level.SEVERE, ex);
        }
        return false;        
    }

    public int storeData(DataList list) {
        boolean ev = false;
        boolean al = false;

        for (int i = 0; i <= list.getHighWaterMark(); i++) {
            DataItem item = list.getItem(i);
            if (item != null) {
                if (item instanceof EventItem) {
                    ev = true;
                } else if (item instanceof AlertItem) {
                    al = true;
                }
            }
        }

        int ret;
        if ((!ev || (ret = storeDataEvents(list)) == INoSQLInterface.OK)
                && (!al || (ret = storeDataAlerts(list)) == INoSQLInterface.OK)) {
            return INoSQLInterface.OK;
        } else {
            return ret;
        }
    }    

    private int storeDataEvents(DataList events) {
        Date t1 = new Date();
                      
        DataItem item;
        EventItem event;
        ValueItem value;

        List<Put> putList = new LinkedList<>();
        for (int i = 0; i <= events.getHighWaterMark() && (item = events.getItem(i)) != null; i++) {
            if (!(item instanceof EventItem)) {
                continue;
            }
            event = (EventItem) item;

            Put put = new Put(getKey(item.getDp(), item.getTimeMS()));

            // value
            value = event.getValue();
           
            // value_number
            Double dval = value.getDouble();
            if (dval != null) {
                put.addColumn(colFamily, Bytes.toBytes("number"), Bytes.toBytes(dval));
            }

            // value_string                    
            if (value.getString() != null) {
                put.addColumn(colFamily, Bytes.toBytes("string"), value.getString().getBytes());
            }

            // value_timestamp
            if (value.getTime() != null) {
                put.addColumn(colFamily, Bytes.toBytes("time"), Bytes.toBytes(value.getTimeMS()));
            }
            
            // dynvar
            if ( value.getVariableType() == VariableType.DynVar ) {
                if ( value.getValueObject() instanceof DynVar ) {
                    DynVar dyn = (DynVar)value.getValueObject();
                    JSONArray arr = new JSONArray();
                    dyn.asList().forEach((row)->arr.add(row.getValueObject())); 
                    put.addColumn(colFamily, Bytes.toBytes("array"), arr.toJSONString().getBytes());                }
            }                    
            
            if (event.hasAttributes()) {
                put.addColumn(colFamily, Bytes.toBytes("status"), Bytes.toBytes(event.getStatus()));
                put.addColumn(colFamily, Bytes.toBytes("manager"), Bytes.toBytes(event.getManager()));
                put.addColumn(colFamily, Bytes.toBytes("user"), Bytes.toBytes(event.getUser()));                
            }
            
            putList.add(put);
        }
                

        try {
            eventTables.get(this.getWorker().getThreadNr()).put(putList);
            Date t2 = new Date();
            addServerStats(events.getHighWaterMark(), t2.getTime()-t1.getTime());            
        } catch (IOException ex) {
            JDebug.StackTrace(Level.SEVERE, ex);
            return INoSQLInterface.ERR_REPEATABLE;
        }   
       
        return INoSQLInterface.OK;
    }

    private int storeDataAlerts(DataList alerts) {
        return INoSQLInterface.OK;
    }
}
