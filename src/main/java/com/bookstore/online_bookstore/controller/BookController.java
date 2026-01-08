package com.bookstore.online_bookstore.controller;
import com.bookstore.online_bookstore.model.Book;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

public class BookController {
    @GetMapping("/books")
    public String viewBooks(Model model) {
        // Get data from SQLite
        Book bookHelper = new Book();
        List<Book> allBooks = bookHelper.getAllBooks(); 
        
        // Sends the list to HTML page
        model.addAttribute("books", allBooks);
        
        return "books"; // This opens books.html
    }
}
