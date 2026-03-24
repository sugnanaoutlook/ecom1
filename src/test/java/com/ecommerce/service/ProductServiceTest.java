package com.ecommerce.service;

import com.ecommerce.model.Product;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product electronics1;
    private Product electronics2;
    private Product footwear1;

    @BeforeEach
    void setUp() {
        electronics1 = new Product(1L, "Wireless Headphones", "Great sound", new BigDecimal("79.99"), null, 50, "Electronics");
        electronics2 = new Product(2L, "Mechanical Keyboard", "RGB keyboard", new BigDecimal("129.99"), null, 30, "Electronics");
        footwear1 = new Product(3L, "Running Shoes", "Lightweight shoes", new BigDecimal("89.99"), null, 60, "Footwear");
    }

    @Test
    void getAllProducts_returnsAllProducts() {
        when(productRepository.findAll()).thenReturn(List.of(electronics1, electronics2, footwear1));

        List<Product> result = productService.getAllProducts();

        assertThat(result).hasSize(3);
        verify(productRepository).findAll();
    }

    @Test
    void getProductById_existingId_returnsProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(electronics1));

        Optional<Product> result = productService.getProductById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Wireless Headphones");
    }

    @Test
    void getProductById_nonExistingId_returnsEmpty() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Product> result = productService.getProductById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void getProductsByCategory_returnsMatchingProducts() {
        when(productRepository.findByCategory("Electronics")).thenReturn(List.of(electronics1, electronics2));

        List<Product> result = productService.getProductsByCategory("Electronics");

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(p -> p.getCategory().equals("Electronics"));
    }

    @Test
    void searchProducts_delegatesToRepository() {
        when(productRepository.findByNameContainingIgnoreCase("head")).thenReturn(List.of(electronics1));

        List<Product> result = productService.searchProducts("head");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Wireless Headphones");
        verify(productRepository).findByNameContainingIgnoreCase("head");
    }

    @Test
    void getAllCategories_returnsDistinctSortedCategories() {
        when(productRepository.findAll()).thenReturn(List.of(electronics1, electronics2, footwear1));

        List<String> categories = productService.getAllCategories();

        assertThat(categories).containsExactly("Electronics", "Footwear");
    }

    @Test
    void getAllCategories_withDuplicates_returnsDistinct() {
        Product extra = new Product(4L, "Extra Shoe", "desc", new BigDecimal("20"), null, 5, "Footwear");
        when(productRepository.findAll()).thenReturn(List.of(electronics1, footwear1, extra));

        List<String> categories = productService.getAllCategories();

        assertThat(categories).containsExactlyInAnyOrder("Electronics", "Footwear");
        assertThat(categories).doesNotHaveDuplicates();
    }

    @Test
    void saveProduct_callsRepositorySave() {
        productService.saveProduct(electronics1);
        verify(productRepository).save(electronics1);
    }
}
