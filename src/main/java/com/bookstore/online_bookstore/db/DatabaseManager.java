package com.bookstore.online_bookstore.db;

import java.sql.*;

/**
 * DatabaseManager
 * Singleton class to manage SQLite database connection
 */
public class DatabaseManager {

    private static DatabaseManager instance;
    private static final String DB_NAME = "online_bookstore.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_NAME;

    private Connection connection;

    private DatabaseManager() {}

    // ============================================================
    // SINGLETON
    // ============================================================
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // ============================================================
    // CONNECTION
    // ============================================================
    public boolean connect() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);

                // IMPORTANT: Enable foreign keys for SQLite
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON");
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
            return false;
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("❌ Error closing database: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        return connection;
    }

    // ============================================================
    // BASIC EXECUTION
    // ============================================================
    public void executeUpdate(String sql) {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("❌ SQL Update Failed: " + e.getMessage());
        }
    }

    // ============================================================
    // PREPARED STATEMENT
    // ============================================================
    public void executePrepared(String sql, Object... params) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Prepared SQL Failed: " + e.getMessage());
        }
    }

    // ============================================================
    // QUERY
    // ============================================================
    public ResultSet executeQuery(String sql, Object... params) {
        try {
            PreparedStatement ps = connection.prepareStatement(sql);

            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            return ps.executeQuery();
        } catch (SQLException e) {
            System.err.println("❌ Query Failed: " + e.getMessage());
            return null;
        }
    }

    public int getTableRowCount(String tableName) {
        String sql = "SELECT COUNT(*) FROM " + tableName;

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            return rs.next() ? rs.getInt(1) : 0;

        } catch (SQLException e) {
            return -1;
        }
    }

    // ============================================================
    // UTILITY
    // ============================================================
    public static String getDbFile() {
        return DB_NAME;
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return connection.prepareStatement(sql);
    }
}
