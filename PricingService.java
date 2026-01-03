package Pricing;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import db.DatabaseManager;

/**
 * Strategy Pattern Implementation for Discount Management
 * Pricing Service (Production Version)
 */
public class PricingService {

    private final DatabaseManager db;

    public PricingService() {
        this.db = DatabaseManager.getInstance();
    }

    // ============================================================
    // STRATEGY INTERFACE
    // ============================================================

    public interface DiscountStrategy {
        double calculateDiscount(double orderTotal);
        String getDiscountType();
        String getDiscountCode();
    }

    // ============================================================
    // CONCRETE STRATEGIES
    // ============================================================

    public class PremiumMemberDiscount implements DiscountStrategy {

        @Override
        public double calculateDiscount(double orderTotal) {
            return orderTotal * 0.15; // 15%
        }

        @Override
        public String getDiscountType() {
            return "Premium Member";
        }

        @Override
        public String getDiscountCode() {
            return "PREMIUM15";
        }
    }

    public class StudentDiscount implements DiscountStrategy {

        @Override
        public double calculateDiscount(double orderTotal) {
            return orderTotal * 0.10; // 10%
        }

        @Override
        public String getDiscountType() {
            return "Student";
        }

        @Override
        public String getDiscountCode() {
            return "STUDENT10";
        }
    }

    public class BundleDiscount implements DiscountStrategy {

        private static final double MINIMUM_AMOUNT = 100.0;
        private static final double DISCOUNT_AMOUNT = 20.0;

        @Override
        public double calculateDiscount(double orderTotal) {
            return orderTotal >= MINIMUM_AMOUNT ? DISCOUNT_AMOUNT : 0.0;
        }

        @Override
        public String getDiscountType() {
            return "Bundle Discount";
        }

        @Override
        public String getDiscountCode() {
            return "BUNDLE20";
        }
    }

    public class NoDiscount implements DiscountStrategy {

        @Override
        public double calculateDiscount(double orderTotal) {
            return 0.0;
        }

        @Override
        public String getDiscountType() {
            return "No Discount";
        }

        @Override
        public String getDiscountCode() {
            return "NONE";
        }
    }

    // ============================================================
    // CONTEXT METHODS
    // ============================================================

    /**
     * Select the best applicable discount strategy
     */
    public DiscountStrategy getBestDiscount(double orderTotal, String memberType) {

        ArrayList<DiscountStrategy> strategies = new ArrayList<>();

        if ("Premium".equalsIgnoreCase(memberType)) {
            strategies.add(new PremiumMemberDiscount());
        } else if ("Student".equalsIgnoreCase(memberType)) {
            strategies.add(new StudentDiscount());
        }

        // Bundle discount applies to everyone
        strategies.add(new BundleDiscount());

        DiscountStrategy bestStrategy = new NoDiscount();
        double maxDiscount = 0.0;

        for (DiscountStrategy strategy : strategies) {
            double discount = strategy.calculateDiscount(orderTotal);
            if (discount > maxDiscount) {
                maxDiscount = discount;
                bestStrategy = strategy;
            }
        }

        return bestStrategy;
    }

    /**
     * Calculate final price after discount
     */
    public double calculateFinalPrice(double orderTotal, String memberType) {
        DiscountStrategy discount = getBestDiscount(orderTotal, memberType);
        return orderTotal - discount.calculateDiscount(orderTotal);
    }

    /**
     * Simplified checkout method (recommended for UI usage)
     */
    public double calculateCheckoutPrice(double subtotal, String memberType) {
        DiscountStrategy discount = getBestDiscount(subtotal, memberType);
        return subtotal - discount.calculateDiscount(subtotal);
    }

    // ============================================================
    // OPTIONAL: DATABASE-BASED DISCOUNT VIEW
    // ============================================================

    public void showAllDiscountsFromDB() {
        ResultSet rs = db.executeQuery(
            "SELECT * FROM discount_codes WHERE active = 1"
        );

        try {
            while (rs != null && rs.next()) {
                String code = rs.getString("code");
                String type = rs.getString("type");
                double percentage = rs.getDouble("percentage");

                System.out.println(code + " - " + type + " (" + percentage + "%)");
            }
        } catch (SQLException e) {
            System.out.println("No discount codes available.");
        }
    }
}
