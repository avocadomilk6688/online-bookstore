package com.bookstore.online_bookstore.services;

import com.bookstore.online_bookstore.db.DatabaseManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PricingService {

    // ============================================================
    // CONTEXT LOGIC
    // ============================================================
    private final List<DiscountStrategy> strategies;

    public PricingService() {
        this.strategies = new ArrayList<>();
        
        // Register all available strategies here
        strategies.add(new PremiumDiscountStrategy());
        strategies.add(new StudentDiscountStrategy());
        strategies.add(new GenreDiscountStrategy());
        strategies.add(new BundleDiscountStrategy());
    }

    public double calculateFinalPrice(
            int userID,
            double subtotal,
            String memberType,
            Map<String, Integer> cartItems
    ) {
        double bestDiscount = 0.0;

        System.out.println("Calculating price for User: " + userID + " | Member: " + memberType);

        // Iterate through every strategy and pick the best discount
        for (DiscountStrategy strategy : strategies) {
            double currentDiscount = strategy.calculate(subtotal, userID, memberType, cartItems);
            
            if (currentDiscount > bestDiscount) {
                bestDiscount = currentDiscount;
                System.out.println(" -> Applied: " + strategy.getClass().getSimpleName() + " (Discount: " + currentDiscount + ")");
            }
        }

        return subtotal - bestDiscount;
    }

    // ============================================================
    // STRATEGY PATTERN IMPLEMENTATION (Nested Classes)
    // ============================================================

    // 1. The Strategy Interface
    public interface DiscountStrategy {
        double calculate(double subtotal, int userID, String memberType, Map<String, Integer> cartItems);
    }

    // 2. Concrete Strategy: Premium Member
    public static class PremiumDiscountStrategy implements DiscountStrategy {

        @Override
        public double calculate(double subtotal, int userID, String memberType, Map<String, Integer> cartItems) {
            if (!"PREMIUM".equalsIgnoreCase(memberType)) return 0.0;

            DatabaseManager db = DatabaseManager.getInstance();
            
            String sql = "SELECT percentage FROM discounts WHERE discountType = 'PREMIUM_MEMBER' AND active = 1";
            
            try (ResultSet rs = db.executeQuery(sql)) {
                if (rs != null && rs.next()) {
                    double percent = rs.getDouble("percentage");
                    return subtotal * (percent / 100.0);
                }
            } catch (SQLException e) {
                System.err.println("Error calculating Premium discount: " + e.getMessage());
            }
            return 0.0;
        }
    }

    // 3. Concrete Strategy: Student (Age 7-24)
    public static class StudentDiscountStrategy implements DiscountStrategy {

        @Override
        public double calculate(double subtotal, int userID, String memberType, Map<String, Integer> cartItems) {
            DatabaseManager db = DatabaseManager.getInstance();

            String userSql = "SELECT birthDate FROM users WHERE userID = ?";
            try (ResultSet rs = db.executeQuery(userSql, userID)) {
                if (rs != null && rs.next()) {
                    LocalDate birth = rs.getDate("birthDate").toLocalDate();
                    int age = Period.between(birth, LocalDate.now()).getYears();

                    if (age >= 7 && age <= 24) {
                        String discSql = "SELECT percentage FROM discounts WHERE discountType = 'STUDENT' AND active = 1";
                        try (ResultSet rsDisc = db.executeQuery(discSql)) {
                            if (rsDisc != null && rsDisc.next()) {
                                double percent = rsDisc.getDouble("percentage");
                                return subtotal * (percent / 100.0);
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error calculating Student discount: " + e.getMessage());
            }
            return 0.0;
        }
    }

    // 4. Concrete Strategy: Genre Based
    public static class GenreDiscountStrategy implements DiscountStrategy {

        @Override
        public double calculate(double subtotal, int userID, String memberType, Map<String, Integer> cartItems) {
            DatabaseManager db = DatabaseManager.getInstance();
            double maxDiscount = 0.0;

            for (String isbn : cartItems.keySet()) {
                String bookSql = "SELECT genre FROM books WHERE isbn = ?";
                try (ResultSet rsBook = db.executeQuery(bookSql, isbn)) {
                    if (rsBook != null && rsBook.next()) {
                        String genre = rsBook.getString("genre");
                        if (genre == null) continue;

                        String discSql = "SELECT percentage FROM discounts WHERE discountType = 'BOOK_GENRE' AND targetValue = ? AND active = 1";
                        try (ResultSet rsDisc = db.executeQuery(discSql, genre)) {
                            if (rsDisc != null && rsDisc.next()) {
                                double percent = rsDisc.getDouble("percentage");
                                double currentVal = subtotal * (percent / 100.0);
                                maxDiscount = Math.max(maxDiscount, currentVal);
                            }
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Error calculating Genre discount: " + e.getMessage());
                }
            }
            return maxDiscount;
        }
    }

    // 5. Concrete Strategy: Bundle (3+ items)
    public static class BundleDiscountStrategy implements DiscountStrategy {

        @Override
        public double calculate(double subtotal, int userID, String memberType, Map<String, Integer> cartItems) {
            int totalBooks = cartItems.values().stream().mapToInt(i -> i).sum();
            
            if (totalBooks < 3) return 0.0;

            DatabaseManager db = DatabaseManager.getInstance();
            
            String sql = "SELECT percentage FROM discounts WHERE discountType = 'BUNDLE' AND active = 1";
            try (ResultSet rs = db.executeQuery(sql)) {
                if (rs != null && rs.next()) {
                    double percent = rs.getDouble("percentage");
                    return subtotal * (percent / 100.0);
                }
            } catch (SQLException e) {
                System.err.println("Error calculating Bundle discount: " + e.getMessage());
            }
            return 0.0;
        }
    }
}