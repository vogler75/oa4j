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
import at.rocworks.oa4j.logger.data.Dp;
import at.rocworks.oa4j.logger.data.DpAttr;
import at.rocworks.oa4j.logger.data.base.EventItem;
import at.rocworks.oa4j.logger.data.lists.DataList;
import at.rocworks.oa4j.logger.data.base.ValueItem;
import at.rocworks.oa4j.logger.query.DpGetPeriodResult;
import at.rocworks.oa4j.logger.query.DpGetPeriodParameter;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.Inet4Address;
import java.net.InetAddress;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

/**
 *
 * @author vogler
 * 
 * guava.18.0.jar must be used!
 * 
 */

/*

GET _cluster/health

GET _cluster/state

GET _cluster/stats

GET _cluster/settings

GET _nodes/stats
GET _nodes/settings

GET /_aliases


GET /_template/scada-event-template
PUT /_template/scada-event-template
{
  "template": "scada-event-*", 
  "order":    1, 
  "settings": {
    "settings":{ "refresh_interval" : "5s" },   
    "number_of_shards":   3, 
    "number_of_replicas": 0    
  },
  "mappings": {
    "_default_": { 
      "_all": {
        "enabled": true
      }
    },
    "event" : {
            "properties" : {
                "tag" : {"type" : "string"},
                "@timestamp" : {"type" : "date"},
                "type" : {"type" : "string","index":  "not_analyzed" },
                "sys" : {"type" : "string", "index" : "not_analyzed"},                
                "dp" : {"type" : "string", "index" : "not_analyzed"},
                "el" : {"type" : "string", "index" : "not_analyzed"},
                "dpel" : {"type" : "string", "index" : "not_analyzed"},
                "value": {
                    "properties": {
                        "number" : {"type" : "double"},
                        "text" : {"type" : "string"},
                        "time" : {"type" : "date"},
                        "bool" : {"type" : "boolean"}
                    }
                },
                "status" : {"type" : "string"},
                "manager" : {"type" : "integer"},
                "user" : {"type" : "integer"}
             }
        }
  },
  "aliases": {
    "scada-events": {} 
  }
}

GET /_template/scada-alert-template
PUT /_template/scada-alert-template
{
    "template": "scada-alert-*", 
    "order":    1, 
    "settings": {
        "settings":{ "refresh_interval" : "5s" },   
        "number_of_shards":   3, 
        "number_of_replicas": 0          
    },
    "mappings": {
      "_default_": { 
        "_all": {
          "enabled": true
        }
      },
      "alert": {
          "properties" : {
              "tag" : {"type" : "string"},
              "@timestamp" : {"type" : "date"},
              "type" : {"type" : "string","index":  "not_analyzed" },
                "sys" : {"type" : "string", "index" : "not_analyzed"},                
                "dp" : {"type" : "string", "index" : "not_analyzed"},
                "el" : {"type" : "string", "index" : "not_analyzed"},
                "dpel" : {"type" : "string", "index" : "not_analyzed"},              
              "value": {
                   "properties": {
                       "number" : {"type" : "double"},
                       "text" : {"type" : "string"},
                       "time" : {"type" : "date"},
                       "bool" : {"type" : "boolean"}      
                   }
               }
          }    
      }
    },
    "aliases": {
      "scada-alerts": {} 
    }
}


    add a new type to an existing index:
    PUT /pvss/_mapping/alert
    {
        "properties" : {
                "tag" : {"type" : "string","index":  "not_analyzed"},
                "@timestamp" : {"type" : "date"},
                "value": {
                     "properties": {
                         "number" : {"type" : "double"},
                         "text" : {"type" : "string"},
                         "time" : {"type" : "date"},
                         "bool" : {"type" : "boolean"}      
                     }
                 }
            }    
    }      
    
    Insert Data
    ===========
    POST http://localhost:9200/pvss/event
    {
            "tag" : "System1:Test2.Node2",
            "@timestamp" : 1455217127001,
            "value_number" : 57,            
            "status" : "00000000000000000000000000000000",
            "manager" : 0,
            "user" : 0
    }
*/

public class NoSQLElasticsearch extends NoSQLServer {

