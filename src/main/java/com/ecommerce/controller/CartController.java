package com.ecommerce.controller;

import com.ecommerce.model.Order;
import com.ecommerce.service.CartService;
import com.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final OrderService orderService;

    @GetMapping("/cart")
    public String viewCart(Model model) {
                System.out.println("TestPR-Validation");
        model.addAttribute("cartItems", cartService.getItems());
        model.addAttribute("totalPrice", cartService.getTotalPrice());
        model.addAttribute("totalItems", cartService.getTotalItems());
        return "cart/view";
    }

    @PostMapping("/cart/update")
    public String updateCart(@RequestParam Long productId,
                             @RequestParam int quantity) {
        cartService.updateQuantity(productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam Long productId) {
        cartService.removeItem(productId);
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String checkoutForm(Model model) {
        if (cartService.isEmpty()) {
            return "redirect:/cart";
        }
        model.addAttribute("cartItems", cartService.getItems());
        model.addAttribute("totalPrice", cartService.getTotalPrice());
        model.addAttribute("order", new Order());
        return "checkout/form";
    }

    @PostMapping("/checkout")
    public String placeOrder(@ModelAttribute Order order,
                             RedirectAttributes redirectAttributes) {
        if (cartService.isEmpty()) {
            return "redirect:/cart";
        }
        Order savedOrder = orderService.placeOrder(order, cartService.getItems());
        cartService.clear();
        redirectAttributes.addFlashAttribute("orderId", savedOrder.getId());
        return "redirect:/checkout/success";
    }

    @GetMapping("/checkout/success")
    public String orderSuccess() {
        return "checkout/success";
    }
}
