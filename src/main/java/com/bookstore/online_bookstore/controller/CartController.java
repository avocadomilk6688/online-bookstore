package com.bookstore.online_bookstore.controller;

import com.bookstore.online_bookstore.db.DatabaseManager;
import com.bookstore.online_bookstore.model.Customer;
import com.bookstore.online_bookstore.model.Order;
import com.bookstore.online_bookstore.model.ShoppingCart;
import com.bookstore.online_bookstore.services.CartService;
import com.bookstore.online_bookstore.services.CartService.CheckoutSummary;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller for Shopping Cart, Checkout, and Orders.
 * Handles all customer-facing cart operations.
 */
@Controller
public class CartController {

    private final CartService cartService;

    public CartController() {
        this.cartService = new CartService();
    }

    // ============================================================
    // CART OPERATIONS
    // ============================================================

    /**
     * View shopping cart.
     */
    @GetMapping("/cart")
    public String viewCart(Model model) {
        Customer customer = DatabaseManager.getInstance().getLoggedInCustomer();

        if (customer == null) {
            return "redirect:/login";
        }

        ShoppingCart cart = cartService.getCart(customer.getUserID());
        model.addAttribute("cart", cart);
        model.addAttribute("isEmpty", cart.getItems().isEmpty());

        return "cart";
    }

    /**
     * Add item to cart.
     */
    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("isbn") String isbn,
            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
            RedirectAttributes redirectAttributes) {

        Customer customer = DatabaseManager.getInstance().getLoggedInCustomer();

        if (customer == null) {
            return "redirect:/login";
        }

        cartService.addToCart(customer.getUserID(), isbn, quantity);
        redirectAttributes.addFlashAttribute("message", "Item added to cart");

        return "redirect:/cart";
    }

    /**
     * Remove item from cart.
     */
    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam("isbn") String isbn,
            RedirectAttributes redirectAttributes) {

        Customer customer = DatabaseManager.getInstance().getLoggedInCustomer();

        if (customer == null) {
            return "redirect:/login";
        }

        cartService.removeFromCart(customer.getUserID(), isbn);
        redirectAttributes.addFlashAttribute("message", "Item removed from cart");

        return "redirect:/cart";
    }

    /**
     * Update item quantity in cart.
     */
    @PostMapping("/cart/update")
    public String updateCartItem(@RequestParam("isbn") String isbn,
            @RequestParam("quantity") int quantity,
            RedirectAttributes redirectAttributes) {

        Customer customer = DatabaseManager.getInstance().getLoggedInCustomer();

        if (customer == null) {
            return "redirect:/login";
        }

        cartService.updateCartItemQuantity(customer.getUserID(), isbn, quantity);
        redirectAttributes.addFlashAttribute("message", "Cart updated");

        return "redirect:/cart";
    }

    // ============================================================
    // CHECKOUT
    // ============================================================

    /**
     * View checkout page with discount summary.
     */
    @GetMapping("/checkout")
    public String viewCheckout(Model model) {
        Customer customer = DatabaseManager.getInstance().getLoggedInCustomer();

        if (customer == null) {
            return "redirect:/login";
        }

        CheckoutSummary summary = cartService.prepareCheckout(customer.getUserID());

        if (summary == null) {
            return "redirect:/cart";
        }

        model.addAttribute("summary", summary);
        model.addAttribute("customer", customer);
        model.addAttribute("defaultAddress", customer.getAddress());

        return "checkout";
    }

    /**
     * Process checkout and create order.
     */
    @PostMapping("/checkout/confirm")
    public String confirmCheckout(@RequestParam("deliveryAddress") String deliveryAddress,
            @RequestParam("paymentMethod") String paymentMethod,
            @RequestParam(value = "cardNumber", required = false) String cardNumber,
            @RequestParam(value = "cardHolder", required = false) String cardHolder,
            @RequestParam(value = "expiryDate", required = false) String expiryDate,
            @RequestParam(value = "cvv", required = false) String cvv,
            @RequestParam(value = "bankCode", required = false) String bankCode,
            RedirectAttributes redirectAttributes) {

        Customer customer = DatabaseManager.getInstance().getLoggedInCustomer();

        if (customer == null) {
            return "redirect:/login";
        }

        Order order = cartService.processCheckout(
                customer.getUserID(),
                deliveryAddress,
                paymentMethod,
                cardNumber,
                cardHolder,
                expiryDate,
                cvv,
                bankCode);

        if (order == null) {
            redirectAttributes.addFlashAttribute("error", "Checkout failed. Please try again.");
            return "redirect:/checkout";
        }

        redirectAttributes.addFlashAttribute("orderSuccess", true);
        redirectAttributes.addFlashAttribute("orderID", order.getOrderID());

        return "redirect:/orders/" + order.getOrderID();
    }

    // ============================================================
    // ORDER HISTORY
    // ============================================================

    /**
     * View order history.
     */
    @GetMapping("/orders")
    public String viewOrders(Model model) {
        Customer customer = DatabaseManager.getInstance().getLoggedInCustomer();

        if (customer == null) {
            return "redirect:/login";
        }

        List<Order> orders = cartService.getUserOrders(customer.getUserID());
        model.addAttribute("orders", orders);
        model.addAttribute("isEmpty", orders.isEmpty());

        return "orders";
    }

    /**
     * View specific order details.
     */
    @GetMapping("/orders/{id}")
    public String viewOrderDetails(@PathVariable("id") int orderId, Model model,
            @ModelAttribute("orderSuccess") String orderSuccess) {

        Customer customer = DatabaseManager.getInstance().getLoggedInCustomer();

        if (customer == null) {
            return "redirect:/login";
        }

        Order order = cartService.getOrder(orderId);

        if (order == null || order.getUserID() != customer.getUserID()) {
            return "redirect:/orders";
        }

        model.addAttribute("order", order);
        model.addAttribute("isNewOrder", "true".equals(orderSuccess));

        return "order-details";
    }
}
