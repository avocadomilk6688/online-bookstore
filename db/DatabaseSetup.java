package com.bookstore.online_bookstore.db;

public class DatabaseSetup {

    public static void main(String[] args) {

        System.out.println("🚀 ONLINE BOOKSTORE - DATABASE SETUP");
        System.out.println("=====================================\n");

        DatabaseManager db = DatabaseManager.getInstance();

        if (!db.connect()) {
            System.err.println("❌ Database connection failed!");
            return;
        }

        db.executeUpdate("PRAGMA foreign_keys = ON");

        createUsersTable(db);
        createBooksTable(db);
        createShoppingCartTables(db);
        createOrdersTable(db);
        createOrderItemsTable(db);
        createPaymentsTable(db);
        createDiscountsTable(db);
        createNotificationsTable(db);
        createAdminLogTable(db);

        verifySetup(db);

        db.disconnect();
        System.out.println("\n✅ DATABASE SETUP COMPLETED SUCCESSFULLY!");
    }

    // ================= USERS =================
    private static void createUsersTable(DatabaseManager db) {
        db.executeUpdate("""
            CREATE TABLE IF NOT EXISTS users (
                userID INTEGER PRIMARY KEY AUTOINCREMENT,
                email TEXT UNIQUE,
                password TEXT,
                role TEXT CHECK(role IN ('ADMIN','GUEST','MEMBER')) NOT NULL,
                memberType TEXT CHECK(memberType IN ('STANDARD','PREMIUM')),
                birthDate DATE,
                address TEXT
            )
        """);
    }

    // ================= BOOKS =================
    private static void createBooksTable(DatabaseManager db) {
        db.executeUpdate("""
            CREATE TABLE IF NOT EXISTS books (
                isbn TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                author TEXT NOT NULL,
                genre TEXT NOT NULL,
                description TEXT,
                price REAL NOT NULL,
                stock INTEGER NOT NULL,
                publishDate DATE,
                createdAt DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """);
    }

    // ================= SHOPPING CART =================
    private static void createShoppingCartTables(DatabaseManager db) {

        db.executeUpdate("""
            CREATE TABLE IF NOT EXISTS shopping_cart (
                cartID INTEGER PRIMARY KEY AUTOINCREMENT,
                userID INTEGER UNIQUE,
                totalPrice REAL DEFAULT 0,
                FOREIGN KEY (userID) REFERENCES users(userID)
            )
        """);

        db.executeUpdate("""
            CREATE TABLE IF NOT EXISTS cart_items (
                cartItemID INTEGER PRIMARY KEY AUTOINCREMENT,
                cartID INTEGER NOT NULL,
                isbn TEXT NOT NULL,
                quantity INTEGER NOT NULL,
                FOREIGN KEY (cartID) REFERENCES shopping_cart(cartID),
                FOREIGN KEY (isbn) REFERENCES books(isbn)
            )
        """);
    }

    // ================= ORDERS =================
    private static void createOrdersTable(DatabaseManager db) {
        db.executeUpdate("""
            CREATE TABLE IF NOT EXISTS orders (
                orderID INTEGER PRIMARY KEY AUTOINCREMENT,
                userID INTEGER NOT NULL,
                status TEXT CHECK(
                    status IN ('PLACED','SHIPPED','OUT_FOR_DELIVERY','DELIVERED')
                ) NOT NULL DEFAULT 'PLACED',
                totalPrice REAL NOT NULL,
                orderDate DATETIME DEFAULT CURRENT_TIMESTAMP,
                deliveryAddress TEXT NOT NULL,
                FOREIGN KEY (userID) REFERENCES users(userID)
            )
        """);
    }

    // ================= ORDER ITEMS =================
    private static void createOrderItemsTable(DatabaseManager db) {
        db.executeUpdate("""
            CREATE TABLE IF NOT EXISTS order_items (
                orderItemID INTEGER PRIMARY KEY AUTOINCREMENT,
                orderID INTEGER NOT NULL,
                isbn TEXT NOT NULL,
                quantity INTEGER NOT NULL,
                price REAL NOT NULL,
                FOREIGN KEY (orderID) REFERENCES orders(orderID),
                FOREIGN KEY (isbn) REFERENCES books(isbn)
            )
        """);
    }

    // ================= PAYMENTS (COD ONLY) =================
    private static void createPaymentsTable(DatabaseManager db) {
        db.executeUpdate("""
            CREATE TABLE IF NOT EXISTS payments (
                paymentID INTEGER PRIMARY KEY AUTOINCREMENT,
                orderID INTEGER UNIQUE NOT NULL,
                method TEXT CHECK(method = 'COD') NOT NULL,
                amount REAL NOT NULL,
                paymentDate DATETIME,
                FOREIGN KEY (orderID) REFERENCES orders(orderID)
            )
        """);
    }

    // ================= DISCOUNTS =================
    private static void createDiscountsTable(DatabaseManager db) {
        db.executeUpdate("""
            CREATE TABLE IF NOT EXISTS discounts (
                discountID INTEGER PRIMARY KEY AUTOINCREMENT,
                discountType TEXT CHECK(
                    discountType IN ('PREMIUM_MEMBER','STUDENT','BUNDLE','BOOK_GENRE')
                ) NOT NULL,
                targetValue TEXT,
                percentage REAL NOT NULL,
                active INTEGER DEFAULT 1
            )
        """);
    }

    // ================= NOTIFICATIONS =================
    private static void createNotificationsTable(DatabaseManager db) {
        db.executeUpdate("""
            CREATE TABLE IF NOT EXISTS notifications (
                notificationID INTEGER PRIMARY KEY AUTOINCREMENT,
                userID INTEGER NOT NULL,
                orderID INTEGER NOT NULL,
                message TEXT NOT NULL,
                status TEXT CHECK(status IN ('UNREAD','READ')) DEFAULT 'UNREAD',
                createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (userID) REFERENCES users(userID),
                FOREIGN KEY (orderID) REFERENCES orders(orderID)
            )
        """);
    }

    // ================= ADMIN LOG =================
    private static void createAdminLogTable(DatabaseManager db) {
        db.executeUpdate("""
            CREATE TABLE IF NOT EXISTS admin_log (
                logID INTEGER PRIMARY KEY AUTOINCREMENT,
                adminID INTEGER NOT NULL,
                action TEXT NOT NULL,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (adminID) REFERENCES users(userID)
            )
        """);
    }

    // ================= VERIFY =================
    private static void verifySetup(DatabaseManager db) {
        String[] tables = {
            "users","books","shopping_cart","cart_items",
            "orders","order_items","payments",
            "discounts","notifications","admin_log"
        };

        System.out.println("🔍 Database Verification");
        for (String table : tables) {
            System.out.println("✅ " + table + ": " + db.getTableRowCount(table) + " rows");
        }
    }
}
