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

        dropTables(db);

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

        insertBooks(db);
        insertTestData(db);

        verifySetup(db);

        db.disconnect();
        System.out.println("\n✅ DATABASE SETUP COMPLETED SUCCESSFULLY!");
    }

    // ================= USERS =================
    private static void createUsersTable(DatabaseManager db) {
        db.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS users (
                        userID INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT,-- Added for the Profile feature
                        email TEXT UNIQUE NOT NULL,
                        password TEXT NOT NULL,
                        role TEXT CHECK(role IN ('ADMIN','GUEST','MEMBER')) NOT NULL,
                        memberType TEXT CHECK(memberType IN ('STANDARD','PREMIUM')),
                        birthDate DATE,
                        address TEXT
                    )
                """);
    }

    private static void dropTables(DatabaseManager db) {
        String[] tables = { "admin_log", "notifications", "discounts", "payments", "order_items", "orders",
                "cart_items", "shopping_cart", "books", "users" };
        for (String table : tables) {
            db.executeUpdate("DROP TABLE IF EXISTS " + table);
        }
    }

    // ================= BOOKS =================
    private static void createBooksTable(DatabaseManager db) {
        db.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS books (
                        bookID INTEGER PRIMARY KEY AUTOINCREMENT,
                        bookID TEXT UNIQUE,
                        coverImageUrl TEXT,
                        title TEXT NOT NULL,
                        author TEXT NOT NULL,
                        price REAL NOT NULL,
                        publisher TEXT,
                        publicationYear INTEGER,
                        language TEXT,
                        pageCount INTEGER,
                        type TEXT,
                        genre TEXT,
                        status TEXT,
                        isPromo INTEGER DEFAULT 0
                    )
                """);
    }

    private static void insertBooks(DatabaseManager db) {
        System.out.println("📥 Inserting books...");

        String sql = """
                    INSERT INTO books (
                        bookID, coverImageUrl, title, author, price, publisher,
                        publicationYear, language, pageCount, type, genre, status, isPromo
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        db.executePrepared(sql, "979-8745274824", "/images/gatsby.jpg", "The Great Gatsby", "F. Scott Fitzgerald",
                68.00,
                "Independently published", 2021, "English", 110, "Hardcover", "Classic Novel", "Available", 0);

        db.executePrepared(sql, "978-1408855652", "/images/hp1.jpg", "Harry Potter and the Philosopher's Stone",
                "J.K. Rowling", 62.90,
                "Bloomsbury Publishing Plc", 2014, "English", 223, "Paperback", "Fantasy Novel", "Available", 0);

        db.executePrepared(sql, "978-1612680194", "/images/richdad.jpg", "Rich Dad Poor Dad", "Robert Kiyosaki", 25.94,
                "Warner Books", 2017, "English", 336, "Paperback", "Personal Finance", "Available", 0);

        db.executePrepared(sql, "978-0735211292", "/images/habits.jpg", "Atomic Habits", "James Clear", 75.00,
                "Penguin", 2018, "English", 320, "Hardcover", "Self-Help", "Available", 0);

        db.executePrepared(sql, "978-0134686097", "/images/java.jpg", "Effective Java", "Joshua Bloch", 268.00,
                "Addison-Wesley", 2017, "English", 412, "Paperback", "Programming", "Available", 0);

        db.executePrepared(sql, "978-1593275990", "/images/python.jpg", "Automate the Boring Stuff with Python",
                "Al Sweigart", 95.00,
                "No Starch Press", 2019, "English", 504, "Paperback", "Programming", "Available", 0);

        db.executePrepared(sql, "978-0062316097", "/images/sapiens.png", "Sapiens: A Brief History of Humankind",
                "Yuval Noah Harari", 85.00,
                "Harper", 2011, "English", 443, "Paperback", "History/Science", "Available", 0);

        System.out.println("✅ Books inserted! IDs generated automatically.");
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
                        bookID TEXT NOT NULL,
                        quantity INTEGER NOT NULL,
                        FOREIGN KEY (cartID) REFERENCES shopping_cart(cartID),
                        FOREIGN KEY (bookID) REFERENCES books(bookID)
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
                        bookID TEXT NOT NULL,
                        quantity INTEGER NOT NULL,
                        price REAL NOT NULL,
                        FOREIGN KEY (orderID) REFERENCES orders(orderID),
                        FOREIGN KEY (bookID) REFERENCES books(bookID)
                    )
                """);
    }

    // ================= PAYMENTS =================
    private static void createPaymentsTable(DatabaseManager db) {
        db.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS payments (
                        paymentID INTEGER PRIMARY KEY AUTOINCREMENT,
                        orderID INTEGER UNIQUE NOT NULL,
                        method TEXT CHECK(method IN ('COD', 'FPX', 'CREDIT_CARD')) NOT NULL,
                        amount REAL NOT NULL,
                        paymentDate DATETIME DEFAULT CURRENT_TIMESTAMP,
                        paymentStatus TEXT CHECK(paymentStatus IN ('PENDING', 'COMPLETED', 'FAILED')) DEFAULT 'PENDING',
                        transactionRef TEXT,
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

    private static void insertTestData(DatabaseManager db) {
        System.out.println("📥 Inserting test users...");

        // Create an Admin user
        db.addUser("admin@bookstore.com", "admin123", "ADMIN", "System Admin", "PREMIUM", "1985-05-20", "Admin Office");

        // Create a basic Member user for testing
        db.addUser("member@test.com", "pass123", "MEMBER", "Test Member", "STANDARD", "1995-10-10", "456 Library St");

        // Note: To test discounts, register new users via the registration page:
        // - Select PREMIUM membership type for 15% discount
        // - Enter birthdate between age 7-24 for student discount (10%)
        // - Add 3+ books to cart for bundle discount (12%)

        // Insert discount configurations
        insertDiscounts(db);
    }

    private static void insertDiscounts(DatabaseManager db) {
        System.out.println("📥 Inserting discount configurations...");

        String sql = "INSERT INTO discounts (discountType, targetValue, percentage, active) VALUES (?, ?, ?, 1)";

        // Premium member discount - 15%
        db.executePrepared(sql, "PREMIUM_MEMBER", null, 15.0);

        // Student discount (age 7-24) - 10%
        db.executePrepared(sql, "STUDENT", null, 10.0);

        // Bundle discount (3+ books) - 12%
        db.executePrepared(sql, "BUNDLE", null, 12.0);

        // Genre-based discounts
        db.executePrepared(sql, "BOOK_GENRE", "Programming", 8.0);
        db.executePrepared(sql, "BOOK_GENRE", "Self-Help", 5.0);

        System.out.println("✅ Discount configurations inserted!");
    }

    // ================= VERIFY =================
    private static void verifySetup(DatabaseManager db) {
        String[] tables = {
                "users", "books", "shopping_cart", "cart_items",
                "orders", "order_items", "payments",
                "discounts", "notifications", "admin_log"
        };

        System.out.println("🔍 Database Verification");
        for (String table : tables) {
            System.out.println("✅ " + table + ": " + db.getTableRowCount(table) + " rows");
        }
    }
}
