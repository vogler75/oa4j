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
import at.rocworks.oa4j.logger.data.base.DataItem;
import at.rocworks.oa4j.logger.data.lists.DataList;
import at.rocworks.oa4j.logger.data.Dp;
import at.rocworks.oa4j.logger.data.DpAttr;
import at.rocworks.oa4j.logger.data.base.EventItem;
import at.rocworks.oa4j.logger.keys.KeyBuilder;
import at.rocworks.oa4j.logger.query.DpGetPeriodResult;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class NoSQLInfluxDB extends NoSQLServer {

    private final String url;
    private final String db;
    private final KeyBuilder key;

    HashMap<DpAttr, String> attrMap = new HashMap<DpAttr, String>() {
        {
            put(DpAttr.Value, "\"value\",\"type\"");
            put(DpAttr.Status, "\"status\"");
            put(DpAttr.Status64, "\"status\"");
            put(DpAttr.Manager, "\"manager\"");
            put(DpAttr.User, "\"user\"");
        }
    };

    public static NoSQLServer createServer(NoSQLSettings srvcfg, String srvprefix) {
        String url = srvcfg.getStringProperty(srvprefix, "url", "http://localhost:8086");
        String db = srvcfg.getStringProperty(srvprefix, "db", "pvss");
        String fmt = srvcfg.getStringProperty(srvprefix, "measurement", "dpel");
        JDebug.out.log(Level.CONFIG, "{0}.type: {1} \n{2}\nurl: {3}\ndb: {4}",
                new Object[]{srvprefix, NoSQLInfluxDB.class.getName(), srvcfg, url, db});
        return new NoSQLInfluxDB(srvcfg, KeyBuilder.getKeyBuilder(fmt), url, db);
    }    
    
    public NoSQLInfluxDB(NoSQLSettings settings, KeyBuilder key, String url, String db) {
        super(settings);
        this.url = url;
        this.db = db;
        this.key = key;
    }

    @Override
    public String toString() {
        return url + "/" + db;
    }
    
    Set<VariableType> unhandled = ConcurrentHashMap.newKeySet();        
    
    @Override
    public int storeData(DataList events) {
        int i, k, ret;
        DataItem item;
        EventItem event;
        StringBuffer data = new StringBuffer();
        k = 0;
        for (i = 0; i < events.size() && (item = events.getItem(i)) != null; i++) {
            if (!(item instanceof EventItem)) {
                continue;
            }
            event = (EventItem) item;

            //<metric>,tagk=tagv,tagk=tagv value=0.64 1434055562000000000
            String value, tag;
            try {
                switch (event.getValue().getVariableType()) {
                    case FloatVar:
                        value = String.format(Locale.ENGLISH, "%f", event.getValue().getDouble());
                        tag = ",type=float"; // add a tag
                        break;
                    case IntegerVar:
                    case UIntegerVar:
                        value = event.getValue().getLong().toString() + "i";
                        tag = ",type=integer"; // add a tag
                        break;
                    case TimeVar:
                        value = event.getValue().getTimeMS().toString();
                        tag = ",type=time"; // add a tag
                        break;
                    case BitVar:
                        value = event.getValue().getBoolean() ? "1" : "0";
                        tag = ",type=bool"; // add a tag
                        break;
                    case Bit32Var:
                        value = event.getValue().getLong().toString() + "i";
                        tag = ",type=bit32"; // add a tag
                        break;
                    case Bit64Var:
                        value = event.getValue().getLong().toString() + "i";
                        tag = ",type=bit64"; // add a tag
                        break;                        
                    case TextVar:
                        value = String.format("\"%s\"", URLEncoder.encode(event.getValue().getString(), "UTF-8"));
                        tag = ",type=text"; // add a tag   
                        break;
                    case Unknown:
                        JDebug.out.log(Level.WARNING, "unknown value type {0}", event.getValue().getVariableType());
                        continue;
                    default:
                        VariableType vtype = event.getValue().getVariableType();
                        if (unhandled.add(vtype)) {
                            JDebug.out.log(Level.WARNING, "unhandeled value type {0}", vtype);
                        }
                        continue;
                }
            } catch (Exception ex) {
                JDebug.StackTrace(Level.SEVERE, ex);
                continue;
            }
            if (++k > 1) {
                data.append((char) 10);
            }
            data.append(key.getStoreOfDp(event.getDp()))
                    .append(",tag=").append(key.getTagOfDp(event.getDp()))
                    .append(",sys=").append(event.getDp().getSystem())
                    .append(",dp=").append(event.getDp().getDp())
                    .append(",el=").append(Variable.nvl(event.getDp().getElement(), "."))
                    .append(tag)
                    .append(" value=").append(value);
            // status, manager, user
            if (event.hasAttributes()) {
                data.append(",status=").append(event.getStatus()).append("i");
                data.append(",manager=").append(event.getManager()).append("i");
                data.append(",user=").append(event.getUser()).append("i");
            }
            data.append(" ").append(event.getTimeNS());
        }

        if (data.length() == 0) {
            JDebug.out.warning("sendEvents: no values");
            ret = INoSQLInterface.OK;
        } else {
            try {
                Date t1 = new Date();
                StringBuffer result = new StringBuffer();
                int httpRet = HttpUtil.httpPost(this.url + "/write?db=" + this.db, data, result);
                if (httpRet >= 200 && httpRet <= 299) { // Successful					
                    ret = INoSQLInterface.OK;
                } else {
                    JDebug.out.log(Level.SEVERE, "Error {0} request to: {1}", new Object[]{httpRet, url});
                    if (httpRet != 404 /*page/db does not exists*/ && (httpRet >= 400 && httpRet <= 499)) { // Client Error
                        //Debug.out.log(Level.SEVERE, "data: {0}", data);
                        JDebug.out.log(Level.SEVERE, "result: {0}", result.toString());
                        ret = INoSQLInterface.ERR_UNRECOVERABLE;
                    } else {
                        ret = INoSQLInterface.ERR_REPEATABLE;
                    }
                }
                Date t2 = new Date();
                addServerStats(events.getHighWaterMark(), t2.getTime() - t1.getTime());
            } catch (IOException ex) {
                JDebug.StackTrace(Level.SEVERE, ex);
                ret = INoSQLInterface.ERR_REPEATABLE;
            }
        }

        return ret;
    }

    @Override
    public boolean dpGetPeriod(Date t1, Date t2, Dp dp, Set<String> configs, DpGetPeriodResult result) {
        // select "tag","value" from "Test_1_1.Value" where "tag" = 'System2/Test_1_1/_online.._value' and time > 1448623350000000u and time < 1448623362000000u 
        /*
        {  
           "results":[  
              {  
                 "series":[  
                    {  
                       "name":"Test_1_1.Value",
                       "columns":[  
                          "time",
                          "tag",
                          "value"
                       ],
                       "values":[  
                          [  
                             "2015-11-28T06:21:27.915Z",
                             "System2/Test_1_1/_online.._value",
                             3558.719
                          ],
                          [  
                             "2015-11-28T06:24:07.129Z",
                             "System2/Test_1_1/_online.._value",
                             5
                          ]
                       ]
                    }
                 ]
              }
           ]
        }        */

        //Debug.out.log(Level.INFO, "dpGetPeriod: {0}-{1} {2} {3}", new Object[]{t1, t2, dp.toString(), configs.toString()});        
        final SimpleDateFormat fmt = new SimpleDateFormat(TimeVar.FMT_DATE_JS_MS);
        final SimpleDateFormat fmtSecOnly = new SimpleDateFormat(TimeVar.FMT_DATE_JS_SEC);

        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        fmtSecOnly.setTimeZone(TimeZone.getTimeZone("UTC"));

        StringBuilder query = new StringBuilder();
        query.append("select \"tag\"");

        ArrayList<Dp> dps = createDpConfigAttrList(dp, configs);
        if (dps.isEmpty()) {
            JDebug.out.warning("dpGetPeriod without any valid config.");
            return false;
        }

        StringBuilder columns = new StringBuilder();
        dps.forEach((Dp x) -> {
            String c=attrMap.get(x.getAttribute());
            if ( c!= null ) columns.append(",").append(c);
        });
        query.append(columns);

        query.append("from \"").append(key.getStoreOfDp(dp)).append("\"");
        query.append(" where \"tag\" = '").append(key.getTagOfDp(dp)).append("'");
        query.append(" and time >= ").append(t1.getTime() * 1000).append("u ");
        query.append(" and time <= ").append(t2.getTime() * 1000).append("u ");

        boolean ok = false;
        try {
            String request = this.url + "/query?db=" + this.db + "&q=" + URLEncoder.encode(query.toString(), "UTF-8");
            JDebug.out.log(Level.FINE, "DpGetPeriod: {0}", request);
            StringBuffer content = new StringBuffer();
            int ret = HttpUtil.httpGet(request, content);
            if (ret == 200) {
                JSONParser parser = new JSONParser();
                try {
                    int i, j, k, l;
                    Object value;
                    String s, type;
                    Date ts;

                    JSONObject body = (JSONObject) parser.parse(content.toString());
                    JSONArray results = (JSONArray) body.get("results");
                    for (i = 0; i < results.size(); i++) {
                        JSONObject jresult = (JSONObject) (results.get(i));
                        JSONArray series = (JSONArray) jresult.get("series");
                        if (series == null) {
                            continue;
                        }
                        for (j = 0; j < series.size(); j++) { // series
                            JSONObject serie = (JSONObject) series.get(j);
                            JSONArray values = (JSONArray) serie.get("values");
                            for (k = 0; k < values.size(); k++) { // values
                                // timestamp
                                JSONArray valueSet = (JSONArray) values.get(k);
                                s = (String) valueSet.get(0);
                                try {
                                    // InfluxDB returns timestamps wihtout ms if the timestamp is .000                                    
                                    if (((String) s).length() == ("0000-00-00T00:00:00Z".length())) {
                                        ts = fmtSecOnly.parse((String) s);
                                    } else {
                                        ts = fmt.parse((String) s);
                                    }
                                } catch (Exception ex) {
                                    JDebug.StackTrace(Level.SEVERE, ex);
                                    continue;
                                }

                                // attributes
                                int c = 1;
                                for (l = 0; l < dps.size(); l++) {
                                    final Dp attr=dps.get(l);
                                    switch (attr.getAttribute()) {
                                        case Value: 
                                            value = valueSet.get(++c);                                            
                                            type = (valueSet.get(++c) != null ? (String) valueSet.get(c) : "");
                                            //Debug.out.info("type="+type);
                                            switch (type) {
                                                case "time":
                                                    value = new Date(((Double) value).longValue());
                                                    break;
                                                case "bool":
                                                    Boolean b = (Long) value == 1;
                                                    value = b;
                                                    break;
                                                case "text":
                                                    value = URLDecoder.decode((String) value, "UTF-8");
                                                    break;
                                                case "float":
                                                    // value is of type java.lang.Double
                                                    break;
                                                default:
                                                    JDebug.out.log(Level.WARNING, "unhandeled value type {0} {1}", new Object[]{type, value.getClass().getName()});
                                                    break;
                                            }
                                            //Debug.out.info("value type="+value.getClass().getName());                                            
                                            result.addValue(attr, ts, value);
                                            break;
                                        case Status:
                                            value = valueSet.get(++c);                                                                                        
                                            result.addVariable(attr, ts, new Bit32Var(value));
                                            break;
                                        case Status64: 
                                            value = valueSet.get(++c);                                                                                        
                                            result.addVariable(attr, ts, new Bit64Var(value));
                                            break;
                                        case Stime: 
                                            value = ts;
                                            result.addVariable(attr, ts, Variable.newVariable(value)); 
                                            break;   
                                        default:
                                            value = valueSet.get(++c);
                                            JDebug.out.log(Level.WARNING, "other attr={0} type={1} of {2}", new Object[]{attr.getAttribute(), value == null ? "null" : value.getClass().getName(), dps.get(l).getConfig()});
                                            result.addValue(attr, ts, value);
                                            break;
                                    }
                                }
                            }
                        }
                    }
                    ok = true;
                } catch (ParseException ex) {
                    JDebug.StackTrace(Level.SEVERE, ex);
                }
            } else {
                JDebug.out.log(Level.SEVERE, "dpGetPeriod: {0} Error request: {1}", new Object[]{ret, query});
            }
        } catch (IOException ex) {
            JDebug.StackTrace(Level.SEVERE, ex);
        } catch (Exception ex) {
            JDebug.StackTrace(Level.SEVERE, ex);
        }
        return ok;
    }
}
