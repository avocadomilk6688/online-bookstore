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
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        @RequestParam String role,
                        Model model) {

    User user = DatabaseManager.getInstance().getUserByEmail(username);

        if (user == null || !user.getPassword().equals(password)) {
            model.addAttribute("error", "Invalid Email or Password");
            return "login";
        }

        if (!user.getRole().equalsIgnoreCase(role)) {
        model.addAttribute("error", "Unauthorized: You do not have " + role + " privileges.");
        return "login";
        }

        DatabaseManager.getInstance().setLoggedInUser(user);

        if (user.getRole().equals("ADMIN"))
            return "redirect:/admin/dashboard";

        return "redirect:/catalog";
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/login";
    }
}
