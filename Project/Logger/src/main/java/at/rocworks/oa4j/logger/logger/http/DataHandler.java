package at.rocworks.oa4j.logger.logger.http;

import at.rocworks.oa4j.logger.base.IDataCollector;
import at.rocworks.oa4j.logger.base.INoSQLInterface;
import at.rocworks.oa4j.logger.data.lists.DataList;
import at.rocworks.oa4j.logger.data.types.json.DataSetJSON;
import at.rocworks.oa4j.base.JDebug;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.logging.Level;

/**
 * Created by vogler on 2/23/2017.
 */ //-----------------------------------------------------------------------------------------
class DataHandler implements HttpHandler {

    private final IDataCollector collector;

    public DataHandler(IDataCollector noSQLServer) {
        this.collector = noSQLServer;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
        BufferedReader br = new BufferedReader(isr);
        String content = br.readLine();
        //isr.close();

        int ret;
        try {
            //JDebug.out.info(content);
            DataSetJSON data = new DataSetJSON(content);
            DataList events = data.getEvents();
            ret = collector.collectData(events);
            JDebug.out.log(Level.FINE, "ret={0} events={1} jthread={2}", new Object[]{ret, events.getHighWaterMark(), Thread.currentThread().getId()});
        } catch (Exception ex) {
            JDebug.StackTrace(Level.SEVERE, ex);
            ret = INoSQLInterface.ERR_UNRECOVERABLE;
        }

        String response = Integer.toString(ret);
        exchange.sendResponseHeaders(ret == INoSQLInterface.OK ? 200/* OK */ : 502 /* Bad Gateway */, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
            os.flush();
        }
    }
}