    private final String home;
    private final String cluster;
    private final String hosts;
    private final String eventIndex; // ~ database
    private final String alertIndex; // ~ database
    private final String eventType = "event"; // ~ event table
    private final String alertType = "alert"; // ~ alert table
    
    private final boolean documents; // store strings as json documents
       
    private Client client;
        
    private final String FMT_DATE = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";    
    
    HashMap<DpAttr, List<String>> attrMap = new HashMap<DpAttr, List<String>>(){{
         put(DpAttr.Value, Arrays.asList("number", "text", "time"));
         put(DpAttr.Status, Arrays.asList("status")); 
         put(DpAttr.Status64, Arrays.asList("status"));          
         put(DpAttr.Manager, Arrays.asList("manager"));          
         put(DpAttr.User, Arrays.asList("user")); 
         put(DpAttr.Stime, Arrays.asList("@timestamp")); 
     }};                        

    public static NoSQLServer createServer(NoSQLSettings srvcfg, String srvprefix) {
        String cluster = srvcfg.getStringProperty(srvprefix, "cluster.name", "elasticsearch");
        String nodes = srvcfg.getStringProperty(srvprefix, "cluster.nodes", "localhost");
        String index = srvcfg.getStringProperty(srvprefix, "index", "pvss");
        boolean docs = srvcfg.getBoolProperty(srvprefix, "documents", false);
        JDebug.out.log(Level.CONFIG, "{0}.type: {1}\n{2}\ncluster: {3}\nnodes: {4}\nindex: {5}\ndocuments: {6}",
                new Object[]{srvprefix, NoSQLElasticsearch.class.getName(), srvcfg, cluster, nodes, index, docs});
        return new NoSQLElasticsearch(srvcfg, "data", cluster, nodes, index, docs);
    }    
    
    public NoSQLElasticsearch(NoSQLSettings settings, String home, String cluster, String hosts, String index, boolean documents) {
        super(settings);
        this.home = home;
        this.cluster = cluster;        
        this.hosts = hosts;
        this.eventIndex = index + "-" + this.eventType;        
        this.alertIndex = index + "-" + this.alertType;
        this.documents = documents;
        initStorage();
    }        
    
