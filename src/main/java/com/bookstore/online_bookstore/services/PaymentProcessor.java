package com.bookstore.online_bookstore.services;

/**
 * Payment Strategy Pattern Implementation
 * 
 * Follows Open/Closed Principle - new payment methods can be added
 * without modifying existing code.
 */

// ============================================================
// 1. PAYMENT DETAILS - Data container for payment information
// ============================================================
class PaymentDetails {
    private String cardNumber;
    private String cardHolder;
    private String expiryDate;
    private String cvv;
    private String bankCode;
    private String deliveryAddress;

    public PaymentDetails() {
    }

    // Card payment details
    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardHolder() {
        return cardHolder;
    }

    public void setCardHolder(String cardHolder) {
        this.cardHolder = cardHolder;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    // FPX payment details
    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    // Common
    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }
}

// ============================================================
// 2. PAYMENT RESULT - Result of payment processing
// ============================================================
class PaymentResult {
    private boolean success;
    private String message;
    private String transactionRef;

    public PaymentResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public PaymentResult(boolean success, String message, String transactionRef) {
        this.success = success;
        this.message = message;
        this.transactionRef = transactionRef;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getTransactionRef() {
        return transactionRef;
    }
}

// ============================================================
// 3. STRATEGY INTERFACE
// ============================================================
interface PaymentStrategy {
    PaymentResult processPayment(double amount, PaymentDetails details);

    String getPaymentMethod();

    String getDisplayName();
}

// ============================================================
// 4. CONCRETE STRATEGY: Cash on Delivery
// ============================================================
class CashOnDeliveryPayment implements PaymentStrategy {

    @Override
    public PaymentResult processPayment(double amount, PaymentDetails details) {
        // COD requires delivery address validation
        if (details.getDeliveryAddress() == null || details.getDeliveryAddress().trim().isEmpty()) {
            return new PaymentResult(false, "Delivery address is required for Cash on Delivery");
        }

        // COD is always successful at checkout - payment collected on delivery
        String ref = "COD-" + System.currentTimeMillis();
        return new PaymentResult(true, "Order placed. Payment will be collected upon delivery.", ref);
    }

    @Override
    public String getPaymentMethod() {
        return "COD";
    }

    @Override
    public String getDisplayName() {
        return "Cash on Delivery";
    }
}

// ============================================================
// 5. CONCRETE STRATEGY: FPX Online Banking
// ============================================================
class FPXPayment implements PaymentStrategy {

    @Override
    public PaymentResult processPayment(double amount, PaymentDetails details) {
        // Validate bank selection
        if (details.getBankCode() == null || details.getBankCode().trim().isEmpty()) {
            return new PaymentResult(false, "Please select a bank for FPX payment");
        }

        // Simulate FPX processing
        String ref = "FPX-" + System.currentTimeMillis();
        return new PaymentResult(true, "FPX payment successful via " + getBankName(details.getBankCode()), ref);
    }

    private String getBankName(String bankCode) {
        return switch (bankCode) {
            case "MBB" -> "Maybank";
            case "CIMB" -> "CIMB Bank";
            case "PBB" -> "Public Bank";
            case "RHB" -> "RHB Bank";
            case "HLB" -> "Hong Leong Bank";
            case "AMBANK" -> "AmBank";
            case "BIMB" -> "Bank Islam";
            default -> bankCode;
        };
    }

    @Override
    public String getPaymentMethod() {
        return "FPX";
    }

    @Override
    public String getDisplayName() {
        return "FPX Online Banking";
    }
}

// ============================================================
// 6. CONCRETE STRATEGY: Credit/Debit Card
// ============================================================
class CreditCardPayment implements PaymentStrategy {

    @Override
    public PaymentResult processPayment(double amount, PaymentDetails details) {
        // Validate card details
        if (!validateCardNumber(details.getCardNumber())) {
            return new PaymentResult(false, "Invalid card number");
        }

        if (details.getCardHolder() == null || details.getCardHolder().trim().isEmpty()) {
            return new PaymentResult(false, "Cardholder name is required");
        }

        if (!validateExpiry(details.getExpiryDate())) {
            return new PaymentResult(false, "Invalid or expired card");
        }

        if (!validateCvv(details.getCvv())) {
            return new PaymentResult(false, "Invalid CVV");
        }

        // Simulate card payment processing
        String maskedCard = maskCardNumber(details.getCardNumber());
        String ref = "CARD-" + System.currentTimeMillis();
        return new PaymentResult(true, "Payment successful. Card ending: " + maskedCard, ref);
    }

    private boolean validateCardNumber(String cardNumber) {
        if (cardNumber == null)
            return false;
        String cleaned = cardNumber.replaceAll("\\s+", "");
        return cleaned.length() >= 13 && cleaned.length() <= 19 && cleaned.matches("\\d+");
    }

    private boolean validateExpiry(String expiry) {
        if (expiry == null || !expiry.matches("\\d{2}/\\d{2}"))
            return false;
        // Basic validation - in production, check if not expired
        return true;
    }

    private boolean validateCvv(String cvv) {
        if (cvv == null)
            return false;
        return cvv.matches("\\d{3,4}");
    }

    private String maskCardNumber(String cardNumber) {
        String cleaned = cardNumber.replaceAll("\\s+", "");
        return cleaned.substring(cleaned.length() - 4);
    }

    @Override
    public String getPaymentMethod() {
        return "CREDIT_CARD";
    }

    @Override
    public String getDisplayName() {
        return "Credit/Debit Card";
    }
}

// ============================================================
// 7. CONTEXT CLASS - PaymentProcessor
// ============================================================
public class PaymentProcessor {
    private PaymentStrategy strategy;

    public PaymentProcessor() {
    }

    public PaymentProcessor(PaymentStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(PaymentStrategy strategy) {
        this.strategy = strategy;
    }

    public PaymentResult process(double amount, PaymentDetails details) {
        if (strategy == null) {
            return new PaymentResult(false, "No payment method selected");
        }
        return strategy.processPayment(amount, details);
    }

    public String getPaymentMethod() {
        return strategy != null ? strategy.getPaymentMethod() : null;
    }

    /**
     * Factory method to create appropriate strategy based on method code.
     */
    public static PaymentStrategy createStrategy(String method) {
        return switch (method) {
            case "COD" -> new CashOnDeliveryPayment();
            case "FPX" -> new FPXPayment();
            case "CREDIT_CARD" -> new CreditCardPayment();
            default -> null;
        };
    }
}
