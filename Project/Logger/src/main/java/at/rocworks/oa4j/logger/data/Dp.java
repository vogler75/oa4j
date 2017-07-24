package at.rocworks.oa4j.logger.data;

import java.io.Serializable;
import java.util.regex.Pattern;

public class Dp implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String name;
    private DpAttr attribute;

    public Dp(String name) {
        this.name = name;
        this.attribute = DpAttr.Unknown;
        //JDebug.out.log(Level.FINE, "Datapoint: {0}/{1}/{2}/{3}", new Object[]{getSystem(), getDp(), getElement(), getConfig()});
    }
    
    public Dp(String name, DpAttr attribute) {
        this.name = name;
        this.attribute = attribute;
        //JDebug.out.log(Level.FINE, "Datapoint: {0}/{1}/{2}/{3}", new Object[]{getSystem(), getDp(), getElement(), getConfig()});
    }    

    public String getSystem() {
        Pattern p = Pattern.compile(":");
        String[] s = p.split(name);
        return s[0];
    }

    public String getDp() {
        Pattern p = Pattern.compile(":|\\.");
        String[] s = p.split(name);
        return s[1];
    }
    
     public String getDpEl() {
        Pattern p = Pattern.compile(":");
        String[] s = p.split(name);
        return s[1];
    }   

    public String getElement() {
        int start = name.indexOf('.') + 1;
        int end = name.indexOf(':', start);
        return (end < 0 ? name.substring(start) : name.substring(start, end));
    }

    public String getConfig() {
        Pattern p = Pattern.compile(":");
        String[] s = p.split(name);
        return s.length == 3 ? s[2] : "";
    }
    
    private String getAttributeString() {
        Pattern p = Pattern.compile("\\.");
        String[] s = p.split(getConfig());
        return s.length < 3 ? "" : s[2].isEmpty() ? "" : s[2];
        
    }
    
    public DpAttr getAttribute() {
        if ( attribute == DpAttr.Unknown ) {
            switch ( getAttributeString() ) {
                case "_value": return (attribute=DpAttr.Value);
                case "_stime": return (attribute=DpAttr.Stime);
                case "_status": return (attribute=DpAttr.Status);
                case "_status64": return (attribute=DpAttr.Status64);
                case "_manager": return (attribute=DpAttr.Manager);
                case "_user": return (attribute=DpAttr.User);
                case "_system_time": return (attribute=DpAttr.SystemTime);
                default: return attribute; // Unknown
            }
        } else {
            return attribute;
        }
    }
    
    public int getDetail() { // detail of config e.q. _alert_hdl.1._came_time => 1
        Pattern p = Pattern.compile("\\.");
        String[] s = p.split(getConfig());
        return s.length < 2 ? 0 : s[1].isEmpty() ? 0 : Integer.parseInt(s[1]);
    }

    public String getFQN() {
        return name; //getSystem() + ":" + getDp() + "." + getElement() + ":" + getConfig();
    }
    
    public String getSysDp() {
        return getSystem() + ":" + getDp();
    }
    
    public String getSysDpEl() {
        return getSystem() + ":" + getDp() + "." + getElement();
    }    
    
    @Override
    public String toString() {
        return name;
    }
    
    @Override
    public int hashCode() {
//        int hashCode = 0;
//        for (int i = 0; i < name.length(); i++) {
//            hashCode = hashCode * 31 + name.charAt(i);
//        }
//        return hashCode;
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Dp other = (Dp) obj;
        return name.equals(other.name);
    }    
}