    private void initStorage() {    
        JDebug.out.log(Level.INFO, "elastic init storage...{0}", this.hosts);
        
        Settings elastic;
        try {
            elastic = Settings.builder()
                    .put("network.host", Inet4Address.getLocalHost().getHostAddress())
                    .put("path.home", this.home)
                    .put("cluster.name", this.cluster)
//                    .put("client.transport.sniff", true)
//                    .put("discovery.zen.ping.unicast.hosts", this.hosts)
                    .build();
        } catch (UnknownHostException ex) {
            JDebug.StackTrace(Level.SEVERE, ex);
            return;
        }
        
        JDebug.out.info("elastic transport client...");
        
//        Client client = NodeBuilder.nodeBuilder()
//                .settings(elastic)
//                .client(true)
//                .data(false)
//                .node()
//                .client();
        
        TransportClient client = new PreBuiltTransportClient(elastic);
        Arrays.asList(this.hosts.split(",")).forEach((String node)-> {
            try {
                JDebug.out.info("elastic add transport address..."+node);
                client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(node), 9300)); 
            } catch (UnknownHostException ex) {
                JDebug.StackTrace(Level.SEVERE, ex);
            }
        });        
        
        JDebug.out.info("elastic check indices...");

        if ( !client.admin().indices().prepareExists(this.eventIndex).execute().actionGet().isExists() ) {
            JDebug.out.log(Level.SEVERE, "event index {0} does not exist!", this.eventIndex);
        }
        
        if( !client.admin().indices().prepareExists(this.alertIndex).execute().actionGet().isExists() ) {
            JDebug.out.log(Level.SEVERE, "alert index {0} does not exist!", this.alertIndex);
        }                        
        
        this.client = client;     
        JDebug.out.info("elastic init storage...done");
        
    }
    
    protected String getTagOfDp(Dp dp) {
        return dp.getFQN();
    }           
    
    private String getKey(EventItem event) {
        return event.getDp().getSysDpEl()+"/"+event.getTimeNS();
    }
    
    private String getKey(AlertItem alert) {
        return alert.getDp().getSysDpEl()+"/"+alert.getDp().getDetail()+"/"+alert.getTimeNS();
    }    
       
    
    private long getId(Dp dp, long ms) {        
        return ((long)dp.hashCode())*1000000000000000L+ms; // max. date: Fri Sep 27 33658 02:46:39 GMT+0100
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
        
        SimpleDateFormat fmtTimeframe = new SimpleDateFormat("yyyy.MM"); 
        String index=this.eventIndex+"-"+fmtTimeframe.format(new Date());
        
        BulkRequestBuilder bulkRequest = client.prepareBulk();                
        XContentBuilder obj;        

        int count=0;
        for (int i = 0; i <= events.getHighWaterMark()&& (item = events.getItem(i)) != null; i++) {           
            if ( !(item instanceof EventItem) ) continue;            
            event=(EventItem)item;            
            try {                
                obj = jsonBuilder().startObject()
                        .field("type", "event")
                        .field("id", getId(event.getDp(), event.getTimeMS())) 
                        .field("tag", getTagOfDp(event.getDp()))
                        .field("@timestamp", new Date(event.getTimeMS()))
                        .field("sys", event.getDp().getSystem())
                        .field("dp", event.getDp().getDp())
                        .field("el", event.getDp().getElement())
                        .field("dpel", event.getDp().getDpEl());

                // value
                value = event.getValue();                
                Map<String, Object> map = new HashMap<>();
                
                // value_number
                Double dval = value.getDouble();
                if (dval != null) 
                    map.put("number", BigDecimal.valueOf(dval));

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
                
                obj.field("value", map);
                                
                // attributes
                if ( event.hasAttributes() ) {
                    obj.field("status", event.getStatus());
                    obj.field("manager", event.getManager());
                    obj.field("user", event.getUser());
                }
                
                // check if text is a json document
                if ( documents && value.getString()!=null ) {
                    try {
                        Object doc = (new JSONParser()).parse(value.getString());
                        obj.field("document", doc);
                    } catch (ParseException ex) {
                        // no json text
                    }
                }                                
                
                obj.endObject();
                
                //JDebug.out.info(obj.string());
                if (count++==0) index = this.eventIndex+"-"+fmtTimeframe.format(event.getDate());                
                bulkRequest.add(client.prepareIndex(index, this.eventType, getKey(event))
                        .setSource(obj));
                
            } catch (IOException ex) {
                JDebug.StackTrace(Level.SEVERE, ex);
            }
        }
        try {
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();        
            Date t2 = new Date();
            addServerStats(events.getHighWaterMark(), t2.getTime()-t1.getTime());               

            if (bulkResponse.hasFailures()) {
                JDebug.out.severe(bulkResponse.buildFailureMessage());
                return INoSQLInterface.ERR_UNRECOVERABLE;
            } else {
                return INoSQLInterface.OK;
            }
        } catch (ClusterBlockException ex) {
            JDebug.out.severe(ex.getMessage());
            return INoSQLInterface.ERR_REPEATABLE;
        }
    }    
    
    private int storeDataAlerts(DataList alerts) {
        // TODO Update Alert Partner
        Date t1 = new Date();        
        
        DataItem item;
        AlertItem alert;  
        Variable value;
        
        SimpleDateFormat fmtTimeframe = new SimpleDateFormat("yyyy");  
        String index = this.alertIndex+"-"+fmtTimeframe.format(new Date());        
        
        BulkRequestBuilder bulkRequest = client.prepareBulk();        
        XContentBuilder obj;
        
        int count=0;
        for (int i = 0; i <= alerts.getHighWaterMark()&& (item = alerts.getItem(i)) != null; i++) {           
            if ( !(item instanceof AlertItem) ) continue;                    
            alert=(AlertItem)item;            
            try {
                obj = jsonBuilder().startObject()
                        .field("type", "alert")
                        .field("id", getId(alert.getDp(), alert.getTimeMS()))
                        .field("tag", getTagOfDp(alert.getDp()))
                        .field("@timestamp", new Date(alert.getTimeMS()))
                        .field("sys", alert.getDp().getSystem())
                        .field("dp", alert.getDp().getDp())
                        .field("el", alert.getDp().getElement())
                        .field("dpel", alert.getDp().getDpEl());

                // value
                value = alert.getValue();
                Map<String, Object> map = new HashMap<>();               
                switch ( value.isA() ) {
                    case FloatVar: 
                        map.put("number", BigDecimal.valueOf(value.getFloatVar().getValue()));  
                        break;
                    case IntegerVar:
                    case UIntegerVar:
                    case ULongVar:
                        map.put("number", BigDecimal.valueOf(value.getIntegerVar().getValue()));                                                 
                        break;
                    case TextVar:
                        map.put("text", value.getTextVar().getValue());
                        map.put("textx", value.getTextVar().getValue());
                        break;
                    case TimeVar:
                        map.put("time", value.getTimeVar().getValue());
                        break;
                    case BitVar:
                        map.put("bool", value.getBitVar().getValue());
                        break;
                    default:
                        JDebug.out.log(Level.WARNING, "unhandled datatype {0}", value.isA());
                        break;                    
                }                    
                obj.field("value", map);
                
                obj.field("ack_state", alert.getAckState().getValue());
                obj.field("ack_time", alert.getAckTime().getValue());
                obj.field("ack_type", alert.getAckType().getValue());
                obj.field("ack_user", alert.getAckUser().getValue());
                obj.field("ackable", alert.getAckable().getValue());
                obj.field("alert_color", alert.getAlertColor().getValue());
                obj.field("alert_font_style", alert.getAlertFontStyle().getValue());
                obj.field("alert_fore_color", alert.getAlertForeColor().getValue());
                obj.field("archive", alert.getArchive().getValue());
                obj.field("came_time", alert.getCameTime().getValue());
                obj.field("came_time_idx", alert.getCameTimeIdx().getValue());
                obj.field("alert_class", alert.getAlertClass().getValue());
                obj.field("comment", alert.getComment().getValue());
                obj.field("dest", alert.getDest().getValue());
                obj.field("direction", alert.getDirection().getValue());
                obj.field("gone_time", alert.getGoneTime().getValue());
                obj.field("gone_time_idx", alert.getGoneTimeIdx().getValue());
                obj.field("inact_ack", alert.getInactAck().getValue());
                obj.field("panel", alert.getPanel().getValue());
                obj.field("partner", alert.getPartner().getValue());
                obj.field("partner_idx", alert.getPartnIdx().getValue());
                obj.field("prior", alert.getPrior().getValue());
                obj.field("single_ack", alert.getSingleAck().getValue());
                obj.field("visible", alert.getVisible().getValue());               

                // LangTextVar
                obj.field("abbr", alert.getAbbr().getValueObject());                
                obj.field("text", alert.getText().getValueObject());                
                obj.field("dest_text", alert.getDestText().getValueObject());                

                // DynVar                
                List<Variable> dv = alert.getAddValues().asList();
                for ( int j=0; j<dv.size(); j++)
                    obj.field("add_value."+j, dv.get(i).getValueObject());
                             
                obj.endObject();
                
                //JDebug.out.info(obj.string());
                if (count++==0) index = this.alertIndex+"-"+fmtTimeframe.format(alert.getDate());                
                bulkRequest.add(client.prepareIndex(index, this.alertType, getKey(alert)).setSource(obj));

            } catch (IOException ex) {
                JDebug.StackTrace(Level.SEVERE, ex);
            }
        }
        BulkResponse bulkResponse = bulkRequest.execute().actionGet();
        
        Date t2 = new Date();
        addServerStats(alerts.getHighWaterMark(), t2.getTime()-t1.getTime());       
        
        if (bulkResponse.hasFailures()) {
            JDebug.out.severe("error sending bulk request!");
            return INoSQLInterface.ERR_REPEATABLE;
        } else {
            return INoSQLInterface.OK;
        }
    }
    
    @Override
    public boolean dpGetPeriod(DpGetPeriodParameter param, DpGetPeriodResult result) {                
        // Execute queries
        HashMap<Dp, Set<String>> dpConfigs = createDpConfigAttrMap(param.dps);        
        int errors = dpConfigs.keySet().parallelStream()
                .mapToInt(dp -> {
                    return dpGetPeriod(param.t1, param.t2, dp, dpConfigs.get(dp), result) ? 0 : 1;
                })
                .sum(); // errors 

        result.setError(errors);
        result.setLast();                
        return true;        
    }      
        
    @Override    
    public boolean dpGetPeriod(Date t1, Date t2, Dp dp, Set<String> configs, DpGetPeriodResult result) {
        JDebug.out.log(Level.INFO, "dpGetPeriod: {0}-{1} {2} {3}", new Object[]{t1, t2, dp.toString(), configs.toString()});
        ArrayList<Dp> dpConfigs = createDpConfigAttrList(dp, configs);     
        if (dpConfigs.isEmpty()) {
            JDebug.out.warning("dpGetPeriod without any valid config.");
            return false;
        }           
        
        final SimpleDateFormat fmt = new SimpleDateFormat(FMT_DATE);
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {                                                                               
            //JDebug.out.info("dpGetPeriod dp="+getTagOfDp(dp));
            SearchRequestBuilder request = this.client.prepareSearch(this.eventIndex+"s")
                    .setTypes(this.eventType)
                    .setScroll(new TimeValue(60000))
                    .setSize(1000)
                    .addDocValueField("@timestamp");
                    
//            request.setQuery(boolQuery().must(rangeQuery("id").gte(getId(dp, t1.getTime())).lte(getId(dp, t2.getTime()))).must(termQuery("tag", getTagOfDp(dp))));
            request.setQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery("@timestamp").gte(t1.getTime()).lte(t2.getTime())).must(QueryBuilders.matchQuery("tag", getTagOfDp(dp)).operator(Operator.AND)));
            
            //JDebug.out.info("REQUEST: "+request.toString());
            SearchResponse response = request.execute().actionGet(); 
            //JDebug.out.info("RESPONSE: "+response.toString());
            //String scrollId=response.getScrollId();
            
            int records=0;             
            while (true) {
                for ( SearchHit hit : response.getHits().getHits() ) {
                    records++;
                    Map<String, SearchHitField> fields = hit.getFields();
                    final Date ts = new Date((Long)fields.get("@timestamp").getValue());
                    
                    Map<String, Object> source = hit.getSource();
                    //JDebug.out.info(source.keySet().toString());
                    
                    dpConfigs.forEach((Dp dpc)->{
                        Object value;
                        switch (dpc.getAttribute()) {
                            case Stime:
                                result.addVariable(dpc, ts, new TimeVar(ts));
                                break;
                            case Status:
                                value = source.get(attrMap.get(dpc.getAttribute()).get(0));                                
                                result.addVariable(dpc, ts, new Bit32Var(value));
                                break;                                    
                            case Status64:
                                value = source.get(attrMap.get(dpc.getAttribute()).get(0));
                                result.addVariable(dpc, ts, new Bit64Var(value));
                                break;
                            case Value: 
                                value = source.get("value");
                                //JDebug.out.info("tag="+source.get("tag")+" value="+value.getClass().getName());
                                if ( value instanceof HashMap ) {
                                    //JDebug.out.info(((HashMap) value).keySet().toString());
                                    ((HashMap) value).keySet().forEach((key)->{
                                        Object v = ((HashMap)value).get(key);
                                        //JDebug.out.info(key + ": "+v.getClass().getName()+": "+v.toString());
                                        result.addValue(dpc, ts, v);
                                    });
                                }
                                break;
                            default:
                                value = source.get(attrMap.get(dpc.getAttribute()).get(0));
                                result.addValue(dpc, ts, value);                                
                                break;                                                                
                        }
                    });
                }                                
                response = client.prepareSearchScroll(response.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
                if (response.getHits().getHits().length == 0) {
                    break;
                }        
            }
            //JDebug.out.log(Level.FINE, "dpGetPeriod: {0} records", records);
            return true;
        } catch (Exception ex) {
            JDebug.StackTrace(Level.SEVERE, ex);
            return false;
        }
    }    
}
