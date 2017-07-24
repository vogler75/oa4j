/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.dbs;

import at.rocworks.oa4j.base.JDebug;

import at.rocworks.oa4j.var.Bit32Var;
import at.rocworks.oa4j.var.Bit64Var;
import at.rocworks.oa4j.var.DynVar;
import at.rocworks.oa4j.var.TimeVar;
import at.rocworks.oa4j.var.Variable;
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

import com.mongodb.Block;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.WriteModel;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 *
 * @author vogler
 */
public class NoSQLMongoDB extends NoSQLServer {
    /*
    use pvss
    db.createCollection("events");
    db.events.createIndex({ts:1});
    db.events.createIndex({tag:1,ns:1},{unique: true});
    db.events.createIndex({tag:1,ts:1},{unique: false});
    */
    
    private MongoClient client;
    private MongoDatabase database;
    private MongoCollection evcoll;
    
    private boolean documents;
    
    HashMap<DpAttr, String> attrMap = new HashMap<DpAttr, String>(){{
         put(DpAttr.Value, "value");            
         put(DpAttr.Status, "status"); 
         put(DpAttr.Status64, "status");          
         put(DpAttr.Manager, "manager");          
         put(DpAttr.User, "user"); 
     }};           
    
    public static NoSQLServer createServer(NoSQLSettings srvcfg, String srvprefix) {
        String url = srvcfg.getStringProperty(srvprefix, "url", "mongodb://localhost/");
        String db = srvcfg.getStringProperty(srvprefix, "db", "pvss");
        boolean docs = srvcfg.getBoolProperty(srvprefix, "documents", false);
        JDebug.out.log(Level.CONFIG, "{0}.type: {1} \n{2}\nurl: {3}\ndb: {4}\ndocuments: {5}",
                new Object[]{srvprefix, NoSQLMongoDB.class.getName(), srvcfg, url, db, docs});
        return new NoSQLMongoDB(srvcfg, url, db, docs);
    }    

    public NoSQLMongoDB(NoSQLSettings settings, String url, String db, boolean documents) {
        super(settings);
        this.documents=documents;        
        initStorage(url, db);
    }
    
    private void initStorage(String url, String db) {
        JDebug.out.info("mongodb init storage...");
        MongoClientURI uri = new MongoClientURI(url);
        client = new MongoClient(uri);
        database = client.getDatabase(db);
        evcoll = database.getCollection("events");
    }
    
    protected String getTagOfDp(Dp dp) {
        return dp.getFQN();
    }                  

    @Override
    public int storeData(DataList list) {
        boolean ev=false;
        boolean al=false;
        
        for ( int i=0; i<=list.getHighWaterMark(); i++ ) {
            DataItem item = list.getItem(i);
            if ( item != null ) {
                if ( item instanceof EventItem )
                    ev=true;
                else if ( item instanceof AlertItem )
                    al=true;
            }
        }
        
        int ret;
        if ( (!ev || (ret=storeDataEvents(list))==INoSQLInterface.OK) &&
             (!al || (ret=storeDataAlerts(list))==INoSQLInterface.OK) )
            return INoSQLInterface.OK;
        else
            return ret;       
    }
    
    private int storeDataEvents(DataList events) {
        Date t1 = new Date();        
        
        DataItem item;
        EventItem event;  
        ValueItem value;
        
        ArrayList<WriteModel<Document>> list = new ArrayList<>();
        
        try {
            for (int i = 0; i <= events.getHighWaterMark()&& (item = events.getItem(i)) != null; i++) {           
                if ( !(item instanceof EventItem) ) continue;                    
                event=(EventItem)item;  

                //JDebug.out.info(getTagOfDp(event.getDp()));
                Document obj = new Document("type", "event")
                        .append("tag", getTagOfDp(event.getDp()))
                        .append("ns", event.getTimeNS()) // ISODate does not support nanoseconds                        
                        .append("ts", new Date(event.getTimeMS())) // ISODate
                        ;
//                        .append("sys", event.getDp().getSystem())
//                        .append("dp", event.getDp().getDp())
//                        .append("el", event.getDp().getElement())
//                        .append("dpel", event.getDp().getDpEl());

                // value
                value = event.getValue();                
                Map<String, Object> map = new HashMap<>();

                // value_number
                Double dval = value.getDouble();
                if (dval != null) 
                    map.put("number", dval);

                // value_string                    
                if (value.getString() != null) 
                    map.put("text", value.getString());

                // value_timestamp
                if ( value.getTime() != null ) 
                    map.put("time", value.getTime());

                // dynvar
                if ( value.getVariableType() == VariableType.DynVar ) {
                    if ( value.getValueObject() instanceof DynVar ) {
                        DynVar dyn = (DynVar)value.getValueObject();
                        JSONArray arr = new JSONArray();
                        dyn.asList().forEach((row)->arr.add(row.getValueObject()));
                        map.put("array", arr);
                    }
                }                     
                
                obj.append("value", map);

                // attributes
                if ( event.hasAttributes() ) {
                    obj.append("status", event.getStatus());
                    obj.append("manager", event.getManager());
                    obj.append("user", event.getUser());
                }
                
                // check if text is a json document
                if ( documents && value.getString()!=null ) {
                    try {
                        Object doc = (new JSONParser()).parse(value.getString());
                        obj.append("document", doc);
                    } catch (ParseException ex) {
                        // no json text
                    }
                }                

                list.add(new InsertOneModel<>(obj));
                //JDebug.out.info(obj.string());
            }

            evcoll.bulkWrite(list, new BulkWriteOptions().ordered(false));            
            Date t2 = new Date();
            addServerStats(events.getHighWaterMark(), t2.getTime()-t1.getTime());               
            return INoSQLInterface.OK;        
        } catch ( MongoBulkWriteException ex ) {
            // TODO mongodb bulk exception
            //JDebug.out.log(Level.SEVERE, "Bulk exception {0} on {1} records.", new Object[]{ex.getCode(), ex.getWriteErrors().size()});
            //JDebug.StackTrace(Level.SEVERE, ex);
            return INoSQLInterface.ERR_UNRECOVERABLE;
        } catch ( Exception ex ) {
            JDebug.StackTrace(Level.SEVERE, ex);
            return INoSQLInterface.ERR_REPEATABLE;            
        }
    }    
    
