package com.ecommerce.repository;

import com.ecommerce.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        productRepository.save(new Product(null, "Wireless Headphones", "Great sound", new BigDecimal("79.99"), null, 50, "Electronics"));
        productRepository.save(new Product(null, "Mechanical Keyboard", "RGB keyboard", new BigDecimal("129.99"), null, 30, "Electronics"));
        productRepository.save(new Product(null, "Running Shoes", "Lightweight shoes", new BigDecimal("89.99"), null, 60, "Footwear"));
        productRepository.save(new Product(null, "Yoga Mat", "Non-slip mat", new BigDecimal("34.99"), null, 80, "Sports"));
    }

    @Test
    void findAll_returnsAllProducts() {
        List<Product> products = productRepository.findAll();
        assertThat(products).hasSize(4);
    }

    @Test
    void findById_existingId_returnsProduct() {
        Product saved = productRepository.save(new Product(null, "Test Product", "Desc", new BigDecimal("10.00"), null, 5, "Test"));
        Optional<Product> found = productRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Product");
    }

    @Test
    void findById_nonExistingId_returnsEmpty() {
        Optional<Product> found = productRepository.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void findByCategory_existingCategory_returnsMatchingProducts() {
        List<Product> electronics = productRepository.findByCategory("Electronics");
        assertThat(electronics).hasSize(2);
        assertThat(electronics).allMatch(p -> p.getCategory().equals("Electronics"));
    }

    @Test
    void findByCategory_nonExistingCategory_returnsEmpty() {
        List<Product> products = productRepository.findByCategory("Nonexistent");
        assertThat(products).isEmpty();
    }

    @Test
    void findByNameContainingIgnoreCase_exactCase_returnsMatches() {
        List<Product> results = productRepository.findByNameContainingIgnoreCase("Headphones");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Wireless Headphones");
    }

    @Test
    void findByNameContainingIgnoreCase_upperCase_returnsMatches() {
        List<Product> results = productRepository.findByNameContainingIgnoreCase("KEYBOARD");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Mechanical Keyboard");
    }

    @Test
    void findByNameContainingIgnoreCase_partialMatch_returnsMatches() {
        List<Product> results = productRepository.findByNameContainingIgnoreCase("shoe");
        assertThat(results).hasSize(1);
    }

    @Test
    void findByNameContainingIgnoreCase_noMatch_returnsEmpty() {
        List<Product> results = productRepository.findByNameContainingIgnoreCase("xyznotfound");
        assertThat(results).isEmpty();
    }

    @Test
    void save_newProduct_persistsSuccessfully() {
        Product product = new Product(null, "New Item", "Desc", new BigDecimal("19.99"), null, 10, "Misc");
        Product saved = productRepository.save(product);
        assertThat(saved.getId()).isNotNull();
        assertThat(productRepository.count()).isEqualTo(5);
    }

    @Test
    void delete_existingProduct_removesFromDb() {
        Product saved = productRepository.save(new Product(null, "To Delete", "Desc", new BigDecimal("5.00"), null, 1, "Misc"));
        Long id = saved.getId();
        productRepository.deleteById(id);
        assertThat(productRepository.findById(id)).isEmpty();
    }
}
