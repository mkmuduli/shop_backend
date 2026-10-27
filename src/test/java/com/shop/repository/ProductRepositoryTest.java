package com.shop.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.shop.DatabaseMigrator;
import com.shop.model.Product;

class ProductRepositoryTest {

    @BeforeAll
    static void setUpDatabase() {
        DatabaseMigrator.migrate();
    }

    @Test
    void shouldSaveAndFindProductById() throws SQLException {
        ProductRepository repository = new ProductRepository();
        Product product = product("Keyboard");

        int id = repository.save(product);

        Product saved = repository.findById(id).orElseThrow();
        assertNotNull(saved.getId());
        assertEquals(product.getName(), saved.getName());
    }

    @Test
    void shouldReturnEmptyWhenProductIdDoesNotExist() throws SQLException {
        ProductRepository repository = new ProductRepository();

        assertTrue(repository.findById(-1).isEmpty());
    }

    @Test
    void shouldFindAllSavedProducts() throws SQLException {
        ProductRepository repository = new ProductRepository();
        Product keyboard = product("Keyboard");
        Product mouse = product("Mouse");
        repository.save(keyboard);
        repository.save(mouse);

        assertTrue(repository.findAll().stream().anyMatch(p -> p.getName().equals(keyboard.getName())));
        assertTrue(repository.findAll().stream().anyMatch(p -> p.getName().equals(mouse.getName())));
    }

    @Test
    void shouldDeleteProductById() throws SQLException {
        ProductRepository repository = new ProductRepository();
        Product product = product("Keyboard");
        int id = repository.save(product);

        assertTrue(repository.deleteById(id));
        assertTrue(repository.findById(id).isEmpty());
        assertFalse(repository.deleteById(id));
    }

    @Test
    void shouldUpdateExistingProduct() throws SQLException {
        ProductRepository repository = new ProductRepository();
        Product original = product("Keyboard");
        int id = repository.save(original);
        Product updatedProduct = product("Mechanical Keyboard");
        updatedProduct = new Product(id, updatedProduct.getName(), updatedProduct.getPrice(),
                updatedProduct.getStockQuantity());

        assertTrue(repository.update(updatedProduct));
        assertEquals(updatedProduct.getName(), repository.findById(id).orElseThrow().getName());
    }

    @Test
    void shouldNotUpdateProductThatDoesNotExist() throws SQLException {
        ProductRepository repository = new ProductRepository();

        assertFalse(repository.update(product("Keyboard")));
    }

    @Test
    void shouldPaginateFindAllByLimitAndOffset() throws SQLException {
        ProductRepository repository = new ProductRepository();
        List<Product> all = repository.findAll(10_000, 0);

        List<Product> stitched = new ArrayList<>();
        for (int offset = 0; offset < all.size(); offset += 2) {
            stitched.addAll(repository.findAll(2, offset));
        }

        assertEquals(
                all.stream().map(Product::getId).toList(),
                stitched.stream().map(Product::getId).toList());
        assertTrue(repository.findAll(2, all.size()).isEmpty());
    }

    @Test
    void shouldRejectInvalidPaginationArguments() {
        ProductRepository repository = new ProductRepository();

        assertThrows(IllegalArgumentException.class,
                () -> repository.findAll(0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> repository.findAll(-1, 0));
        assertThrows(IllegalArgumentException.class,
                () -> repository.findAll(1, -1));
    }

    private Product product(String name) {
        return new Product(name, new BigDecimal("1499.00"), 20);
    }
}
