package com.bookstore.online_bookstore.controller;

import com.bookstore.online_bookstore.model.User;

import jakarta.servlet.http.HttpSession;

import com.bookstore.online_bookstore.db.DatabaseManager;

import org.springframework.boot.micrometer.observation.autoconfigure.ObservationProperties.Http;
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
            Model model, HttpSession session) {

        User user = DatabaseManager.getInstance().getUserByEmail(username);

        // 1. Check if user exists and password is correct
        if (user == null || !user.getPassword().equals(password)) {
            model.addAttribute("error", "Invalid Email or Password");
            return "login";
        }

        // 2. Check if the role chosen in the dropdown matches the database
        if (!user.getRole().equalsIgnoreCase(role)) {
            model.addAttribute("error", "Unauthorized: You do not have " + role + " privileges.");
            return "login";
        }

        // 3. Clear existing session data to prevent role mixing
        session.removeAttribute("userID");
        session.removeAttribute("adminID");

        // 4. Role-Based Routing
        if (user.getRole().equalsIgnoreCase("MEMBER")) {
            session.setAttribute("userID", user.getUserID());
            session.setAttribute("userName", user.getName());

            // Sync the global manager for customer
            DatabaseManager.getInstance().setLoggedInUser(user);
            return "redirect:/catalog";

        } else if (user.getRole().equalsIgnoreCase("ADMIN")) {
            // ONLY set adminID if the database confirms they are an ADMIN
            session.setAttribute("adminID", user.getUserID());
            session.setAttribute("adminName", user.getName());

            // Sync the global manager for admin
            DatabaseManager.getInstance().setLoggedInUser(user);
            return "redirect:/admin/dashboard";
        }

        // Default fallback
        return "redirect:/catalog";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        DatabaseManager.getInstance().setLoggedInUser(null);
        return "redirect:/login";
    }
}
