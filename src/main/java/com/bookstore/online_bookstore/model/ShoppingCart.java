package com.bookstore.online_bookstore.model;

import com.bookstore.online_bookstore.db.DatabaseManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shopping Cart model with database operations.
 * Manages cart items and integrates with the database layer.
 */
public class ShoppingCart {
    private int cartID;
    private int userID;
    private double totalPrice;
    private List<CartItem> items;

    public ShoppingCart() {
        this.items = new ArrayList<>();
    }

    public ShoppingCart(int cartID, int userID) {
        this.cartID = cartID;
        this.userID = userID;
        this.items = new ArrayList<>();
    }

    /**
     * Get or create a shopping cart for the specified user.
     */
    public static ShoppingCart getOrCreateCart(int userID) {
        DatabaseManager db = DatabaseManager.getInstance();

        String selectSql = "SELECT cartID FROM shopping_cart WHERE userID = ?";
        try (ResultSet rs = db.executeQuery(selectSql, userID)) {
            if (rs != null && rs.next()) {
                int cartID = rs.getInt("cartID");
                ShoppingCart cart = new ShoppingCart(cartID, userID);
                cart.loadItems();
                return cart;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching cart: " + e.getMessage());
        }

        // Create new cart if none exists
        String insertSql = "INSERT INTO shopping_cart (userID, totalPrice) VALUES (?, 0)";
        db.executePrepared(insertSql, userID);

        // Fetch the newly created cartID
        try (ResultSet rs = db.executeQuery(selectSql, userID)) {
            if (rs != null && rs.next()) {
                return new ShoppingCart(rs.getInt("cartID"), userID);
            }
        } catch (SQLException e) {
            System.err.println("Error creating cart: " + e.getMessage());
        }

        return new ShoppingCart();
    }

    /**
     * Load all items in this cart from the database.
     */
    public void loadItems() {
        this.items.clear();
        DatabaseManager db = DatabaseManager.getInstance();

        String sql = """
                    SELECT ci.isbn, ci.quantity, b.title, b.author, b.price, b.coverImageUrl
                    FROM cart_items ci
                    JOIN books b ON ci.isbn = b.isbn
                    WHERE ci.cartID = ?
                """;

        try (ResultSet rs = db.executeQuery(sql, this.cartID)) {
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
            System.err.println("Error loading cart items: " + e.getMessage());
        }

        updateTotalPrice();
    }

    /**
     * Add item to cart or update quantity if already exists.
     */
    public void addItem(String isbn, int quantity) {
        DatabaseManager db = DatabaseManager.getInstance();

        // Check if item already exists in cart
        String checkSql = "SELECT quantity FROM cart_items WHERE cartID = ? AND isbn = ?";
        try (ResultSet rs = db.executeQuery(checkSql, this.cartID, isbn)) {
            if (rs != null && rs.next()) {
                int currentQty = rs.getInt("quantity");
                String updateSql = "UPDATE cart_items SET quantity = ? WHERE cartID = ? AND isbn = ?";
                db.executePrepared(updateSql, currentQty + quantity, this.cartID, isbn);
            } else {
                String insertSql = "INSERT INTO cart_items (cartID, isbn, quantity) VALUES (?, ?, ?)";
                db.executePrepared(insertSql, this.cartID, isbn, quantity);
            }
        } catch (SQLException e) {
            System.err.println("Error adding item to cart: " + e.getMessage());
        }

        loadItems();
    }

    /**
     * Remove item from cart.
     */
    public void removeItem(String isbn) {
        DatabaseManager db = DatabaseManager.getInstance();
        String sql = "DELETE FROM cart_items WHERE cartID = ? AND isbn = ?";
        db.executePrepared(sql, this.cartID, isbn);
        loadItems();
    }

    /**
     * Update item quantity in cart.
     */
    public void updateQuantity(String isbn, int quantity) {
        if (quantity <= 0) {
            removeItem(isbn);
            return;
        }

        DatabaseManager db = DatabaseManager.getInstance();
        String sql = "UPDATE cart_items SET quantity = ? WHERE cartID = ? AND isbn = ?";
        db.executePrepared(sql, quantity, this.cartID, isbn);
        loadItems();
    }

    /**
     * Clear all items from the cart.
     */
    public void clearCart() {
        DatabaseManager db = DatabaseManager.getInstance();
        String sql = "DELETE FROM cart_items WHERE cartID = ?";
        db.executePrepared(sql, this.cartID);
        this.items.clear();
        this.totalPrice = 0;

        String updateSql = "UPDATE shopping_cart SET totalPrice = 0 WHERE cartID = ?";
        db.executePrepared(updateSql, this.cartID);
    }

    /**
     * Calculate and update the total price.
     */
    private void updateTotalPrice() {
        this.totalPrice = 0;
        for (CartItem item : items) {
            this.totalPrice += item.getSubtotal();
        }

        DatabaseManager db = DatabaseManager.getInstance();
        String sql = "UPDATE shopping_cart SET totalPrice = ? WHERE cartID = ?";
        db.executePrepared(sql, this.totalPrice, this.cartID);
    }

    /**
     * Get cart items as a map for PricingService integration.
     * Map key: ISBN, Value: quantity
     */
    public Map<String, Integer> getCartItemsMap() {
        Map<String, Integer> map = new HashMap<>();
        for (CartItem item : items) {
            map.put(item.getIsbn(), item.getQuantity());
        }
        return map;
    }

    /**
     * Get total number of items in cart.
     */
    public int getItemCount() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    // Getters and Setters
    public int getCartID() {
        return cartID;
    }

    public void setCartID(int cartID) {
        this.cartID = cartID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public List<CartItem> getItems() {
        return items;
    }
}
