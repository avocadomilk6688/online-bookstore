package com.bookstore.online_bookstore.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.bookstore.online_bookstore.db.DatabaseManager;

public class Book {
    private int bookID;
    private String isbn;
    private String coverImageUrl;
    private String title;
    private String author;
    private double price;
    private String publisher;
    private int publicationYear;
    private String language;
    private int pageCount;
    private String type;
    private String genre;
    private String status;
    private boolean isPromo;

    public Book() {
    }

    // Constructor for creating a Book from Database
    public Book(int bookID, String isbn, String coverImageUrl, String title, String author, double price,
            String publisher,
            int publicationYear, String language, int pageCount, String type, String genre, String status,
            boolean isPromo) {
        this.bookID = bookID;
        this.isbn = isbn;
        this.coverImageUrl = coverImageUrl;
        this.title = title;
        this.author = author;
        this.price = price;
        this.publisher = publisher;
        this.publicationYear = publicationYear;
        this.language = language;
        this.pageCount = pageCount;
        this.type = type;
        this.genre = genre;
        this.status = status;
        this.isPromo = isPromo;
    }

    private Book mapResultSetToBook(ResultSet rs) throws SQLException {
        return new Book(
                rs.getInt("bookID"),
                rs.getString("isbn"),
                rs.getString("coverImageUrl"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getDouble("price"),
                rs.getString("publisher"),
                rs.getInt("publicationYear"),
                rs.getString("language"),
                rs.getInt("pageCount"),
                rs.getString("type"),
                rs.getString("genre"),
                rs.getString("status"),
                rs.getInt("isPromo") == 1);
    }

    public void addBook(Book book) {
        DatabaseManager db = DatabaseManager.getInstance();
        if (db.connect()) {
            String sql = "INSERT INTO books (isbn, coverImageUrl, title, author, price, publisher, " +
                    "publicationYear, language, pageCount, type, genre, status, isPromo) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            db.executePrepared(sql,
                    book.getIsbn(),
                    book.getCoverImageUrl(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getPrice(),
                    book.getPublisher(),
                    book.getPublicationYear(),
                    book.getLanguage(),
                    book.getPageCount(),
                    book.getType(),
                    book.getGenre(),
                    book.getStatus(),
                    book.isPromo() ? 1 : 0);
        }
    }

    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        DatabaseManager db = DatabaseManager.getInstance();
        String sql = "SELECT * FROM books";
        try (ResultSet rs = db.executeQuery(sql)) {
            while (rs != null && rs.next()) {
                books.add(mapResultSetToBook(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    public List<Book> searchBooks(String query) {
        List<Book> books = new ArrayList<>();
        DatabaseManager db = DatabaseManager.getInstance();

        // Searches across three different columns at once
        String sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? OR isbn LIKE ?";
        String fuzzyQuery = "%" + query + "%";

        // DatabaseManager handles the connection check automatically
        try (ResultSet rs = db.executeQuery(sql, fuzzyQuery, fuzzyQuery, fuzzyQuery)) {
            while (rs != null && rs.next()) {
                books.add(mapResultSetToBook(rs));
            }
        } catch (SQLException e) {
            System.err.println("Unified Search Failed: " + e.getMessage());
            e.printStackTrace();
        }
        return books;
    }

    public List<Book> getBooksByGenre(String genre) {
        List<Book> books = new ArrayList<>();
        DatabaseManager db = DatabaseManager.getInstance();
        // Search the actual 'genre' column
        String sql = "SELECT * FROM books WHERE genre = ?";
        try (ResultSet rs = db.executeQuery(sql, genre)) {
            while (rs != null && rs.next()) {
                books.add(mapResultSetToBook(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    public Book getBookById(int id) {
        DatabaseManager db = DatabaseManager.getInstance();
        String sql = "SELECT * FROM books WHERE bookID = ?";
        try (ResultSet rs = db.executeQuery(sql, id)) {
            if (rs != null && rs.next()) {
                return mapResultSetToBook(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateBook(Book book) {
        DatabaseManager db = DatabaseManager.getInstance();
        if (db.connect()) {
            String sql = "UPDATE books SET title=?, author=?, price=?, status=?, isPromo=? WHERE bookID=?";
            db.executePrepared(sql,
                    book.getTitle(),
                    book.getAuthor(),
                    book.getPrice(),
                    book.getStatus(),
                    book.isPromo() ? 1 : 0,
                    book.getBookID());
        }
    }

    public void deleteBook(int id) {
        DatabaseManager db = DatabaseManager.getInstance();
        if (db.connect()) {
            String sql = "DELETE FROM books WHERE bookID = ?";
            db.executePrepared(sql, id);
        }
    }

    public int getBookID() {
        return bookID;
    }

    public void setBookID(int bookID) {
        this.bookID = bookID;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
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

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isPromo() {
        return isPromo;
    }

    public void setPromo(boolean promo) {
        isPromo = promo;
    }
}