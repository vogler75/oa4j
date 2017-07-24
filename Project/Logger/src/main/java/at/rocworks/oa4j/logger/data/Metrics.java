/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;
import com.google.gson.GsonBuilder;

/**
 *
 * @author vogler
 */
public class Metrics {
    private final String name;
    private final Map<String, Object> metrics = new HashMap<>();
    private final Map<String, Metrics> groups = new HashMap<>();
    
    public Metrics(String name) {
        this.name=name;
    }
    
    public Double getDouble(String key) {
        Object v = metrics.get(key);
        return v!=null && v instanceof Double ? (Double)v : 0;
    }  
    
    public String getString(String key) {
        Object v = metrics.get(key);
        return v!=null && v instanceof String ? (String)v : "";
    }      
    
    public Metrics put(String key, Double value) {
        metrics.put(key, value);
        return this;
    }
    
    public Metrics put(String key, String value) {
        metrics.put(key, value);
        return this;
    }    
    
    public Metrics put(String key, JSONObject value) {
        metrics.put(key, value);
        return this;
    }    
    
    
    public Metrics put(Metrics stats) {
        groups.put(stats.name, stats);
        return this;
    }                
    
    public Set<String> getMetricKeys() {
        return metrics.keySet();
    }
    
    public int count() {
        return metrics.size();
    }

    @SuppressWarnings("unchecked")
    public JSONObject toJSONObject() {
        JSONObject r;        
        if ( groups.isEmpty() ) {            
            r=new JSONObject();
            r.put(name, new JSONObject(metrics));
            //r=new JSONObject(metrics);            
        } else {
            r=new JSONObject();
            if (!metrics.isEmpty()) r.put(name, new JSONObject(metrics));
            groups.forEach((k, s)->r.put(k, s.toJSONObject()));            
        }        
        return r;
    }
    
    public String toJSONString() {
        return toJSONObject().toJSONString();
    }
    
    public String toJSONPrettyString() {       
        return (new GsonBuilder().setPrettyPrinting().create()).toJson(toJSONObject());
    }
    
//    public Statistics sumMetrics(String name, Statistics s) {
//        s.metrics.keySet().forEach((String key)->{
//            String metric=getMetricOfKey(key);
//            Float v1 = s.get(key);            
//            Float v2 = this.get(name, metric);            
//            this.put(name, metric, v1+v2);
//        });
//        return this;
//    }
}
