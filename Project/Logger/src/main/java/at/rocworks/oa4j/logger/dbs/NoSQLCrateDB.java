package at.rocworks.oa4j.logger.dbs;

import at.rocworks.oa4j.base.JDebug;

import at.rocworks.oa4j.logger.base.NoSQLServer;
import at.rocworks.oa4j.logger.base.NoSQLSettings;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

public class NoSQLCrateDB extends NoSQLJDBC {
    /*
        CREATE TABLE EVENTHISTORY (
        tag string NOT NULL,
        ts TIMESTAMP NOT NULL,
        value_number double INDEX OFF,
        value_string string INDEX OFF,
        value_timestamp TIMESTAMP INDEX OFF,
        status long INDEX OFF,
        manager integer INDEX OFF,
        user_ integer INDEX OFF,
        primary key (tag, ts)
        );
         */
    public static NoSQLServer createServer(NoSQLSettings srvcfg, String srvprefix) {
        String url = srvcfg.getStringProperty(srvprefix, "url", "jdbc:mysql://localhost/pvss");
        String usr = srvcfg.getStringProperty(srvprefix, "username", "pvssrdb");
        String pwd = srvcfg.getStringProperty(srvprefix, "password", "manager");
        String grp = srvcfg.getStringProperty(srvprefix, "archive", "EVENT");
        JDebug.out.log(Level.CONFIG, "{0}.type: {1}\n{2}\nurl: {3}",
                new Object[]{srvprefix, NoSQLMySQL.class.getName(), srvcfg, url});
        return new NoSQLMySQL(srvcfg, url, usr, pwd, grp);
    }

    public NoSQLCrateDB(NoSQLSettings settings, String url, String username, String password, String archive) {
        super(settings, "io.crate.client.jdbc.CrateDriver", url, username, password, archive);
    }

    @Override
    protected void afterInsert(Connection conn) throws SQLException {
    }
}
