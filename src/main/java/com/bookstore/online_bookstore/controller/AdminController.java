package com.bookstore.online_bookstore.controller;

import com.bookstore.online_bookstore.db.DatabaseManager;
import com.bookstore.online_bookstore.model.Book;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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
    public String adminDashboard(Model model, HttpSession session) {
        if (session.getAttribute("adminID") == null) {
            return "redirect:/login";
        }
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
    public String manageBook(@RequestParam(value = "query", required = false) String query, Model model,
            HttpSession session) {
        if (session.getAttribute("adminID") == null) {
            return "redirect:/login";
        }
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
    public String editBook(@PathVariable("id") int id, Model model, HttpSession session) {
        if (session.getAttribute("adminID") == null) {
            return "redirect:/login";
        }

        Book bookHelper = new Book();
        Book existingBook = bookHelper.getBookById(id);

        // This fills the form at the bottom with the book's current info
        model.addAttribute("bookForm", existingBook);

        model.addAttribute("allBooks", null);

        return "manage_book";
    }

    // Add the DELETE method
    @GetMapping("/admin/books/delete/{id}")
    public String deleteBook(@PathVariable int id, HttpSession session) {
        if (session.getAttribute("adminID") == null) {
            return "redirect:/login";
        }

        Integer currentAdminId = (Integer) session.getAttribute("adminID");
        if (currentAdminId == null)
            currentAdminId = 1;

        Book bookHelper = new Book();
        bookHelper.deleteBook(id);

        logAction(currentAdminId, "Removed Book ID: " + id); // <--- Use session ID

        return "redirect:/admin/books";
    }

    @PostMapping("/admin/books/add")
    public String saveBook(@ModelAttribute("bookForm") Book book,
            @RequestParam("file") MultipartFile file,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (session.getAttribute("adminID") == null) {
            return "redirect:/login";
        }

        Integer currentAdminId = (Integer) session.getAttribute("adminID");
        if (currentAdminId == null) {
            currentAdminId = 1;
        }

        // --- NEW: Physical File Upload Logic ---
        if (!file.isEmpty()) {
            try {
                String fileName = file.getOriginalFilename();
                // Define where to save the image
                String uploadDir = "src/main/resources/static/images/";
                Path uploadPath = Paths.get(uploadDir);

                // Create the directory if it doesn't exist
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Save the file to the folder
                try (InputStream inputStream = file.getInputStream()) {
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                }

                // Update the book object with the path for the database
                book.setCoverImageUrl("/images/" + fileName);

            } catch (IOException e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("errorMsg", "Failed to upload image.");
            }
        }

        Book bookHelper = new Book();

        if (book.getBookID() > 0) {
            bookHelper.updateBook(book);
            logAction(currentAdminId, "Modified Book: " + book.getTitle());
            redirectAttributes.addFlashAttribute("successMsg", "Book updated successfully!");
        } else {
            bookHelper.addBook(book);
            logAction(currentAdminId, "Added New Book: " + book.getTitle());
            redirectAttributes.addFlashAttribute("successMsg", "New book added successfully!");
        }

        return "redirect:/admin/books";
    }

    // ============================================================
    // 3. UPDATE ORDER PAGE
    // ============================================================
    @GetMapping("/admin/orders")
    public String updateOrderGet(HttpSession session) {
        if (session.getAttribute("adminID") == null) {
            return "redirect:/login";
        }
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
                // 2. Fetch User Details
                int userID = rsOrder.getInt("userID");
                String userSql = "SELECT name, email FROM users WHERE userID = ?";
                ResultSet rsUser = db.executeQuery(userSql, userID);

                String customerName = "Unknown";
                String userEmail = "Unknown";

                if (rsUser != null && rsUser.next()) {
                    customerName = rsUser.getString("name");
                    userEmail = rsUser.getString("email");
                }

                // 3. Fetch Order Items (FIXED SQL QUERY)
                // We MUST add books.isbn here so the result set contains it!
                String itemsSql = "SELECT order_items.quantity, order_items.price, books.title, books.isbn, books.bookID "
                        +
                        "FROM order_items JOIN books ON order_items.isbn = books.isbn " +
                        "WHERE order_items.orderID = ?";
                ResultSet rsItems = db.executeQuery(itemsSql, orderId);

                List<Map<String, Object>> itemsList = new ArrayList<>();
                while (rsItems != null && rsItems.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("title", rsItems.getString("title"));
                    item.put("isbn", rsItems.getString("isbn")); // Use "isbn" as the key
                    item.put("bookID", rsItems.getInt("bookID")); // Internal ID
                    item.put("quantity", rsItems.getInt("quantity"));
                    item.put("price", rsItems.getDouble("price"));
                    itemsList.add(item);
                }

                // Construct Order Object for Thymeleaf
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
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "update-order";
    }

    @PostMapping("/admin/orders/update")
    public String updateOrderStatus(@RequestParam int orderID, @RequestParam String status, HttpSession session) {
        if (session.getAttribute("adminID") == null) {
            return "redirect:/login";
        }
        
        Integer currentAdminId = (Integer) session.getAttribute("adminID");
        if (currentAdminId == null)
            currentAdminId = 1;

        DatabaseManager db = DatabaseManager.getInstance();
        String updateSql = "UPDATE orders SET status = ? WHERE orderID = ?";
        db.executePrepared(updateSql, status, orderID);

        // Use the session ID here
        logAction(currentAdminId, "Updated Order #" + orderID + " to " + status);

        // 3. TRIGGER OBSERVER (Notifications)
        // Need the UserID to send the notification
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
    private void logAction(int adminID, String action) {
        DatabaseManager db = DatabaseManager.getInstance();
        String sql = "INSERT INTO admin_log (adminID, action) VALUES (?, ?)";
        db.executePrepared(sql, adminID, action);
        System.out.println("DEBUG: Log saved for Admin " + adminID + " - " + action);
    }
}
