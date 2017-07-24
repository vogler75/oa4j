package at.rocworks.oa4j.logger.data.types.json;

import at.rocworks.oa4j.logger.data.lists.DataList;
import java.util.logging.Level;
import at.rocworks.oa4j.base.JDebug;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

//-----------------------------------------------------------------------------------------
public class DataSetJSON {

    private JSONArray arr;
    private DataList events;

    //{"events":[{"attrs":{"arch":"0","manager":16909313,"status":"9439544818968559873","text":[""]},"ids":{"dpid":20668,"elid":6},"name":"System2:Test_0_2.float","time":1445024341849,"value":{"float":1,"type":"float"}}]}
    public DataSetJSON(String content) {
        if (content == null) {
            events = new DataList(0);
        } else {
            JSONParser parser = new JSONParser();
            Object obj;
            try {
                obj = parser.parse(content);
                arr = (JSONArray) ((JSONObject) obj).get("events");
            } catch (ParseException ex) {
                JDebug.StackTrace(Level.SEVERE, ex);
            }
        }
    }

    public DataSetJSON(JSONArray arr) {
        this.arr = arr;
    }

    public DataList getEvents() {
        if (events == null && arr != null) {
            events = new DataList(arr.size());
            for (int i = 0; i < arr.size(); i++) {
                events.setItem(i, new EventItemJSON((JSONObject) arr.get(i)));
            }
        }
        return events;
    }
}
