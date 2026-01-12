package com.bookstore.online_bookstore.controller;

import com.bookstore.online_bookstore.model.Book;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class BookController {
    @GetMapping("/catalog")
    public String viewBooks(@RequestParam(value = "genre", required = false) String genre, Model model) {
        Book bookHelper = new Book();
        List<Book> displayBooks;

        if (genre != null && !genre.isEmpty()) {
            System.out.println("🔍 Genre Clicked: " + genre);
            displayBooks = bookHelper.getBooksByGenre(genre); // Use the new method
        } else {
            displayBooks = bookHelper.getAllBooks();
        }

        System.out.println("📦 Books found: " + (displayBooks != null ? displayBooks.size() : 0));
        model.addAttribute("books", displayBooks);
        return "catalog";
    }
}
