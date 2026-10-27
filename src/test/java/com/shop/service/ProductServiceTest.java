package com.shop.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.shop.model.Product;
import com.shop.model.ProductListResponse;
import com.shop.repository.ProductRepository;

class ProductServiceTest {

    private ProductService service;

    @BeforeEach
    void setUp() {
        service = new ProductService(new ProductRepository());
    }

    @Test
    void shouldCreateGetAndDeleteProduct() throws SQLException {
        Product product = product("Service Test Product");

        Product created = service.createProduct(product);
        try {
            assertTrue(created.getId() > 0);

            Product fetched = service
                    .getProductById(created.getId())
                    .orElseThrow();
            assertEquals(product.getName(), fetched.getName());
            assertEquals(product.getPrice(), fetched.getPrice());
            assertEquals(product.getStockQuantity(),
                    fetched.getStockQuantity());

            assertTrue(service.deleteProduct(created.getId()));
        } finally {
            service.deleteProduct(created.getId());
        }
    }

    @Test
    void shouldReturnEmptyWhenGetProductDoesNotExist() throws SQLException {
        assertTrue(service.getProductById(999_999_999).isEmpty());
    }

    @Test
    void shouldReturnFalseWhenDeleteProductDoesNotExist()
            throws SQLException {
        assertFalse(service.deleteProduct(999_999_999));
    }

    @Test
    void shouldRejectMissingName() {
        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> service.createProduct(product("   ")));
        assertEquals("Product name is required", e.getMessage());
    }

    @Test
    void shouldRejectZeroPrice() {
        Product product = new Product(
                "Service Test Product", BigDecimal.ZERO, 5);

        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> service.createProduct(product));
        assertEquals("Product price must be greater than zero",
                e.getMessage());
    }

    @Test
    void shouldRejectNegativeStockQuantity() {
        // the Product constructor rejects negative stock before the service sees it
        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> service.createProduct(new Product(
                        "Service Test Product", new BigDecimal("9.99"), -1)));
        assertEquals("Stock quantity cannot be negative", e.getMessage());
    }

    @Test
    void shouldRejectInvalidPage() {
        assertEquals("Page must be greater than zero",
                assertThrows(IllegalArgumentException.class,
                        () -> service.getProducts(0, 10)).getMessage());
    }

    @Test
    void shouldRejectLimitOutOfRange() {
        assertEquals("Limit must be between 1 and 100",
                assertThrows(IllegalArgumentException.class,
                        () -> service.getProducts(1, 0)).getMessage());
        assertEquals("Limit must be between 1 and 100",
                assertThrows(IllegalArgumentException.class,
                        () -> service.getProducts(1, 101)).getMessage());
    }

    @Test
    void shouldReturnRequestedPageSize() throws SQLException {
        ProductListResponse page = service.getProducts(1, 1);

        assertTrue(page.items().size() <= 1);
        assertEquals(1, page.page());
        assertEquals(1, page.limit());
    }

    @Test
    void shouldReturnEmptyItemsForPageBeyondLast() throws SQLException {
        ProductListResponse page = service.getProducts(10_000, 5);

        assertTrue(page.items().isEmpty());
        assertEquals(10_000, page.page());
        assertEquals(5, page.limit());
    }

    private Product product(String name) {
        return new Product(name, new BigDecimal("19.99"), 7);
    }
}
