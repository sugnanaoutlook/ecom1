package com.ecommerce.repository;

import com.ecommerce.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }

    private Order buildOrder(String name, String email, LocalDateTime date) {
        Order order = new Order();
        order.setCustomerName(name);
        order.setCustomerEmail(email);
        order.setShippingAddress("123 Main St");
        order.setTotalAmount(new BigDecimal("99.99"));
        order.setOrderDate(date);
        order.setStatus(Order.OrderStatus.PENDING);
        return order;
    }

    @Test
    void save_newOrder_persistsSuccessfully() {
        Order order = buildOrder("Alice", "alice@example.com", LocalDateTime.now());
        Order saved = orderRepository.save(order);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void findById_existingOrder_returnsOrder() {
        Order saved = orderRepository.save(buildOrder("Bob", "bob@example.com", LocalDateTime.now()));
        Optional<Order> found = orderRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCustomerName()).isEqualTo("Bob");
    }

    @Test
    void findById_nonExistingId_returnsEmpty() {
        Optional<Order> found = orderRepository.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void findByCustomerEmailOrderByOrderDateDesc_returnsOrdersForEmail() {
        LocalDateTime now = LocalDateTime.now();
        orderRepository.save(buildOrder("Carol", "carol@example.com", now.minusDays(2)));
        orderRepository.save(buildOrder("Carol", "carol@example.com", now.minusDays(1)));
        orderRepository.save(buildOrder("Dave", "dave@example.com", now));

        List<Order> carolOrders = orderRepository.findByCustomerEmailOrderByOrderDateDesc("carol@example.com");
        assertThat(carolOrders).hasSize(2);
        assertThat(carolOrders).allMatch(o -> o.getCustomerEmail().equals("carol@example.com"));
    }

    @Test
    void findByCustomerEmailOrderByOrderDateDesc_orderedByDateDescending() {
        LocalDateTime now = LocalDateTime.now();
        orderRepository.save(buildOrder("Eve", "eve@example.com", now.minusDays(3)));
        orderRepository.save(buildOrder("Eve", "eve@example.com", now.minusDays(1)));
        orderRepository.save(buildOrder("Eve", "eve@example.com", now.minusDays(2)));

        List<Order> orders = orderRepository.findByCustomerEmailOrderByOrderDateDesc("eve@example.com");
        assertThat(orders).hasSize(3);
        assertThat(orders.get(0).getOrderDate()).isAfter(orders.get(1).getOrderDate());
        assertThat(orders.get(1).getOrderDate()).isAfter(orders.get(2).getOrderDate());
    }

    @Test
    void findByCustomerEmailOrderByOrderDateDesc_unknownEmail_returnsEmpty() {
        List<Order> orders = orderRepository.findByCustomerEmailOrderByOrderDateDesc("unknown@example.com");
        assertThat(orders).isEmpty();
    }

    @Test
    void delete_existingOrder_removesFromDb() {
        Order saved = orderRepository.save(buildOrder("Frank", "frank@example.com", LocalDateTime.now()));
        Long id = saved.getId();
        orderRepository.deleteById(id);
        assertThat(orderRepository.findById(id)).isEmpty();
    }
}
