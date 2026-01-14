package com.bookstore.online_bookstore.controller;

import com.bookstore.online_bookstore.db.DatabaseManager;
import com.bookstore.online_bookstore.model.Book;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AdminController {

    // ============================================================
    // 1. ADMIN DASHBOARD
    // ============================================================
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        DatabaseManager db = DatabaseManager.getInstance();

        // A. Data for the UI (Name, ID, Email)
        // In a real app, fetch this from the logged-in session. Here we set defaults.
        model.addAttribute("adminName", "Admin User");
        model.addAttribute("adminId", "ADM001");
        model.addAttribute("adminEmail", "admin@bookstore.com");

        // B. Data for Logs (Optional - if you want to display them somewhere else)
        List<String> logs = new ArrayList<>();
        String logSql = "SELECT * FROM admin_log ORDER BY timestamp DESC LIMIT 20";

        try (ResultSet rs = db.executeQuery(logSql)) {
            while (rs != null && rs.next()) {
                String entry = "Admin ID: " + rs.getInt("adminID") +
                        " | Action: " + rs.getString("action") +
                        " | Time: " + rs.getString("timestamp");
                logs.add(entry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        model.addAttribute("adminLogs", logs);
        return "admin-dashboard";
    }

    // ============================================================
    // 2. MANAGE BOOK PAGE
    // ============================================================
    @GetMapping("/admin/books")
    public String manageBook(@RequestParam(value = "query", required = false) String query, Model model) {
        if (query != null && !query.trim().isEmpty()) {
            Book bookHelper = new Book();
            model.addAttribute("allBooks", bookHelper.searchBooks(query));
            model.addAttribute("currentQuery", query);
        } else {
            model.addAttribute("allBooks", null); // Keeps the table hidden
        }
        model.addAttribute("bookForm", new Book());
        return "manage_book";
    }

    @GetMapping("/admin/books/edit/{id}")
    public String editBook(@PathVariable("id") int id, Model model) {
        Book bookHelper = new Book();
        Book existingBook = bookHelper.getBookById(id);

        // This fills the form at the bottom with the book's current info
        model.addAttribute("bookForm", existingBook);

        // We keep allBooks as null so the search results disappear and
        // you can focus on editing the form.
        model.addAttribute("allBooks", null);

        return "manage_book";
    }

    // Add the DELETE method
    @GetMapping("/admin/books/delete/{id}")
    public String deleteBook(@PathVariable int id) {
        Book bookHelper = new Book();
        bookHelper.deleteBook(id);

        logAction("Removed Book ID: " + id);

        return "redirect:/admin/books";
    }

    @PostMapping("/admin/books/add")
    public String saveBook(@ModelAttribute("bookForm") Book book,
            @RequestParam("file") MultipartFile file) {

        // Handle the image upload
        if (!file.isEmpty()) {
            book.setCoverImageUrl("/images/" + file.getOriginalFilename());
        }

        Book bookHelper = new Book();

        // If bookID is > 0, it means the book already exists in the DB
        if (book.getBookID() > 0) {
            bookHelper.updateBook(book); // Uses your 'updateBook' method in Book.java
        } else {
            bookHelper.addBook(book); // Adds a brand new record
        }

        return "redirect:/admin/books";
    }

    // ============================================================
    // 3. UPDATE ORDER PAGE
    // ============================================================
    @GetMapping("/admin/orders")
    public String updateOrderGet() {
        return "update-order";
    }

    @PostMapping("/admin/orders")
    public String fetchOrder(@RequestParam("orderId") int orderId, Model model) {
        DatabaseManager db = DatabaseManager.getInstance();

        try {
            // 1. Fetch Order Details
            String orderSql = "SELECT * FROM orders WHERE orderID = ?";
            ResultSet rsOrder = db.executeQuery(orderSql, orderId);

            if (rsOrder != null && rsOrder.next()) {
                // 2. Fetch User Details (Customer Name & Email)
                int userID = rsOrder.getInt("userID");
                String userSql = "SELECT name, email FROM users WHERE userID = ?"; // Assuming 'name' column exists in
                                                                                   // users
                ResultSet rsUser = db.executeQuery(userSql, userID);

                String customerName = "Unknown";
                String userEmail = "Unknown";

                if (rsUser != null && rsUser.next()) {
                    customerName = rsUser.getString("name"); // Adjust column name if needed (e.g. 'fullName')
                    userEmail = rsUser.getString("email");
                }

                // 3. Fetch Order Items
                String itemsSql = "SELECT order_items.quantity, order_items.price, books.title, books.isbn " +
                        "FROM order_items JOIN books ON order_items.isbn = books.isbn " +
                        "WHERE order_items.orderID = ?";
                ResultSet rsItems = db.executeQuery(itemsSql, orderId);

                List<Map<String, Object>> itemsList = new ArrayList<>();
                while (rsItems != null && rsItems.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("title", rsItems.getString("title"));
                    item.put("isbn", rsItems.getString("isbn"));
                    item.put("quantity", rsItems.getInt("quantity"));
                    item.put("price", rsItems.getDouble("price"));
                    itemsList.add(item);
                }

                // Construct Order Object (Map) for Thymeleaf
                Map<String, Object> order = new HashMap<>();
                order.put("orderID", rsOrder.getInt("orderID"));
                order.put("customerName", customerName);
                order.put("userEmail", userEmail);
                order.put("totalPrice", rsOrder.getDouble("totalPrice"));
                order.put("deliveryAddress", rsOrder.getString("deliveryAddress"));
                order.put("orderDate", rsOrder.getString("orderDate"));
                order.put("status", rsOrder.getString("status"));
                order.put("items", itemsList);

                model.addAttribute("order", order);
            } else {
                // Order not found logic could go here
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "update-order";
    }

    @PostMapping("/admin/orders/update")
    public String updateOrderStatus(@RequestParam int orderID, @RequestParam String status) {
        DatabaseManager db = DatabaseManager.getInstance();

        // 1. Update the Status in DB
        String updateSql = "UPDATE orders SET status = ? WHERE orderID = ?";
        db.executePrepared(updateSql, status, orderID);

        // 2. LOG THE ACTION (Admin Log)
        logAction("Updated Order #" + orderID + " to " + status);

        // 3. TRIGGER OBSERVER (Notifications)
        // We need the UserID to send the notification.
        // Let's fetch the user ID first to simulate the Observer update.
        String getUserIdSql = "SELECT userID FROM orders WHERE orderID = ?";
        ResultSet rs = db.executeQuery(getUserIdSql, orderID);
        try {
            if (rs != null && rs.next()) {
                int userID = rs.getInt("userID");

                // Manually create the notification (Simulating Observer Pattern in Controller)
                // Since the Controller orchestrates the flow, it can act as the trigger here.
                String message = "Order #" + orderID + " status updated to: " + status;
                String notifySql = "INSERT INTO notifications (userID, orderID, message, status) VALUES (?, ?, ?, 'UNREAD')";
                db.executePrepared(notifySql, userID, orderID, message);

                System.out.println("✅ [Observer Pattern] Notification sent for Order " + orderID);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "redirect:/admin/dashboard";
    }

    // ============================================================
    // 4. NOTIFICATIONS (Existing)
    // ============================================================
    @GetMapping("/notifications")
    public String viewNotifications(@RequestParam("userID") int userID, Model model) {
        DatabaseManager db = DatabaseManager.getInstance();
        List<String> notifications = new ArrayList<>();

        String sql = "SELECT * FROM notifications WHERE userID = ? AND status = 'UNREAD' ORDER BY createdAt DESC";

        try (ResultSet rs = db.executeQuery(sql, userID)) {
            while (rs != null && rs.next()) {
                String note = "Order #" + rs.getInt("orderID") + ": " +
                        rs.getString("message") +
                        " (" + rs.getString("createdAt") + ")";
                notifications.add(note);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        model.addAttribute("notifications", notifications);
        return "notifications";
    }

    // ============================================================
    // HELPER: LOG ADMIN ACTION
    // ============================================================
    private void logAction(String action) {
        DatabaseManager db = DatabaseManager.getInstance();
        // Assuming Admin ID 1 is the current admin. In a real app, get this from
        // Security Context.
        int currentAdminId = 1;
        String sql = "INSERT INTO admin_log (adminID, action) VALUES (?, ?)";
        db.executePrepared(sql, currentAdminId, action);
    }
}