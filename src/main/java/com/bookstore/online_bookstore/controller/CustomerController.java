package com.bookstore.online_bookstore.controller;

import com.bookstore.online_bookstore.db.DatabaseManager;
import com.bookstore.online_bookstore.model.Customer;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String registerCustomer(@RequestParam String name,
                                   @RequestParam String email,
                                   @RequestParam String password,
                                   @RequestParam String confirmPassword,
                                   @RequestParam String role,      // Hidden field from our UI
                                   @RequestParam String memberType,
                                   @RequestParam String birthDate,
                                   @RequestParam String address,
                                   Model model) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match. Please try again.");
            return "register";
        }
        // 1. Check if the email is already taken
        if (DatabaseManager.getInstance().isEmailExists(email)) {
            model.addAttribute("error", "This email is already registered. Please use another or login.");
            return "register"; // Stay on the register page
        }
        // 2. If not taken, proceed with registration
        DatabaseManager.getInstance().addUser(email, password, "MEMBER", name, memberType, birthDate, address);    
        
        return "redirect:/login?registered=true";
    }

    @GetMapping("/profile")
    public String profilePage(Model model) {
        model.addAttribute("customer",
                DatabaseManager.getInstance().getLoggedInCustomer());
        return "profile";
    }

    @PostMapping("/updateProfile")
    public String updateProfile(@RequestParam String name,
                                @RequestParam String birthDate,
                                @RequestParam String address) {
    
        Customer current = DatabaseManager.getInstance().getLoggedInCustomer();
        if (current != null) {
            DatabaseManager.getInstance().updateCustomerProfile(current.getEmail(), name, birthDate, address);
            // These require setters in your Customer class
            current.setName(name);
            current.setBirthDate(birthDate);
            current.setAddress(address);
        }
        return "redirect:/customer/profile?success=true";
    }
}

