package com.ecommerce.controller;

import com.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("featuredProducts", productService.getAllProducts().stream().limit(6).toList());
        model.addAttribute("categories", productService.getAllCategories());
        return "index";
    }

    @GetMapping("/search")
    public String search(@RequestParam("q") String keyword, Model model) {
        model.addAttribute("products", productService.searchProducts(keyword));
        model.addAttribute("keyword", keyword);
        model.addAttribute("categories", productService.getAllCategories());
        return "products/list";
    }
}
