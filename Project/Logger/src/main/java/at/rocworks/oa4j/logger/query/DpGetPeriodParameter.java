package at.rocworks.oa4j.logger.query;

import at.rocworks.oa4j.var.TimeVar;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.ArrayList;

import at.rocworks.oa4j.logger.data.Dp;

public class DpGetPeriodParameter {

    //public int id = 0;    
    public Date t1;
    public Date t2;
    public ArrayList<Dp> dps;    

    public DpGetPeriodParameter(Date t1, Date t2, Dp dp) {
        this.t1 = t1;
        this.t2 = t2;
        this.dps = new ArrayList<>();
        this.dps.add(dp);
    }
    
    public DpGetPeriodParameter(Date t1, Date t2, ArrayList<Dp> dps) {
        this.t1 = t1;
        this.t2 = t2;
        this.dps = dps;
    }

    @SuppressWarnings("unchecked")
    public JSONObject toJSONObject() {
        SimpleDateFormat fmt = new SimpleDateFormat(TimeVar.FMT_DATE_JS_MS);
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        JSONObject json = new JSONObject();
        json.put("t1", fmt.format(t1));
        json.put("t2", fmt.format(t2));
        JSONArray jdps = new JSONArray();
        dps.forEach((Dp dp)->jdps.add(dp.toString()));
        json.put("dps", jdps);                
        return json;
    }

    @Override
    public String toString() {        
        return toJSONObject().toJSONString();        
    }
    
    
}
