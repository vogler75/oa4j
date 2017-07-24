package at.rocworks.oa4j.logger.logger.http;

import at.rocworks.oa4j.logger.base.IDataReader;
import at.rocworks.oa4j.logger.logger.http.QueryParameter;
import at.rocworks.oa4j.logger.query.DpGetPeriodResult;
import at.rocworks.oa4j.base.JDebug;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;

/**
 * Created by vogler on 2/23/2017.
 */
//-----------------------------------------------------------------------------------------
class QueryHandler implements HttpHandler {

    private final IDataReader reader;
    private final Class resultFactory;
    private final boolean chunked;
    private volatile int counter=0;

    private final HashMap<Integer, DpGetPeriodResult> results;

    public <T extends DpGetPeriodResult> QueryHandler(IDataReader noSQLServer, Class<T> resultFactory) {
        this.results = new HashMap<>();
        this.reader = noSQLServer;
        this.resultFactory = resultFactory;
        this.chunked = false;
    }

    public <T extends DpGetPeriodResult> QueryHandler(IDataReader noSQLServer, Class<T> resultFactory, boolean chunked) {
        this.results = new HashMap<>();
        this.reader = noSQLServer;
        this.resultFactory = resultFactory;
        this.chunked = chunked;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            Date t1 = new Date();
            int ret = 200 /*OK*/;

            final QueryParameter param = new QueryParameter(exchange);

            DpGetPeriodResult result = null;
            byte[] b = new byte[0];

            //JDebug.out.log(Level.INFO, "{0} {1}", new Object[] {exchange.getRequestURI(), param});

            if ( !chunked ) {
                result = (DpGetPeriodResult) resultFactory.newInstance();
                if (reader.dpGetPeriod(param, result))
                    b = result.getBytes();
                else
                    ret=502; /*Internal Server Error*/
            } else {
                if (param.id == 0) { // new query
                    counter++;
                    result = (DpGetPeriodResult) resultFactory.newInstance();
                    final DpGetPeriodResult tresult = result; // must be final for thread
                    Thread t = new Thread(()->reader.dpGetPeriod(param, tresult));
                    param.id = t.hashCode();
                    result.setId(param.id); // set it of thread to result

                    // TODO somewhere a cleanup of queries, client connection can break...

                    results.put(result.getId(), result); // add result to global list
                    t.start();
                    JDebug.out.log(Level.INFO, "{0} strt {1}", new Object[] {exchange.getRequestURI(), counter});
                }
                if ( results.containsKey(param.id) ) {
                    result = results.get(param.id);
                    result = result.getChunk();
                    //JDebug.out.log(Level.INFO, "got chunk={0} size={1} isLast={2}", new Object[]{result.getChunkNr(), result.getSize(), result.isLast()});

                    b = result.getBytes();
                    if ( result.isLast() ) {
                        counter--;
                        results.remove(result.getId());
                        JDebug.out.log(Level.INFO, "{0} done {1}", new Object[] {exchange.getRequestURI(), counter});
                    }
                } else {
                    ret=404; /*Not Found*/
                    JDebug.StackTrace(Level.SEVERE, "query id "+param.id+" not found");
                }
            }

            Date t2 = new Date();

            // serialize result
            exchange.sendResponseHeaders(ret, b.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(b);
                os.flush();
            }

            Date t3 = new Date();

            JDebug.out.log(Level.FINE, "DpGetPeriodHandler: Size: {0} Bytes: {1} Time: {2}ms Query: {3}ms Transmit: {4}ms",
                    new Object[] {result==null?-1:result.getSize(), b.length, (t3.getTime()-t1.getTime()), (t2.getTime()-t1.getTime()), (t3.getTime()-t2.getTime())});
        } catch (IOException | InstantiationException | IllegalAccessException ex) {
            JDebug.StackTrace(Level.SEVERE, ex);
        }
    }
}