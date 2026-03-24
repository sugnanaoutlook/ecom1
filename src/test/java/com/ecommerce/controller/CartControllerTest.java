package com.ecommerce.controller;

import com.ecommerce.model.CartItem;
import com.ecommerce.model.Order;
import com.ecommerce.model.Product;
import com.ecommerce.service.CartService;
import com.ecommerce.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @MockBean
    private OrderService orderService;

    private Product makeProduct(Long id, String name) {
        return new Product(id, name, "desc", new BigDecimal("49.99"), null, 10, "Electronics");
    }

    @Test
    void viewCart_returnsCartView() throws Exception {
        when(cartService.getItems()).thenReturn(List.of());
        when(cartService.getTotalPrice()).thenReturn(BigDecimal.ZERO);
        when(cartService.getTotalItems()).thenReturn(0);

        mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart/view"))
                .andExpect(model().attributeExists("cartItems"))
                .andExpect(model().attributeExists("totalPrice"))
                .andExpect(model().attributeExists("totalItems"));
    }

    @Test
    void viewCart_withItems_populatesModel() throws Exception {
        CartItem item = new CartItem(makeProduct(1L, "Headphones"), 2);
        when(cartService.getItems()).thenReturn(List.of(item));
        when(cartService.getTotalPrice()).thenReturn(new BigDecimal("99.98"));
        when(cartService.getTotalItems()).thenReturn(2);

        mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("totalItems", 2));
    }

    @Test
    void updateCart_redirectsToCart() throws Exception {
        mockMvc.perform(post("/cart/update")
                        .param("productId", "1")
                        .param("quantity", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        verify(cartService).updateQuantity(1L, 3);
    }

    @Test
    void removeFromCart_redirectsToCart() throws Exception {
        mockMvc.perform(post("/cart/remove").param("productId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        verify(cartService).removeItem(1L);
    }

    @Test
    void checkoutForm_cartNotEmpty_returnsCheckoutView() throws Exception {
        CartItem item = new CartItem(makeProduct(1L, "Headphones"), 1);
        when(cartService.isEmpty()).thenReturn(false);
        when(cartService.getItems()).thenReturn(List.of(item));
        when(cartService.getTotalPrice()).thenReturn(new BigDecimal("49.99"));

        mockMvc.perform(get("/checkout"))
                .andExpect(status().isOk())
                .andExpect(view().name("checkout/form"))
                .andExpect(model().attributeExists("cartItems"))
                .andExpect(model().attributeExists("totalPrice"))
                .andExpect(model().attributeExists("order"));
    }

    @Test
    void checkoutForm_emptyCart_redirectsToCart() throws Exception {
        when(cartService.isEmpty()).thenReturn(true);

        mockMvc.perform(get("/checkout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));
    }

    @Test
    void placeOrder_cartNotEmpty_placesOrderAndRedirects() throws Exception {
        CartItem item = new CartItem(makeProduct(1L, "Headphones"), 1);
        when(cartService.isEmpty()).thenReturn(false);
        when(cartService.getItems()).thenReturn(List.of(item));

        Order savedOrder = new Order();
        savedOrder.setId(42L);
        when(orderService.placeOrder(any(Order.class), any())).thenReturn(savedOrder);

        mockMvc.perform(post("/checkout")
                        .param("customerName", "Alice")
                        .param("customerEmail", "alice@example.com")
                        .param("shippingAddress", "123 Main St"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/checkout/success"));

        verify(orderService).placeOrder(any(Order.class), any());
        verify(cartService).clear();
    }

    @Test
    void placeOrder_emptyCart_redirectsToCart() throws Exception {
        when(cartService.isEmpty()).thenReturn(true);

        mockMvc.perform(post("/checkout")
                        .param("customerName", "Alice")
                        .param("customerEmail", "alice@example.com")
                        .param("shippingAddress", "123 Main St"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        verify(orderService, never()).placeOrder(any(), any());
    }

    @Test
    void orderSuccess_returnsSuccessView() throws Exception {
        mockMvc.perform(get("/checkout/success"))
                .andExpect(status().isOk())
                .andExpect(view().name("checkout/success"));
    }
}
