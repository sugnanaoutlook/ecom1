package com.ecommerce.controller;

import com.ecommerce.model.Product;
import com.ecommerce.service.CartService;
import com.ecommerce.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private CartService cartService;

    private Product makeProduct(Long id, String name, String category) {
        return new Product(id, name, "desc", new BigDecimal("49.99"), null, 10, category);
    }

    @Test
    void listProducts_noCategory_returnsAllProducts() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of(makeProduct(1L, "Item1", "Electronics")));
        when(productService.getAllCategories()).thenReturn(List.of("Electronics"));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/list"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attributeExists("categories"));

        verify(productService).getAllProducts();
    }

    @Test
    void listProducts_withCategory_returnsFilteredProducts() throws Exception {
        when(productService.getProductsByCategory("Electronics"))
                .thenReturn(List.of(makeProduct(1L, "Headphones", "Electronics")));
        when(productService.getAllCategories()).thenReturn(List.of("Electronics"));

        mockMvc.perform(get("/products").param("category", "Electronics"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/list"))
                .andExpect(model().attribute("selectedCategory", "Electronics"));

        verify(productService).getProductsByCategory("Electronics");
        verify(productService, never()).getAllProducts();
    }

    @Test
    void listProducts_emptyCategory_returnsAllProducts() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of());
        when(productService.getAllCategories()).thenReturn(List.of());

        mockMvc.perform(get("/products").param("category", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("products/list"));

        verify(productService).getAllProducts();
    }

    @Test
    void productDetail_existingId_returnsDetailView() throws Exception {
        Product product = makeProduct(1L, "Headphones", "Electronics");
        when(productService.getProductById(1L)).thenReturn(Optional.of(product));
        when(productService.getProductsByCategory("Electronics")).thenReturn(List.of(product));

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/detail"))
                .andExpect(model().attributeExists("product"))
                .andExpect(model().attributeExists("relatedProducts"));
    }

    @Test
    void productDetail_nonExistingId_redirectsToProducts() throws Exception {
        when(productService.getProductById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/products/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));
    }

    @Test
    void productDetail_relatedProductsExcludeCurrentProduct() throws Exception {
        Product current = makeProduct(1L, "Headphones", "Electronics");
        Product related = makeProduct(2L, "Keyboard", "Electronics");
        when(productService.getProductById(1L)).thenReturn(Optional.of(current));
        when(productService.getProductsByCategory("Electronics")).thenReturn(List.of(current, related));

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("relatedProducts",
                        org.hamcrest.Matchers.hasSize(1)));
    }

    @Test
    void addToCart_existingProduct_addsItemAndRedirects() throws Exception {
        Product product = makeProduct(1L, "Headphones", "Electronics");
        when(productService.getProductById(1L)).thenReturn(Optional.of(product));

        mockMvc.perform(post("/products/1/add-to-cart").param("quantity", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products/1"));

        verify(cartService).addItem(product, 2);
    }

    @Test
    void addToCart_defaultQuantityIsOne() throws Exception {
        Product product = makeProduct(1L, "Headphones", "Electronics");
        when(productService.getProductById(1L)).thenReturn(Optional.of(product));

        mockMvc.perform(post("/products/1/add-to-cart"))
                .andExpect(status().is3xxRedirection());

        verify(cartService).addItem(product, 1);
    }

    @Test
    void addToCart_nonExistingProduct_redirectsWithoutAddingToCart() throws Exception {
        when(productService.getProductById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/products/999/add-to-cart").param("quantity", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products/999"));

        verify(cartService, never()).addItem(any(), anyInt());
    }
}
