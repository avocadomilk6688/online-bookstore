package com.bookstore.online_bookstore.controller;

import com.bookstore.online_bookstore.db.DatabaseManager;
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
    public String registerCustomer(@RequestParam String email,
                                   @RequestParam String password,
                                   @RequestParam String memberType,
                                   @RequestParam String address) {

        DatabaseManager.getInstance().addCustomer(email, password, memberType, address);
        return "redirect:/login";
    }

    @GetMapping("/profile")
    public String profilePage(Model model) {
        model.addAttribute("customer",
                DatabaseManager.getInstance().getLoggedInCustomer());
        return "profile";
    }
}

