/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.data.base;

import at.rocworks.oa4j.logger.data.Dp;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.simple.JSONObject;

/**
 *
 * @author vogler
 */
public abstract class DataItem implements Serializable {

    private final Dp dp;
    private int nanos;    
    
    public DataItem(Dp dp) {
        this.dp=dp;
        this.nanos=0;
    }
    
    public Dp getDp() {
        return dp;
    }

    @SuppressWarnings("unchecked")
    public JSONObject toJSONObject() {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        JSONObject obj = new JSONObject();
        obj.put("Name", dp.getFQN());
        obj.put("Time", fmt.format(getDate()));  
        obj.put("TimeMS", getTimeMS());
        return obj;
    }

    @Override
    public String toString() {
        // pretty print
        //Gson gson = new GsonBuilder().setPrettyPrinting().create();
        //return gson.toJson(toJSONObject());
        // flat print
        return toJSONObject().toString();
    }
    
    abstract public long getTimeMS(); // Milli
    abstract public long getTimeNS(); // Nano
    
    public void setAddedNanos(int nanos) {
        this.nanos=nanos;
    }
    public int getAddedNanos() {
        return this.nanos;
    }
    public int getNanos() {
        return (int)(getTimeNS()%1000000000L);
    }
    
    public Date getDate() {
        return new Date(getTimeMS());
    }

}
