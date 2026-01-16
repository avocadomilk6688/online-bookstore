package com.bookstore.online_bookstore.db;

import com.bookstore.online_bookstore.model.Admin;
import com.bookstore.online_bookstore.model.Customer;
import com.bookstore.online_bookstore.model.User;

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
    private User loggedInUser; // store session user

    private DatabaseManager() {
    }

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
        // FORCE a connection check here!
        if (!connect()) {
            System.err.println("❌ Cannot execute query: Connection failed.");
            return null;
        }

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
    // ADD USER
    // ============================================================
    public void addUser(String email, String password, String role, String name,
            String memberType, String birthDate, String address) {

        String sql = """
                INSERT INTO users (email, password, role, name, memberType, birthDate, address)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        if (!connect()) return;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);
            ps.setString(3, role); // ADMIN / GUEST / MEMBER
            ps.setString(4, name);
            ps.setString(5, memberType); // STANDARD / PREMIUM
            ps.setString(6, birthDate); // DATE
            ps.setString(7, address);

            ps.executeUpdate();

            System.out.println("✔ New user registered: " + email);

        } catch (SQLException e) {
            System.err.println("❌ Registration Error: " + e.getMessage());
        }
    }

    // ADD CUSTOMER (shortcut)
    public void addCustomer(String email, String password, String name, String memberType, String address) {
        addUser(email, password, "MEMBER", name, memberType, null, address);
    }
    
    // ============================================================
    // CHECK EMAIL EXISTS
    // ============================================================
    public boolean isEmailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        if (!connect()) return false;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Check Email Error: " + e.getMessage());
        }
        return false;
    }

    // ============================================================
    // GET USER BY EMAIL
    // ============================================================
    public User getUserByEmail(String email) {

        if (!connect()) return null;
        
        String sql = "SELECT * FROM users WHERE email = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            //ResultSet rs = ps.executeQuery();

            //String role = rs.getString("role");

            //if (role.equals("ADMIN")) {
               // return new Admin(
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) { // CRITICAL: Move cursor to the first result
                String role = rs.getString("role");
                if ("ADMIN".equals(role)) {
                    return new Admin(rs.getInt("userID"), rs.getString("name"), rs.getString("email"), rs.getString("password"));
                } else {
                    Customer c = new Customer(
                        rs.getInt("userID"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("memberType"),
                        rs.getString("birthDate"),
                        rs.getString("address"));
                        return c;
             }
        }
    }
        } catch (SQLException e) {
            System.err.println("❌ getUserByEmail Error: " + e.getMessage());
        }
        return null;
    }

    // SET LOGGED IN USER
    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
    }

    // ============================================================
    // GET LOGGED IN CUSTOMER (optional)
    // ============================================================
    public Customer getLoggedInCustomer() {
        if (loggedInUser instanceof Customer) {
            return (Customer) loggedInUser;
        }
        return null;
    }

    // ============================================================
    // UPDATE CUSTOMER PROFILE
    // ============================================================
    public void updateCustomerProfile(String email, String name, String birthDate, String address) {
        String sql = "UPDATE users SET name = ?, birthDate = ?, address = ? WHERE email = ?";
    
        if (!connect()) return;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, birthDate);
            ps.setString(3, address);
            ps.setString(4, email);
        
            ps.executeUpdate();
            System.out.println("✔ Profile updated for: " + email);
        } catch (SQLException e) {
            System.err.println("❌ Update Profile Error: " + e.getMessage());
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
