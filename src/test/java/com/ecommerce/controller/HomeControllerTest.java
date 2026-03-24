package com.ecommerce.controller;

import com.ecommerce.model.Product;
import com.ecommerce.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HomeController.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    private Product makeProduct(Long id, String name, String category) {
        return new Product(id, name, "desc", new BigDecimal("19.99"), null, 10, category);
    }

    @Test
    void home_returnsIndexView() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of(makeProduct(1L, "Item1", "Electronics")));
        when(productService.getAllCategories()).thenReturn(List.of("Electronics"));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("featuredProducts"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    void home_limitsFeaturedProductsToSix() throws Exception {
        List<Product> products = List.of(
                makeProduct(1L, "P1", "A"), makeProduct(2L, "P2", "A"),
                makeProduct(3L, "P3", "A"), makeProduct(4L, "P4", "A"),
                makeProduct(5L, "P5", "A"), makeProduct(6L, "P6", "A"),
                makeProduct(7L, "P7", "A"), makeProduct(8L, "P8", "A")
        );
        when(productService.getAllProducts()).thenReturn(products);
        when(productService.getAllCategories()).thenReturn(List.of("A"));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("featuredProducts",
                        org.hamcrest.Matchers.hasSize(6)));
    }

    @Test
    void search_returnsProductsListView() throws Exception {
        when(productService.searchProducts("headphones")).thenReturn(List.of(makeProduct(1L, "Headphones", "Electronics")));
        when(productService.getAllCategories()).thenReturn(List.of("Electronics"));

        mockMvc.perform(get("/search").param("q", "headphones"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/list"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attribute("keyword", "headphones"));
    }

    @Test
    void search_noResults_returnsEmptyProductsList() throws Exception {
        when(productService.searchProducts("xyz")).thenReturn(List.of());
        when(productService.getAllCategories()).thenReturn(List.of());

        mockMvc.perform(get("/search").param("q", "xyz"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/list"))
                .andExpect(model().attribute("products", org.hamcrest.Matchers.hasSize(0)));
    }
}
