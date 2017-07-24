package at.rocworks.oa4j.logger.query;

import at.rocworks.oa4j.var.TimeVar;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import at.rocworks.oa4j.var.Variable;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import at.rocworks.oa4j.logger.data.Dp;

public class DpGetPeriodResultJSON extends DpGetPeriodResult {
    
    private final SimpleDateFormat fmtDate = new SimpleDateFormat(TimeVar.FMT_DATE_OA_MS);
    
    private final JSONObject obj = new JSONObject();
    private String json;
    
    public DpGetPeriodResultJSON() {
        super();
        fmtDate.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private String toJSONString() {
        if ( json == null ) 
            json=obj.toJSONString();
        return json;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addValue(Dp dp, Date ts, Object value) {
        JSONArray t;
        JSONArray v;
        JSONObject c; // column
        //Logger.DebugTN("addValue: "+dp.getFQN()+" "+ts+" "+value.toString());
        if (obj.containsKey(dp.getFQN())) {
            c = (JSONObject) obj.get(dp.getFQN());
            t = (JSONArray) (c.get("t"));
            v = (JSONArray) (c.get("v"));
        } else {
            c = new JSONObject();
            t = new JSONArray();
            v = new JSONArray();
            c.put("t", t);
            c.put("v", v);
            synchronized (obj) {
                obj.put(dp.getFQN(), c);
            }
        }
        t.add(fmtDate.format(ts));
        if (value instanceof Date) {
            v.add(fmtDate.format(value));
        } else if (dp.getConfig().equals("_online.._status")) {
            v.add(Long.toBinaryString((Long) value));
        } else {
            v.add(value);
        }        
        if ( json != null )
            json=null;
        
        size++;
    }
    
    @Override
    public void addVariable(Dp dp, Date ts, Variable value) {
        addValue(dp, ts, value.getValueObject());
    }    

    @Override
    public byte[] getBytes() {
        return toJSONString().getBytes();
    }

    @Override
    public DpGetPeriodResult getChunk() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
