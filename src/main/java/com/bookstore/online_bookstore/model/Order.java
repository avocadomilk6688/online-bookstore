package com.bookstore.online_bookstore.model;

import com.bookstore.online_bookstore.db.DatabaseManager;
import com.bookstore.online_bookstore.services.OrderSubject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Order model with database operations.
 * Integrates with OrderSubject (Observer Pattern) for notifications.
 */
public class Order {
    private int orderID;
    private int userID;
    private String status;
    private double totalPrice;
    private String orderDate;
    private String deliveryAddress;
    private String paymentMethod;
    private List<CartItem> items;

    public Order() {
        this.items = new ArrayList<>();
    }

    public Order(int orderID, int userID, String status, double totalPrice,
            String orderDate, String deliveryAddress) {
        this.orderID = orderID;
        this.userID = userID;
        this.status = status;
        this.totalPrice = totalPrice;
        this.orderDate = orderDate;
        this.deliveryAddress = deliveryAddress;
        this.items = new ArrayList<>();
    }

    /**
     * Create a new order from cart items.
     * Triggers Observer pattern notification on creation.
     */
    public static Order createOrder(int userID, double totalPrice, String deliveryAddress,
            String paymentMethod, List<CartItem> cartItems) {
        DatabaseManager db = DatabaseManager.getInstance();

        // Insert order
        String orderSql = "INSERT INTO orders (userID, status, totalPrice, deliveryAddress) VALUES (?, 'PLACED', ?, ?)";
        db.executePrepared(orderSql, userID, totalPrice, deliveryAddress);

        // Get the newly created order ID
        int orderID = -1;
        String getIdSql = "SELECT MAX(orderID) as orderID FROM orders WHERE userID = ?";
        try (ResultSet rs = db.executeQuery(getIdSql, userID)) {
            if (rs != null && rs.next()) {
                orderID = rs.getInt("orderID");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching order ID: " + e.getMessage());
            return null;
        }

        // Insert order items
        String itemSql = "INSERT INTO order_items (orderID, isbn, quantity, price) VALUES (?, ?, ?, ?)";
        for (CartItem item : cartItems) {
            db.executePrepared(itemSql, orderID, item.getIsbn(), item.getQuantity(), item.getPrice());
        }

        // Insert payment record
        String paymentSql = "INSERT INTO payments (orderID, method, amount, paymentStatus) VALUES (?, ?, ?, ?)";
        String paymentStatus = "COD".equals(paymentMethod) ? "PENDING" : "COMPLETED";
        db.executePrepared(paymentSql, orderID, paymentMethod, totalPrice, paymentStatus);

        // Trigger Observer Pattern notification
        OrderSubject orderSubject = new OrderSubject(orderID, userID);
        orderSubject.setStatus("PLACED");

        Order order = new Order();
        order.setOrderID(orderID);
        order.setUserID(userID);
        order.setStatus("PLACED");
        order.setTotalPrice(totalPrice);
        order.setDeliveryAddress(deliveryAddress);
        order.setPaymentMethod(paymentMethod);
        order.setItems(cartItems);

        return order;
    }

    /**
     * Get all orders for a specific user.
     */
    public static List<Order> getOrdersByUser(int userID) {
        List<Order> orders = new ArrayList<>();
        DatabaseManager db = DatabaseManager.getInstance();

        String sql = "SELECT * FROM orders WHERE userID = ? ORDER BY orderDate DESC";
        try (ResultSet rs = db.executeQuery(sql, userID)) {
            while (rs != null && rs.next()) {
                Order order = new Order(
                        rs.getInt("orderID"),
                        rs.getInt("userID"),
                        rs.getString("status"),
                        rs.getDouble("totalPrice"),
                        rs.getString("orderDate"),
                        rs.getString("deliveryAddress"));
                order.loadItems();
                order.loadPaymentMethod();
                orders.add(order);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching orders: " + e.getMessage());
        }

        return orders;
    }

    /**
     * Get a specific order by ID.
     */
    public static Order getOrderById(int orderID) {
        DatabaseManager db = DatabaseManager.getInstance();

        String sql = "SELECT * FROM orders WHERE orderID = ?";
        try (ResultSet rs = db.executeQuery(sql, orderID)) {
            if (rs != null && rs.next()) {
                Order order = new Order(
                        rs.getInt("orderID"),
                        rs.getInt("userID"),
                        rs.getString("status"),
                        rs.getDouble("totalPrice"),
                        rs.getString("orderDate"),
                        rs.getString("deliveryAddress"));
                order.loadItems();
                order.loadPaymentMethod();
                return order;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching order: " + e.getMessage());
        }

        return null;
    }

    /**
     * Load order items from database.
     */
    public void loadItems() {
        this.items.clear();
        DatabaseManager db = DatabaseManager.getInstance();

        String sql = """
                    SELECT oi.isbn, oi.quantity, oi.price, b.title, b.author, b.coverImageUrl
                    FROM order_items oi
                    JOIN books b ON oi.isbn = b.isbn
                    WHERE oi.orderID = ?
                """;

        try (ResultSet rs = db.executeQuery(sql, this.orderID)) {
            while (rs != null && rs.next()) {
                CartItem item = new CartItem(
                        rs.getString("isbn"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getDouble("price"),
                        rs.getInt("quantity"),
                        rs.getString("coverImageUrl"));
                this.items.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Error loading order items: " + e.getMessage());
        }
    }

    /**
     * Load payment method from database.
     */
    private void loadPaymentMethod() {
        DatabaseManager db = DatabaseManager.getInstance();
        String sql = "SELECT method FROM payments WHERE orderID = ?";
        try (ResultSet rs = db.executeQuery(sql, this.orderID)) {
            if (rs != null && rs.next()) {
                this.paymentMethod = rs.getString("method");
            }
        } catch (SQLException e) {
            System.err.println("Error loading payment method: " + e.getMessage());
        }
    }

    /**
     * Get display-friendly payment method name.
     */
    public String getPaymentMethodDisplay() {
        if (paymentMethod == null)
            return "Unknown";
        return switch (paymentMethod) {
            case "COD" -> "Cash on Delivery";
            case "FPX" -> "FPX Online Banking";
            case "CREDIT_CARD" -> "Credit/Debit Card";
            default -> paymentMethod;
        };
    }

    /**
     * Get display-friendly status.
     */
    public String getStatusDisplay() {
        if (status == null)
            return "Unknown";
        return switch (status) {
            case "PLACED" -> "Order Placed";
            case "SHIPPED" -> "Shipped";
            case "OUT_FOR_DELIVERY" -> "Out for Delivery";
            case "DELIVERED" -> "Delivered";
            default -> status;
        };
    }

    // Getters and Setters
    public int getOrderID() {
        return orderID;
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }
}
