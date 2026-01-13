package com.bookstore.online_bookstore.controller;

import com.bookstore.online_bookstore.model.User;
import com.bookstore.online_bookstore.db.DatabaseManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        Model model) {

    User user = DatabaseManager.getInstance().getUserByEmail(email);

        if (user == null || !user.getPassword().equals(password)) {
            model.addAttribute("error", "Invalid login");
            return "login";
        }

        if (user.getRole().equals("ADMIN"))
            return "redirect:/admin/dashboard";

        return "redirect:/customer/profile";
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/login";
    }
}
