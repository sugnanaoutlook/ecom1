package com.ecommerce.controller;

import com.ecommerce.model.Product;
import com.ecommerce.service.CartService;
import com.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CartService cartService;

    @GetMapping
    public String listProducts(@RequestParam(required = false) String category, Model model) {
        if (category != null && !category.isEmpty()) {
            model.addAttribute("products", productService.getProductsByCategory(category));
            model.addAttribute("selectedCategory", category);
        } else {
            model.addAttribute("products", productService.getAllProducts());
        }
        model.addAttribute("categories", productService.getAllCategories());
        return "products/list";
    }

    @GetMapping("/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        return productService.getProductById(id)
                .map(product -> {
                    model.addAttribute("product", product);
                    model.addAttribute("relatedProducts",
                            productService.getProductsByCategory(product.getCategory())
                                    .stream()
                                    .filter(p -> !p.getId().equals(id))
                                    .limit(4)
                                    .toList());
                    return "products/detail";
                })
                .orElse("redirect:/products");
    }

    @PostMapping("/{id}/add-to-cart")
    public String addToCart(@PathVariable Long id,
                            @RequestParam(defaultValue = "1") int quantity,
                            RedirectAttributes redirectAttributes) {
        productService.getProductById(id).ifPresent(product -> {
            cartService.addItem(product, quantity);
            redirectAttributes.addFlashAttribute("success",
                    product.getName() + " added to cart!");
        });
        return "redirect:/products/" + id;
    }
}
