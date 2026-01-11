package com.bookstore.online_bookstore.services;

import com.bookstore.online_bookstore.db.DatabaseManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// ============================================================
// 1. THE OBSERVER INTERFACE
// ============================================================
interface OrderObserver {
    void update(int orderID, int userID, String status);
}

// ============================================================
// 2. THE CONCRETE OBSERVER (Database Notifier)
// ============================================================
class DatabaseNotificationObserver implements OrderObserver {

    @Override
    public void update(int orderID, int userID, String status) {
        DatabaseManager db = DatabaseManager.getInstance();
        
        String message = "Order #" + orderID + " status updated to: " + status;
        
        // Insert into the 'notifications' table created in DatabaseSetup
        String sql = "INSERT INTO notifications (userID, orderID, message, status) VALUES (?, ?, ?, 'UNREAD')";
        
        db.executePrepared(sql, userID, orderID, message);
        System.out.println("✅ [Observer] Notification saved to DB for User " + userID);
    }
}

// ============================================================
// 3. THE SUBJECT (Order) - Integration Logic
// ============================================================
// NOTE: This class is a helper that demonstrates how to attach the observer.
// Developer C will use this logic inside their actual Order class.
public class OrderSubject {
    
    private int orderID;
    private int userID;
    private String status;
    
    // List of observers listening to this order
    private final List<OrderObserver> observers = new ArrayList<>();

    public OrderSubject(int orderID, int userID) {
        this.orderID = orderID;
        this.userID = userID;
        // Automatically attach the database observer when a new order is created
        this.attach(new DatabaseNotificationObserver());
    }

    // Attach an observer to this order
    public void attach(OrderObserver observer) {
        observers.add(observer);
    }

    // This is the method Developer C will call when updating status
    public void setStatus(String status) {
        this.status = status;
        notifyObservers();
    }

    private void notifyObservers() {
        for (OrderObserver observer : observers) {
            observer.update(this.orderID, this.userID, this.status);
        }
    }
    
    // Getters
    public String getStatus() { return status; }
    public int getUserID() { return userID; }
}