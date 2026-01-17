package com.bookstore.online_bookstore.model;

/**
 * Represents an item in the shopping cart.
 * Simple data container for cart item details.
 */
public class CartItem {
    private String isbn;
    private String title;
    private String author;
    private double price;
    private int quantity;
    private String coverImageUrl;

    public CartItem() {
    }

    public CartItem(String isbn, String title, String author, double price, int quantity, String coverImageUrl) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.price = price;
        this.quantity = quantity;
        this.coverImageUrl = coverImageUrl;
    }

    public double getSubtotal() {
        return price * quantity;
    }

    // Getters and Setters
    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }
}
