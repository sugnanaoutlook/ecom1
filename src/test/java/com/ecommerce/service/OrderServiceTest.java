package com.ecommerce.service;

import com.ecommerce.model.CartItem;
import com.ecommerce.model.Order;
import com.ecommerce.model.Product;
import com.ecommerce.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private Product product1;
    private Product product2;
    private Order order;

    @BeforeEach
    void setUp() {
        product1 = new Product(1L, "Headphones", "Sound", new BigDecimal("79.99"), null, 50, "Electronics");
        product2 = new Product(2L, "Keyboard", "RGB", new BigDecimal("129.99"), null, 30, "Electronics");

        order = new Order();
        order.setCustomerName("Alice");
        order.setCustomerEmail("alice@example.com");
        order.setShippingAddress("123 Main St");
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.PENDING);
    }

    @Test
    void placeOrder_singleItem_savesOrderWithCorrectTotal() {
        List<CartItem> cartItems = List.of(new CartItem(product1, 2));

        Order savedOrder = new Order();
        savedOrder.setId(1L);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        Order result = orderService.placeOrder(order, cartItems);

        assertThat(result.getId()).isEqualTo(1L);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        Order captured = captor.getValue();

        // 79.99 * 2 = 159.98
        assertThat(captured.getTotalAmount()).isEqualByComparingTo(new BigDecimal("159.98"));
        assertThat(captured.getItems()).hasSize(1);
    }

    @Test
    void placeOrder_multipleItems_setsCorrectTotalAndOrderItems() {
        List<CartItem> cartItems = List.of(
                new CartItem(product1, 1),  // 79.99
                new CartItem(product2, 2)   // 259.98
        );

        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.placeOrder(order, cartItems);

        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("339.97"));
        assertThat(result.getItems()).hasSize(2);
    }

    @Test
    void placeOrder_setsUnitPriceFromProduct() {
        List<CartItem> cartItems = List.of(new CartItem(product1, 3));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.placeOrder(order, cartItems);

        assertThat(result.getItems().get(0).getUnitPrice()).isEqualByComparingTo(new BigDecimal("79.99"));
        assertThat(result.getItems().get(0).getQuantity()).isEqualTo(3);
    }

    @Test
    void placeOrder_emptyCart_savesOrderWithZeroTotal() {
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.placeOrder(order, List.of());

        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getItems()).isEmpty();
    }

    @Test
    void getOrderById_existingId_returnsOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Optional<Order> result = orderService.getOrderById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getCustomerName()).isEqualTo("Alice");
    }

    @Test
    void getOrderById_nonExistingId_returnsEmpty() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Order> result = orderService.getOrderById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void getOrdersByEmail_delegatesToRepository() {
        when(orderRepository.findByCustomerEmailOrderByOrderDateDesc("alice@example.com"))
                .thenReturn(List.of(order));

        List<Order> result = orderService.getOrdersByEmail("alice@example.com");

        assertThat(result).hasSize(1);
        verify(orderRepository).findByCustomerEmailOrderByOrderDateDesc("alice@example.com");
    }

    @Test
    void getOrdersByEmail_unknownEmail_returnsEmpty() {
        when(orderRepository.findByCustomerEmailOrderByOrderDateDesc("unknown@example.com"))
                .thenReturn(List.of());

        List<Order> result = orderService.getOrdersByEmail("unknown@example.com");

        assertThat(result).isEmpty();
    }
}
