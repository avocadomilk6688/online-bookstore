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
    public String viewBooks(
            @RequestParam(value = "genre", required = false) String genre,
            @RequestParam(value = "search", required = false) String search,
            Model model) {

        Book bookHelper = new Book();
        List<Book> displayBooks;

        if (search != null && !search.isEmpty()) {
            displayBooks = bookHelper.searchBooks(search);
        } else if (genre != null && !genre.isEmpty() && !genre.equalsIgnoreCase("All")) {
            displayBooks = bookHelper.getBooksByGenre(genre);
        } else {
            displayBooks = bookHelper.getAllBooks();
        }

        model.addAttribute("books", displayBooks);
        return "catalog";
    }
}