    private int storeDataAlerts(DataList alerts) { 
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.        
    }   
    
    @Override
    public boolean dpGetPeriod(Date t1, Date t2, Dp dp, Set<String> configs, DpGetPeriodResult result) {
        // db.events.find({tag: "System1:Test_1_1.Value", ts: {$gt: ISODate("2016-07-28T09:00:00.000Z"), $lt: ISODate("2016-07-28T10:00:00.000Z")}}, {_id:0, tag:1, ts:1});
        JDebug.out.log(Level.INFO, "dpGetPeriod {0}-{1} dp={2} configs={3}", new Object[]{t1, t2, dp, configs.toString()});

        final SimpleDateFormat fmt = new SimpleDateFormat(TimeVar.FMT_DATE_JS_MS);
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));        
              
        // columns
        final ArrayList<Dp> dps = createDpConfigAttrList(dp, configs);                               
        if (dps.isEmpty()) {
            JDebug.out.warning("dpGetPeriod without any valid config.");
            return false;
        }                      
        Document columns = new Document();
        columns.append("_id", 0);
        columns.append("ts", 1);
        dps.forEach((Dp x)->{
            String c=attrMap.get(x.getAttribute());
            if ( c!=null ) columns.append(c, 1);
        });        
        
        // filter
        Document query = new Document();
        query.append("tag", getTagOfDp(dp));
        query.append("ts", new Document("$gte", t1).append("$lte", t2));        
        
        // query
        FindIterable<Document> find  = evcoll.find(query);
        find.projection(columns);                        
        find.forEach((Block<Document>) document -> {
            // { "ts" : { "$date" : 1469696660635 }, "value" : { "number" : 3.0 }, "status" : { "$numberLong" : "-9007199254738370303" }, "user" : 0 }
            //JDebug.out.info(document.toJson());
            Date ts = document.getDate("ts");
            Object value;
            for ( int i=0; i<dps.size(); i++) {
                try {
                    final Dp attr=dps.get(i);                    
                    switch (attr.getAttribute()) {
                        case Value:
                            // value_number
                            value = document.get("value");                        
                            if (value instanceof Document) {
                                Document dval = (Document) value; 
                                dval.keySet().forEach(type -> result.addValue(attr, ts, dval.get(type)));
                            } 
                            break;                                   
                        case Status:
                            value = document.get("status");                         
                            result.addVariable(attr, ts, new Bit32Var(value)); 
                            break;
                            
                        case Status64:
                            value = document.get("status");                         
                            result.addVariable(attr, ts, new Bit64Var(value)); 
                            break;
                        case Manager:
                            value = document.get("manager");
                            result.addVariable(attr, ts, Variable.newVariable(value)); 
                            break;
                        case User:                                    
                            value = document.get("user");
                            result.addVariable(attr, ts, Variable.newVariable(value)); 
                            break;
                        case Stime:
                            value = ts;
                            result.addVariable(attr, ts, Variable.newVariable(value)); 
                            break;                                    
                        default: 
                            JDebug.out.log(Level.SEVERE, "unhandeled config {0}", attr.getAttribute());
                    }
                } catch ( Exception ex ) {
                    JDebug.StackTrace(Level.SEVERE, ex);
                }
            }                                                                            
        });        

        return true;
    }    
}
