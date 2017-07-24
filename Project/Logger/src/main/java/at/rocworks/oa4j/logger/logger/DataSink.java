/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.rocworks.oa4j.logger.logger;

import at.rocworks.oa4j.logger.base.NoSQLGroup;
import at.rocworks.oa4j.logger.base.NoSQLGroupPinned;
import at.rocworks.oa4j.logger.base.NoSQLGroupRobin;
import at.rocworks.oa4j.logger.base.NoSQLLogger;
import at.rocworks.oa4j.logger.base.NoSQLServer;
import at.rocworks.oa4j.logger.base.NoSQLSettings;
import at.rocworks.oa4j.logger.data.Metrics;
import at.rocworks.oa4j.base.JDebug;
import java.util.logging.Level;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method; 


/**
 *
 * @author vogler
 */
public class DataSink /*implements IDataCollector*/ {

    private final NoSQLSettings settings;
    private final NoSQLLogger logger = new NoSQLLogger();

    public DataSink(NoSQLSettings settings) {
        this.settings = settings;
    }

    public void start() {
        createGroups();
        logger.startNoSQLGroups();
    }

    public Metrics getStats() {
        return logger.getStats();
    }

    private void createGroups() {
        // Logging Groups
        JDebug.out.info("logging groups...");
        int gcount;
        String gprimary = settings.getStringProperty("logger", "primary", "");
        String sgroups = settings.getStringProperty("logger", "groups", "");
        try {
            JSONArray jgroups = (JSONArray) JSONValue.parse(sgroups);
            gcount = jgroups.size();
            for (int j = 0; j < gcount; j++) {
                if (createGroup(settings, jgroups.get(j).toString())) {
                    if (gprimary.isEmpty()) {
                        gprimary = jgroups.get(j).toString();
                    }
                }
            }
            JDebug.out.log(Level.CONFIG, "primary={0}", gprimary);
            logger.setReadGroup(gprimary);
        } catch (java.lang.ClassCastException ex) {
            JDebug.out.log(Level.SEVERE, "not a valid json group string '{0} [{1}]'!", new Object[]{sgroups, ex.toString()});
        }
    }

    private boolean createGroup(NoSQLSettings configs, String grpprefix) {
        NoSQLGroup group = null;
        boolean grun = configs.getBoolProperty(grpprefix, "run", false);
        int scount = configs.getIntProperty(grpprefix, "servers", 1);
        String name = configs.getStringProperty(grpprefix, "name", grpprefix);
        
        JDebug.out.log(Level.INFO, "loading group {0} run={1} servers={2} name={3}", new Object[]{grpprefix, grun, scount, name});
        if (grun) {
            switch (configs.getDistribution()) {
                case ROBIN:
                    group = new NoSQLGroupRobin(name, scount);
                    break;
                case PINNED:
                    group = new NoSQLGroupPinned(name, scount);
                    break;
                default:
                    JDebug.out.log(Level.SEVERE, "invalid distribution type {0}", configs.getDistribution());
            }
        }

        if (!grun || group == null) {
            return false;
        } else {
            logger.addNoSQLGroup(group);
        }

        for (int i = 0; i < scount; i++) {
            try {
                String srvprefix = grpprefix + ".server." + i;
                String type = configs.getStringProperty(srvprefix, "type", "");
                boolean srun = configs.getBoolProperty(srvprefix, "run", grun);
                if ( !srun ) continue;
                NoSQLSettings srvcfg;
                try {
                    //srvcfg = configs.clone();
                    srvcfg = configs.cloneWithNewPath(srvprefix);
                } catch (CloneNotSupportedException ex) {
                    JDebug.StackTrace(Level.SEVERE, ex);
                    return false;
                }
                //srvcfg.readProperties(srvprefix);
                JDebug.out.log(Level.CONFIG, "section {0}...", type);
                switch (type) {
//                    case "com.etm.dbs.nosql.NoSQLCassandra":
//                        {
//                            NoSQLServer srv = com.etm.dbs.nosql.NoSQLCassandra.createServer(srvcfg, srvprefix);
//                            group.setNoSQLServer(i, srv);
//                            break;
//                        }
//                    case "com.etm.dbs.nosql.NoSQLMongoDB":
//                        {
//                            NoSQLServer srv = com.etm.dbs.nosql.NoSQLMongoDB.createServer(srvcfg, srvprefix);
//                            group.setNoSQLServer(i, srv);
//                            break;
//                        }
//                    case "com.etm.dbs.nosql.NoSQLElasticSearch":
//                        {
//                            NoSQLServer srv = com.etm.dbs.nosql.NoSQLElasticSearch.createServer(srvcfg, srvprefix);
//                            group.setNoSQLServer(i, srv);
//                            break;
//                        }                    
                    default:
                        {
                            Class<?> clazz = Class.forName(type);
                            Method create = clazz.getMethod("createServer", NoSQLSettings.class, String.class);
                            NoSQLServer srv = (NoSQLServer)create.invoke(null, srvcfg, srvprefix);
                            group.setNoSQLServer(i, srv);
                            break;
                        }
                }
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                JDebug.StackTrace(Level.SEVERE, ex);
            }
        }
        return true;
    }

    public NoSQLLogger getLogger() {
        return logger;
    }
}
