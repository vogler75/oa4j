package at.rocworks.oa4j.logger.logger.http;

import at.rocworks.oa4j.logger.data.Dp;
import at.rocworks.oa4j.logger.query.DpGetPeriodParameter;
import at.rocworks.oa4j.base.JDebug;
import at.rocworks.oa4j.var.TimeVar;
import com.sun.net.httpserver.HttpExchange;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

/**
 * Created by vogler on 2/23/2017.
 */
//-----------------------------------------------------------------------------------------
class QueryParameter extends DpGetPeriodParameter {
    public int id;

    public QueryParameter (HttpExchange exchange) throws IOException {
        super(new Date(0), new Date(0), new ArrayList<>());

        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
        BufferedReader br = new BufferedReader(isr);
        String content = br.readLine();

        SimpleDateFormat fmt = new SimpleDateFormat(TimeVar.FMT_DATE_JS_MS);

        // query in content or as parameter
        if (content != null && !content.equals("")) {
            JSONParser parser = new JSONParser();
            Object obj;
            try {
                //JDebug.out.info(content);
                obj = parser.parse(content);
                fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
                t1 = fmt.parse((String) ((JSONObject) obj).get("t1"));
                t2 = fmt.parse((String) ((JSONObject) obj).get("t2"));
                if (((JSONObject)obj).containsKey("id")) {
                    id =(int)((long) ((JSONObject)obj).get("id"));
                }
                JSONArray list = (JSONArray) ((JSONObject) obj).get("dps");
                for (int i = 0; i < list.size(); i++) {
                    dps.add(new Dp((String) list.get(i)));
                }
            } catch (java.text.ParseException | org.json.simple.parser.ParseException ex) {
                JDebug.StackTrace(Level.SEVERE, ex);
            }
        } else {
            Map<String, Object> parameters = new HashMap<>();
            URI requestedUri = exchange.getRequestURI();
            String query = requestedUri.getRawQuery();
            //JDebug.out.log(Level.INFO, "DpGetPeriodParameter: {0}", query);

            parseQuery(query, parameters);

            JSONParser parser = new JSONParser();
            try {
                if (parameters.containsKey("t1")) {
                    t1 = fmt.parse((String) parameters.get("t1"));
                }
                if (parameters.containsKey("t2")) {
                    t2 = fmt.parse((String) parameters.get("t2"));
                }
                if (parameters.containsKey("id")) {
                    id = Integer.valueOf((String) parameters.get("id"));
                }
                if (parameters.containsKey("dps")) {
                    JSONArray list = (JSONArray) parser.parse((String) parameters.get("dps"));
                    for (int i = 0; i < list.size(); i++) {
                        dps.add(new Dp((String) list.get(i)));
                    }
                }

            } catch (java.text.ParseException | org.json.simple.parser.ParseException ex) {
                JDebug.StackTrace(Level.SEVERE, ex);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void parseQuery(String query, Map<String, Object> parameters) throws UnsupportedEncodingException {

        if (query != null) {
            String pairs[] = query.split("[&]");

            for (String pair : pairs) {
                String param[] = pair.split("[=]");

                String key = null;
                String value = null;
                if (param.length > 0) {
                    key = URLDecoder.decode(param[0], "utf-8");
                }

                if (param.length > 1) {
                    value = URLDecoder.decode(param[1], "utf-8");
                }

                if (parameters.containsKey(key)) {
                    Object obj = parameters.get(key);
                    if (obj instanceof List<?>) {
                        List<String> values = (List<String>) obj;
                        values.add(value);
                    } else if (obj instanceof String) {
                        List<String> values = new ArrayList<>();
                        values.add((String) obj);
                        values.add(value);
                        parameters.put(key, values);
                    }
                } else {
                    parameters.put(key, value);
                }
            }
        }
    }
}