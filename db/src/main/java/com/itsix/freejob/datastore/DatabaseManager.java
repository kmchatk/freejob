package com.itsix.freejob.datastore;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseManager {
    void registerDatabase(String databaseName, String parameters)
            throws SQLException;

    Connection getConnection(String databaseName) throws SQLException;

    void releaseConnection(Connection connection);
}
