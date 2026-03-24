package com.ecommerce.service;

import com.ecommerce.model.CartItem;
import com.ecommerce.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CartServiceTest {

    private CartService cartService;
    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        cartService = new CartService();
        product1 = new Product(1L, "Headphones", "Sound", new BigDecimal("79.99"), null, 50, "Electronics");
        product2 = new Product(2L, "Keyboard", "Typing", new BigDecimal("129.99"), null, 30, "Electronics");
    }

    @Test
    void initialCart_isEmpty() {
        assertThat(cartService.isEmpty()).isTrue();
        assertThat(cartService.getItems()).isEmpty();
        assertThat(cartService.getTotalItems()).isZero();
        assertThat(cartService.getTotalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void addItem_newProduct_addsToCart() {
        cartService.addItem(product1, 2);

        assertThat(cartService.getItems()).hasSize(1);
        assertThat(cartService.getItems().get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    void addItem_existingProduct_incrementsQuantity() {
        cartService.addItem(product1, 2);
        cartService.addItem(product1, 3);

        assertThat(cartService.getItems()).hasSize(1);
        assertThat(cartService.getItems().get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    void addItem_differentProducts_addsMultipleItems() {
        cartService.addItem(product1, 1);
        cartService.addItem(product2, 1);

        assertThat(cartService.getItems()).hasSize(2);
    }

    @Test
    void getTotalItems_sumsAllQuantities() {
        cartService.addItem(product1, 3);
        cartService.addItem(product2, 2);

        assertThat(cartService.getTotalItems()).isEqualTo(5);
    }

    @Test
    void getTotalPrice_calculatesCorrectTotal() {
        cartService.addItem(product1, 1); // 79.99
        cartService.addItem(product2, 2); // 259.98

        BigDecimal expected = new BigDecimal("79.99").add(new BigDecimal("129.99").multiply(BigDecimal.valueOf(2)));
        assertThat(cartService.getTotalPrice()).isEqualByComparingTo(expected);
    }

    @Test
    void updateQuantity_validQuantity_updatesItem() {
        cartService.addItem(product1, 2);
        cartService.updateQuantity(1L, 5);

        assertThat(cartService.getItems().get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    void updateQuantity_zeroQuantity_removesItem() {
        cartService.addItem(product1, 2);
        cartService.updateQuantity(1L, 0);

        assertThat(cartService.getItems()).isEmpty();
    }

    @Test
    void updateQuantity_negativeQuantity_removesItem() {
        cartService.addItem(product1, 2);
        cartService.updateQuantity(1L, -1);

        assertThat(cartService.getItems()).isEmpty();
    }

    @Test
    void updateQuantity_nonExistingProduct_doesNothing() {
        cartService.addItem(product1, 2);
        cartService.updateQuantity(999L, 5);

        assertThat(cartService.getItems()).hasSize(1);
        assertThat(cartService.getItems().get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    void removeItem_existingProduct_removesFromCart() {
        cartService.addItem(product1, 2);
        cartService.addItem(product2, 1);

        cartService.removeItem(1L);

        assertThat(cartService.getItems()).hasSize(1);
        assertThat(cartService.getItems().get(0).getProduct().getId()).isEqualTo(2L);
    }

    @Test
    void removeItem_nonExistingProduct_doesNothing() {
        cartService.addItem(product1, 2);
        cartService.removeItem(999L);

        assertThat(cartService.getItems()).hasSize(1);
    }

    @Test
    void clear_removesAllItems() {
        cartService.addItem(product1, 2);
        cartService.addItem(product2, 1);

        cartService.clear();

        assertThat(cartService.isEmpty()).isTrue();
        assertThat(cartService.getItems()).isEmpty();
    }

    @Test
    void isEmpty_withItems_returnsFalse() {
        cartService.addItem(product1, 1);
        assertThat(cartService.isEmpty()).isFalse();
    }
}
