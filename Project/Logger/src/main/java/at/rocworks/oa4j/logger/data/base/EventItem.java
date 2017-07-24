package at.rocworks.oa4j.logger.data.base;

import at.rocworks.oa4j.logger.data.Dp;
import java.io.Serializable;
import java.util.Date;

import org.json.simple.JSONObject;

//-----------------------------------------------------------------------------------------
public abstract class EventItem extends DataItem implements Serializable {

    public EventItem(Dp dp) {
        super(dp);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public JSONObject toJSONObject() {
        JSONObject ret = new JSONObject();
        
        JSONObject data = super.toJSONObject();
        data.forEach((key, value)->ret.put(key, value));       
        
        Object val = this.getValue().getValueObject();
        if ( val instanceof Date ) val = ((Date)val).getTime();
        
        ret.put("Value", val); 
        ret.put("Status", this.getStatus());
        ret.put("Manager", this.getManager());
        ret.put("User", this.getUser());
        return ret;
    }
    
//    public JSONObject toJSONObject() {
//        JSONObject ret = new JSONObject();
//        ret.put("Data", super.toJSONObject());
//        
//        JSONObject obj = new JSONObject();
//        ret.put("Event", obj);       
//        obj.put("Value", this.getValue().toString()); 
//        obj.put("Status", this.getStatus());
//        obj.put("Manager", this.getManager());
//        obj.put("User", this.getUser());
//        return ret;
//    }    

    public abstract ValueItem getValue();

    // Attributes   
    public abstract boolean hasAttributes();    
    public abstract long getStatus();
    public abstract int getManager();
    public abstract int getUser();
}
