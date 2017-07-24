package at.rocworks.oa4j.logger.data.types.json;

import at.rocworks.oa4j.logger.data.Dp;
import at.rocworks.oa4j.logger.data.base.ValueItem;
import at.rocworks.oa4j.var.TimeVar;
import at.rocworks.oa4j.logger.data.base.EventItem;

import java.io.Serializable;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.json.simple.JSONObject;
import at.rocworks.oa4j.base.JDebug;


public class EventItemJSON extends EventItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private final JSONObject obj;
    private final JSONObject ids;
    private final JSONObject attrs;

    public EventItemJSON(JSONObject obj) {
        super(new Dp((String) obj.get("name")));
        this.obj = obj;                
        this.ids = (JSONObject) (obj.containsKey("ids") ? obj.get("ids") : null);
        this.attrs = (JSONObject) (obj.containsKey("attrs") ? obj.get("attrs") : null);
    }

    @Override
    public boolean hasAttributes() {
        return !(attrs == null);
    }
    
    @Override
    public long getTimeMS() { // Milliseconds
        long t = readTimeMS();
        if ( t == -1L ) {
            t = readTimeNS() / 1000000L;
        }
        return t;
    }
    
    @Override
    public long getTimeNS() { // Nanoseconds
        long t = readTimeNS();
        if ( t == -1L ) {
            t = readTimeMS() * 1000000L;
        }
        return t;        
    }    
    
    private long readTimeMS() {
        Long ms = -1L;
        
        if (obj.containsKey("time")) {
            Object t = obj.get("time");
            if (t instanceof Long) {
                ms = (Long) t;
            } else if (t instanceof String) {
                SimpleDateFormat fmt = new SimpleDateFormat(TimeVar.FMT_DATE_JS_MS);
                try {
                    ms = fmt.parse((String) t).getTime();
                } catch (ParseException e) {
                    JDebug.out.severe(e.getMessage());
                }
            }
            //JDebug.out.finest("getStimeMS "+obj.get("stime")+" => "+ms);
        }
        return ms;
    }        
    
    public long readTimeNS() { // Nano Seconds        
        Long ns = -1L;
        
        if (obj.containsKey("time_ns")) {        
            Object t = obj.get("time_ns");
            if (t instanceof Long) {
                ns = (Long) t;
            } else if (t instanceof String) {
                SimpleDateFormat fmt = new SimpleDateFormat(TimeVar.FMT_DATE_JS_NS);
                try {
                    ns = fmt.parse((String) t).getTime();
                } catch (ParseException e) {
                    JDebug.out.severe(e.getMessage());
                }
            } else {
                return getTimeMS() * 1000000L;
            }
            //JDebug.out.finest("getStimeNS "+obj.get("stime")+" => "+ns);
        }
        return ns;        
    }    

    @Override
    public ValueItem getValue() {
        return new ValueItemJSON((JSONObject) obj.get("value"));
    }

    // Attributes
    @Override
    public long getStatus() {
        // TODO parseUnsignedLong only available in Java 8
        //return attrs==null ? 0 : Long.parseUnsignedLong((String) attrs.get("status"));
        
        //return attrs == null ? 0 : Long.parseLong((String) attrs.get("status"));
        // => SEVERE : java.lang.NumberFormatException: For input string: "9439544818970657025":
        
        BigInteger l = new BigInteger((String)attrs.get("status"));
        return l.longValue();
    }

    @Override
    public int getManager() {
        return getIntValue(attrs.get("manager"));
    }

    @Override
    public int getUser() {
        return getIntValue(attrs.get("user"));
    }
    
    private int getIntValue(Object value) {
        if (value == null) {
            return 0;
        } else  if (value instanceof Long) {
            return ((Long) value).intValue();
        } else if (value instanceof Integer) {
            return (Integer) value;
        } else {
            return 0;
        }
    }
}
