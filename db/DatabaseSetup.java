package db;

public class DatabaseSetup {

    public static void main(String[] args) {

        System.out.println("🚀 ONLINE BOOKSTORE - DATABASE SETUP");
        System.out.println("=====================================\n");

        DatabaseManager db = DatabaseManager.getInstance();

        if (!db.connect()) {
            System.err.println("❌ Database connection failed!");
            return;
        }

        // IMPORTANT: Enable foreign keys for SQLite
        db.executeUpdate("PRAGMA foreign_keys = ON");

        createUsersTable(db);
        createBooksTable(db);
        createAdminLogTable(db);
        createShoppingCartTables(db);
        createOrdersTable(db);
        createDiscountCodesTable(db);
        createNotificationsTable(db);

        insertInitialData(db);
        verifySetup(db);

        System.out.println("\n✅ DATABASE SETUP COMPLETED SUCCESSFULLY!");
        db.disconnect();
    }

    // ================= TABLES =================

    private static void createUsersTable(DatabaseManager db) {
        db.executeUpdate("""
            CREATE TABLE IF NOT EXISTS users (
                userID INTEGER PRIMARY KEY AUTOINCREMENT,
                email TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                role TEXT NOT NULL,
                memberType TEXT,
                address TEXT
            )
        """);
    }

    private static void createBooksTable(DatabaseManager db) {
        db.executeUpdate("""
            CREATE TABLE IF NOT EXISTS books (
                isbn TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                author TEXT NOT NULL,
                genre TEXT,
                price REAL NOT NULL,
                stock INTEGER NOT NULL DEFAULT 0
            )
        """);
    }

    private static void createAdminLogTable(DatabaseManager db) {
        db.executeUpdate("""
            CREATE TABLE IF NOT EXISTS admin_log (
                logID INTEGER PRIMARY KEY AUTOINCREMENT,
                adminID INTEGER,
                action TEXT,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (adminID) REFERENCES users(userID)
            )
        """);
    }

    private static void createShoppingCartTables(DatabaseManager db) {
        db.executeUpdate("""
            CREATE TABLE IF NOT EXISTS shopping_cart (
                cartID INTEGER PRIMARY KEY AUTOINCREMENT,
                userID INTEGER,
                totalPrice REAL,
                FOREIGN KEY (userID) REFERENCES users(userID)
            )
        """);

        db.executeUpdate("""
            CREATE TABLE IF NOT EXISTS cart_items (
                cartItemID INTEGER PRIMARY KEY AUTOINCREMENT,
                cartID INTEGER,
                bookID TEXT,
                quantity INTEGER,
                FOREIGN KEY (cartID) REFERENCES shopping_cart(cartID),
                FOREIGN KEY (bookID) REFERENCES books(isbn)
            )
        """);
    }

    private static void createOrdersTable(DatabaseManager db) {
        db.executeUpdate("""
            CREATE TABLE IF NOT EXISTS orders (
                orderID INTEGER PRIMARY KEY AUTOINCREMENT,
                userID INTEGER,
                status TEXT,
                totalPrice REAL,
                date DATETIME DEFAULT CURRENT_TIMESTAMP,
                deliveryAddress TEXT,
                FOREIGN KEY (userID) REFERENCES users(userID)
            )
        """);
    }

    private static void createDiscountCodesTable(DatabaseManager db) {
        db.executeUpdate("""
            CREATE TABLE IF NOT EXISTS discount_codes (
                code TEXT PRIMARY KEY,
                type TEXT,
                percentage REAL,
                active INTEGER DEFAULT 1
            )
        """);
    }

    private static void createNotificationsTable(DatabaseManager db) {
        db.executeUpdate("""
            CREATE TABLE IF NOT EXISTS notifications (
                notificationID INTEGER PRIMARY KEY AUTOINCREMENT,
                userID INTEGER,
                orderID INTEGER,
                message TEXT,
                status TEXT DEFAULT 'unread',
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (userID) REFERENCES users(userID),
                FOREIGN KEY (orderID) REFERENCES orders(orderID)
            )
        """);
    }

    // ================= DATA =================

    private static void insertInitialData(DatabaseManager db) {

        db.executeUpdate("""
            INSERT OR IGNORE INTO users (email, password, role, memberType, address)
            VALUES ('john.doe@example.com','password123','Customer','Premium','KL')
        """);

        db.executeUpdate("""
            INSERT OR IGNORE INTO users (email, password, role, memberType, address)
            VALUES ('jane.smith@example.com','password456','Customer','Student','PJ')
        """);

        db.executeUpdate("""
            INSERT OR IGNORE INTO users (email, password, role)
            VALUES ('admin@bookstore.com','admin123','Admin')
        """);

        db.executeUpdate("""
            INSERT OR IGNORE INTO books VALUES
            ('978-0132350884','Clean Code','Robert C. Martin','Programming',89.90,15),
            ('978-0201633610','Design Patterns','GoF','Software Design',125.00,8)
        """);

        db.executeUpdate("""
            INSERT OR IGNORE INTO discount_codes VALUES
            ('PREMIUM15','Premium Member',15,1),
            ('STUDENT10','Student',10,1),
            ('BUNDLE20','Bundle',20,1)
        """);
    }

    private static void verifySetup(DatabaseManager db) {
        String[] tables = {
            "users","books","admin_log","shopping_cart",
            "cart_items","orders","discount_codes","notifications"
        };

        System.out.println("\n🔍 Database Verification");
        for (String table : tables) {
            System.out.println("✅ " + table + ": " + db.getTableRowCount(table) + " rows");
        }
    }
}
