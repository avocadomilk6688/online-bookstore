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

        if (user == null || !user.getPassword().equals(password)) {
            model.addAttribute("error", "Invalid Email or Password");
            return "login";
        }

        if (!user.getRole().equalsIgnoreCase(role)) {
            model.addAttribute("error", "Unauthorized: You do not have " + role + " privileges.");
            return "login";
        }

        if (user.getRole().equalsIgnoreCase("CUSTOMER")) {
            session.setAttribute("userID", user.getUserID());
            session.setAttribute("userName", user.getName());
            return "redirect:/catalog";
        }

        session.setAttribute("adminID", user.getUserID());
        session.setAttribute("adminName", user.getName());

        DatabaseManager.getInstance().setLoggedInUser(user);

        if (user.getRole().equals("ADMIN"))
            return "redirect:/admin/dashboard";

        return "redirect:/catalog";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
