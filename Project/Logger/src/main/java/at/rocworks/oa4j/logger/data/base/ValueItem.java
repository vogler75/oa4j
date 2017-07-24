package at.rocworks.oa4j.logger.data.base;

import at.rocworks.oa4j.var.TimeVar;
import java.text.SimpleDateFormat;
import java.util.Date;
import at.rocworks.oa4j.var.VariableType;

//-----------------------------------------------------------------------------------------
public abstract class ValueItem {

//    public static final String FMT_DATE_JS_MS = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
//    public static final String FMT_DATE_JS_NS = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'"; // Nanoseconds    
//    public static final String FMT_DATE_JS_SEC = "yyyy-MM-dd'T'HH:mm:ss'Z'";
//    
//    public static final String FMT_DATE_OA_MS = "yyyy.MM.dd HH:mm:ss.SSS";    

    private final SimpleDateFormat fmt = new SimpleDateFormat(TimeVar.FMT_DATE_JS_MS);

    public abstract VariableType getVariableType();
    
    @Override
    public String toString() {
        switch (getVariableType()) {
            case TextVar:
                return getString();
            case CharVar:
                return getChar();
            case IntegerVar:
            case UIntegerVar:
                return getLong().toString();
            case BitVar:
                return getBoolean().toString();
            case FloatVar:
                return getDouble().toString();
            case TimeVar:
                return fmt.format(getTime());
            case Unknown:
                return "<unknown>";
            default:
                return "<unhandeled>";
        }
    }        

    public abstract String getString();

    public abstract String getChar();

    public abstract Long getLong();

    public abstract Double getDouble();

    public abstract Boolean getBoolean();

    public abstract Date getTime();

    public abstract Long getTimeMS();   
    
    public abstract Object getValueObject();
}
