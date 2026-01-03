package com.bookstore.online_bookstore.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.bookstore.online_bookstore.db.DatabaseManager;

public class Book {
    private String isbn;
    private String coverImageUrl;
    private String title;
    private String author;
    private String publisher;
    private int publicationYear;
    public String language;
    public int pageCount;
    public String type;
    public String genre;
    public String status;
    public boolean isPromo;

    public Book(String isbn, String coverImageUrl, String title, String author, String publisher, int publicationYear,
            String language, int pageCount, String type, String genre) {
        this.isbn = isbn;
        this.title = title;
        this.coverImageUrl = coverImageUrl;
        this.author = author;
        this.publisher = publisher;
        this.publicationYear = publicationYear;
        this.language = language;
        this.pageCount = pageCount;
        this.type = type;
        this.genre = genre;
        this.status = "Available";
        this.isPromo = false;
    }

    // Getters
    public String getIsbn() {
        return isbn;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getPublisher() {
        return publisher;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public String getLanguage() {
        return language;
    }

    public int getPageCount() {
        return pageCount;
    }

    public String getType() {
        return type;
    }

    public String getGenre() {
        return genre;
    }

    public String getStatus() {
        return status;
    }

    public boolean isPromo() {
        return isPromo;
    }

    // Setters
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPromo(boolean promo) {
        isPromo = promo;
    }

    public void saveBook(Book book) {
        DatabaseManager db = DatabaseManager.getInstance();

        if (db.connect()) {
            String sql = "INSERT INTO books (isbn, coverImageUrl, title, author, publisher, " +
                    "publicationYear, language, pageCount, type, genre, status, isPromo) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            // Mapping Book fields to the DatabaseManager executePrepared method
            db.executePrepared(sql,
                    book.getIsbn(),
                    book.getCoverImageUrl(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getPublisher(),
                    book.getPublicationYear(),
                    book.getLanguage(),
                    book.getPageCount(),
                    book.getType(),
                    book.getGenre(),
                    book.getStatus(),
                    book.isPromo() ? 1 : 0 // Converting boolean to SQLite integer
            );
        }
    }

    // Put this inside your class to reuse the logic
    private Book mapResultSetToBook(ResultSet rs) throws SQLException {
        Book book = new Book(
                rs.getString("isbn"),
                rs.getString("coverImageUrl"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("publisher"),
                rs.getInt("publicationYear"),
                rs.getString("language"),
                rs.getInt("pageCount"),
                rs.getString("type"),
                rs.getString("genre"));
        book.setStatus(rs.getString("status"));
        book.setPromo(rs.getInt("isPromo") == 1);
        return book;
    }

    public List<Book> searchByTitle(String title) {
        List<Book> books = new ArrayList<>();
        DatabaseManager db = DatabaseManager.getInstance();

        // Using % so it finds "Harry" inside "Harry Potter"
        String sql = "SELECT * FROM books WHERE title LIKE ?";

        try (ResultSet rs = db.executeQuery(sql, "%" + title + "%")) {
            while (rs != null && rs.next()) {
                books.add(mapResultSetToBook(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    public List<Book> searchByAuthor(String author) {
        List<Book> books = new ArrayList<>();
        DatabaseManager db = DatabaseManager.getInstance();

        String sql = "SELECT * FROM books WHERE author LIKE ?";

        try (ResultSet rs = db.executeQuery(sql, "%" + author + "%")) {
            while (rs != null && rs.next()) {
                books.add(mapResultSetToBook(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    public Book getBookByIsbn(String isbn) {
        DatabaseManager db = DatabaseManager.getInstance();
        String sql = "SELECT * FROM books WHERE isbn = ?";

        // We pass only the isbn string directly (no % needed for exact match)
        try (ResultSet rs = db.executeQuery(sql, isbn)) {
            if (rs != null && rs.next()) {
                // Reusing the helper method to convert result set to Book object
                return mapResultSetToBook(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Return null if no book with that ISBN exists
    }

    public void updateBook(Book book) {
        DatabaseManager db = DatabaseManager.getInstance();
        if (db.connect()) {
            String sql = "UPDATE books SET title=?, author=?, status=? WHERE isbn=?";
            db.executePrepared(sql, book.getTitle(), book.getAuthor(), book.getStatus(),
                    book.getIsbn());
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
}