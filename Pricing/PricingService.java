package Pricing;

import db.DatabaseManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.util.Map;

public class PricingService {

    private final DatabaseManager db;

    public PricingService() {
        this.db = DatabaseManager.getInstance();
    }

    // ============================================================
    // MAIN PRICE CALCULATION
    // ============================================================
    public double calculateFinalPrice(
            int userID,
            double subtotal,
            String memberType,
            Map<String, Integer> cartItems
    ) {

        double bestDiscount = 0.0;

        bestDiscount = Math.max(bestDiscount,
                getPremiumDiscount(subtotal, memberType));

        bestDiscount = Math.max(bestDiscount,
                getStudentDiscount(subtotal, userID));

        bestDiscount = Math.max(bestDiscount,
                getGenreDiscount(subtotal, cartItems));

        bestDiscount = Math.max(bestDiscount,
                getBundleDiscount(subtotal, cartItems));

        return subtotal - bestDiscount;
    }

    // ============================================================
    // PREMIUM MEMBER DISCOUNT
    // ============================================================
    private double getPremiumDiscount(double subtotal, String memberType) {
        if (!"PREMIUM".equalsIgnoreCase(memberType)) return 0.0;
        return getPercentageDiscount("PREMIUM_MEMBER", null, subtotal);
    }

    // ============================================================
    // STUDENT DISCOUNT (AGE 7–24)
    // ============================================================
    private double getStudentDiscount(double subtotal, int userID) {
        if (!isStudentByAge(userID)) return 0.0;
        return getPercentageDiscount("STUDENT", null, subtotal);
    }

    private boolean isStudentByAge(int userID) {

        ResultSet rs = db.executeQuery(
                "SELECT birthDate FROM users WHERE userID = ?", userID
        );

        try {
            if (rs != null && rs.next()) {
                LocalDate birth = rs.getDate("birthDate").toLocalDate();
                int age = Period.between(birth, LocalDate.now()).getYears();
                return age >= 7 && age <= 24;
            }
        } catch (Exception ignored) {}

        return false;
    }

    // ============================================================
    // GENRE DISCOUNT
    // ============================================================
    private double getGenreDiscount(double subtotal, Map<String, Integer> cartItems) {

        double maxDiscount = 0.0;

        for (String isbn : cartItems.keySet()) {
            String genre = getBookGenre(isbn);
            if (genre == null) continue;

            double discount = getPercentageDiscount(
                    "BOOK_GENRE", genre, subtotal
            );

            maxDiscount = Math.max(maxDiscount, discount);
        }

        return maxDiscount;
    }

    // ============================================================
    // BUNDLE DISCOUNT (≥ 3 BOOKS)
    // ============================================================
    private double getBundleDiscount(double subtotal, Map<String, Integer> cartItems) {

        int totalBooks = cartItems.values().stream().mapToInt(i -> i).sum();
        if (totalBooks < 3) return 0.0;

        return getPercentageDiscount("BUNDLE", null, subtotal);
    }

    // ============================================================
    // CORE DISCOUNT FETCH
    // ============================================================
    private double getPercentageDiscount(
            String discountType,
            String targetValue,
            double subtotal
    ) {

        ResultSet rs = db.executeQuery("""
            SELECT percentage FROM discounts
            WHERE discountType = ?
            AND active = 1
            AND (targetValue IS NULL OR targetValue = ?)
        """, discountType, targetValue);

        try {
            if (rs != null && rs.next()) {
                return subtotal * (rs.getDouble("percentage") / 100.0);
            }
        } catch (SQLException ignored) {}

        return 0.0;
    }

    private String getBookGenre(String isbn) {

        ResultSet rs = db.executeQuery(
                "SELECT genre FROM books WHERE isbn = ?", isbn
        );

        try {
            return (rs != null && rs.next()) ? rs.getString("genre") : null;
        } catch (SQLException e) {
            return null;
        }
    }
}
