package com.itsix.freejob.datastore.internal;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Context;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.log4j.xml.DOMConfigurator;
import org.h2.jdbcx.JdbcConnectionPool;
import org.osgi.framework.BundleContext;

import com.itsix.freejob.datastore.DatabaseManager;

@Component(publicFactory = false)
@Instantiate
@Provides
public class DatabaseManagerProvider implements DatabaseManager {

    private Map<String, JdbcConnectionPool> pools = new HashMap<>();

    @Context
    BundleContext context;

    @Validate
    private void validate() {
        DOMConfigurator.configure(context.getBundle().getResource("log4j.xml"));
    }

    @Override
    synchronized public void registerDatabase(String databaseName,
            String parameters) throws java.sql.SQLException {
        //        logService.log(LogService.LOG_DEBUG,
        //                "Registering database " + databaseName);
        if (pools.containsKey(databaseName)) {
            return;
        }

        File dbdir = new File("data");
        if (!dbdir.exists()) {
            //            logService.log(LogService.LOG_DEBUG,
            //                    "Creating /data dir in " + dbdir.getAbsolutePath());
            dbdir.mkdirs();
        }

        String url = "jdbc:h2:data/" + databaseName;
        String user = "admin";
        String password = "admin";

        JdbcConnectionPool connectionPool = JdbcConnectionPool.create(url, user,
                password);
        try {
            java.sql.Connection c = connectionPool.getConnection();
            c.close();
        } catch (java.sql.SQLException e) {
            throw e;
        }
        pools.put(databaseName, connectionPool);
    }

    @Override
    synchronized public Connection getConnection(String databaseName)
            throws SQLException {
        JdbcConnectionPool pool = pools.get(databaseName);
        return pool == null ? null : pool.getConnection();
    }

    @Override
    synchronized public void releaseConnection(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (Exception e) {
            //            logService.log(LogService.LOG_WARNING,
            //                    "Could not release connection: ", e);
        }
    }
}
