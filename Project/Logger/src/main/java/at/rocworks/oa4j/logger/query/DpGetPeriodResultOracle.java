package at.rocworks.oa4j.logger.query;

import at.rocworks.oa4j.logger.data.Dp;
import at.rocworks.oa4j.var.TimeVar;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import at.rocworks.oa4j.var.Variable;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class DpGetPeriodResultOracle extends DpGetPeriodResult {

    private final SimpleDateFormat fmtDate = new SimpleDateFormat(TimeVar.FMT_DATE_OA_MS);    
    
    private final JSONArray arr = new JSONArray();
    private String json;
    
    public DpGetPeriodResultOracle() {
        super();
        fmtDate.setTimeZone(TimeZone.getTimeZone("UTC"));        
    }

    private String toJSONString() {
        if (json == null) {
            json = arr.toJSONString();
        }
        return json;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addValue(Dp dp, Date ts, Object value) {
        JSONObject obj = new JSONObject(); // column		

        obj.put("t", fmtDate.format(ts));
        obj.put("e", dp.getFQN());
        obj.put("v", value);

        synchronized (arr) {
            arr.add(obj);
        }
        if (json!=null)
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
