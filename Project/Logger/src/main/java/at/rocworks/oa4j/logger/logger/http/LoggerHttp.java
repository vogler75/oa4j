package at.rocworks.oa4j.logger.logger.http;

import at.rocworks.oa4j.logger.logger.http.QueryParameter;

import at.rocworks.oa4j.logger.base.IDataCollector;
import at.rocworks.oa4j.logger.base.IDataReader;
import at.rocworks.oa4j.logger.base.INoSQLInterface;
import at.rocworks.oa4j.logger.data.lists.DataList;
import at.rocworks.oa4j.logger.data.types.json.DataSetJSON;
import at.rocworks.oa4j.logger.query.DpGetPeriodResult;
import at.rocworks.oa4j.logger.query.DpGetPeriodResultJSON;
import at.rocworks.oa4j.logger.query.DpGetPeriodResultJava;
import at.rocworks.oa4j.logger.query.DpGetPeriodResultOracle;
import at.rocworks.oa4j.base.JDebug;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.InetSocketAddress;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

/**
 *
 * @author vogler
 */
public class LoggerHttp implements Runnable {
    HttpServer httpServer;
    INoSQLInterface database;
    private final int threads;
    private final int port;

    public LoggerHttp(INoSQLInterface database, int threads, int port) {
        this.database=database;
        this.threads=threads;
        this.port=port;
    }

    @Override
    public void run() {
        try {
            ExecutorService executor;
            executor = Executors.newFixedThreadPool(this.threads);

            httpServer = HttpServer.create(new InetSocketAddress(this.port), 0);
            httpServer.setExecutor(executor);

            // put handler for all groups (home)
            httpServer.createContext("/put", new DataHandler(database));

            // query handlers for the primary group
            httpServer.createContext("/json/dpGetPeriod", new QueryHandler(database, DpGetPeriodResultJSON.class));
            httpServer.createContext("/orcl/dpGetPeriod", new QueryHandler(database, DpGetPeriodResultOracle.class));
            httpServer.createContext("/java/dpGetPeriod", new QueryHandler(database, DpGetPeriodResultJava.class));
            httpServer.createContext("/java/dpGetPeriodChunked", new QueryHandler(database, DpGetPeriodResultJava.class, true));

            //httpServer.createContext("/active", new ActiveHandler(noSQLHome));
            httpServer.start();
        } catch ( Exception ex ) {
            JDebug.StackTrace(Level.SEVERE, ex);
        }
    }
}

