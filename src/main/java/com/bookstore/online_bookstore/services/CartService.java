package com.bookstore.online_bookstore.services;

import com.bookstore.online_bookstore.db.DatabaseManager;
import com.bookstore.online_bookstore.model.CartItem;
import com.bookstore.online_bookstore.model.Order;
import com.bookstore.online_bookstore.model.ShoppingCart;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Cart Service - Orchestrates cart operations and checkout flow.
 * Integrates with PricingService for discount calculations.
 */
public class CartService {

    private final PricingService pricingService;

    public CartService() {
        this.pricingService = new PricingService();
    }

    /**
     * Get shopping cart for a user.
     */
    public ShoppingCart getCart(int userID) {
        return ShoppingCart.getOrCreateCart(userID);
    }

    /**
     * Add item to user's cart.
     */
    public void addToCart(int userID, String isbn, int quantity) {
        ShoppingCart cart = ShoppingCart.getOrCreateCart(userID);
        cart.addItem(isbn, quantity);
    }

    /**
     * Remove item from user's cart.
     */
    public void removeFromCart(int userID, String isbn) {
        ShoppingCart cart = ShoppingCart.getOrCreateCart(userID);
        cart.removeItem(isbn);
    }

    /**
     * Update item quantity in user's cart.
     */
    public void updateCartItemQuantity(int userID, String isbn, int quantity) {
        ShoppingCart cart = ShoppingCart.getOrCreateCart(userID);
        cart.updateQuantity(isbn, quantity);
    }

    /**
     * Prepare checkout summary with discount calculation.
     * Uses the existing PricingService to calculate discounts.
     */
    public CheckoutSummary prepareCheckout(int userID) {
        ShoppingCart cart = ShoppingCart.getOrCreateCart(userID);

        if (cart.getItems().isEmpty()) {
            return null;
        }

        double subtotal = cart.getTotalPrice();
        String memberType = getMemberType(userID);
        Map<String, Integer> cartItemsMap = cart.getCartItemsMap();

        // Use PricingService to calculate final price with discounts
        double finalPrice = pricingService.calculateFinalPrice(
                userID,
                subtotal,
                memberType,
                cartItemsMap);

        double discount = subtotal - finalPrice;

        return new CheckoutSummary(
                cart.getItems(),
                subtotal,
                discount,
                finalPrice,
                memberType);
    }

    /**
     * Process checkout and create order.
     */
    public Order processCheckout(int userID, String deliveryAddress, String paymentMethod,
            String cardNumber, String cardHolder, String expiryDate,
            String cvv, String bankCode) {

        ShoppingCart cart = ShoppingCart.getOrCreateCart(userID);

        if (cart.getItems().isEmpty()) {
            return null;
        }

        // Calculate final price with discounts
        CheckoutSummary summary = prepareCheckout(userID);

        // Process payment using Strategy Pattern
        PaymentProcessor processor = new PaymentProcessor();
        PaymentStrategy strategy = PaymentProcessor.createStrategy(paymentMethod);

        if (strategy == null) {
            return null;
        }

        processor.setStrategy(strategy);

        // Build payment details
        PaymentDetails details = new PaymentDetails();
        details.setDeliveryAddress(deliveryAddress);
        details.setCardNumber(cardNumber);
        details.setCardHolder(cardHolder);
        details.setExpiryDate(expiryDate);
        details.setCvv(cvv);
        details.setBankCode(bankCode);

        PaymentResult result = processor.process(summary.getFinalPrice(), details);

        if (!result.isSuccess()) {
            return null;
        }

        // Create order
        Order order = Order.createOrder(
                userID,
                summary.getFinalPrice(),
                deliveryAddress,
                paymentMethod,
                cart.getItems());

        // Clear cart after successful order
        cart.clearCart();

        return order;
    }

    /**
     * Get member type for user.
     */
    private String getMemberType(int userID) {
        DatabaseManager db = DatabaseManager.getInstance();
        String sql = "SELECT memberType FROM users WHERE userID = ?";
        try (ResultSet rs = db.executeQuery(sql, userID)) {
            if (rs != null && rs.next()) {
                return rs.getString("memberType");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching member type: " + e.getMessage());
        }
        return "STANDARD";
    }

    /**
     * Get user orders.
     */
    public List<Order> getUserOrders(int userID) {
        return Order.getOrdersByUser(userID);
    }

    /**
     * Get specific order.
     */
    public Order getOrder(int orderID) {
        return Order.getOrderById(orderID);
    }

    // ============================================================
    // CHECKOUT SUMMARY - Data container for checkout information
    // ============================================================
    public static class CheckoutSummary {
        private final List<CartItem> items;
        private final double subtotal;
        private final double discount;
        private final double finalPrice;
        private final String memberType;

        public CheckoutSummary(List<CartItem> items, double subtotal, double discount,
                double finalPrice, String memberType) {
            this.items = items;
            this.subtotal = subtotal;
            this.discount = discount;
            this.finalPrice = finalPrice;
            this.memberType = memberType;
        }

        public List<CartItem> getItems() {
            return items;
        }

        public double getSubtotal() {
            return subtotal;
        }

        public double getDiscount() {
            return discount;
        }

        public double getFinalPrice() {
            return finalPrice;
        }

        public String getMemberType() {
            return memberType;
        }

        public boolean hasDiscount() {
            return discount > 0;
        }

        public String getDiscountPercentage() {
            if (subtotal == 0)
                return "0%";
            double percent = (discount / subtotal) * 100;
            return String.format("%.1f%%", percent);
        }
    }
}
