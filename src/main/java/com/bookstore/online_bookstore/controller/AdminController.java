package com.bookstore.online_bookstore.controller;

import com.bookstore.online_bookstore.db.DatabaseManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class AdminController {

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        DatabaseManager db = DatabaseManager.getInstance();
        
        // 1. Fetch Admin Logs from DB
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
        return "admin-dashboard"; // This maps to admin-dashboard.html
    }
    
    @GetMapping("/notifications")
    public String viewNotifications(@RequestParam("userID") int userID, Model model) {
        DatabaseManager db = DatabaseManager.getInstance();
        List<String> notifications = new ArrayList<>();
        
        // Fetch unread notifications for this user
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
        return "notifications"; // This maps to notifications.html
    }
}