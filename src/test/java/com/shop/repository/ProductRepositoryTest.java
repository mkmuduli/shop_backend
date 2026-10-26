package com.shop.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.SQLException;
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

        repository.save(product);

        Product saved = findSavedProduct(repository, product.getName());
        assertNotNull(saved.getId());
        assertEquals(product.getName(), repository.findById(saved.getId()).orElseThrow().getName());
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
        repository.save(product);
        int id = findSavedProduct(repository, product.getName()).getId();

        assertTrue(repository.deleteById(id));
        assertTrue(repository.findById(id).isEmpty());
        assertFalse(repository.deleteById(id));
    }

    @Test
    void shouldUpdateExistingProduct() throws SQLException {
        ProductRepository repository = new ProductRepository();
        Product original = product("Keyboard");
        repository.save(original);
        int id = findSavedProduct(repository, original.getName()).getId();
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

    private Product findSavedProduct(ProductRepository repository, String name) throws SQLException {
        Optional<Product> saved = repository.findAll().stream()
                .filter(product -> product.getName().equals(name))
                .reduce((first, second) -> second);
        return saved.orElseThrow();
    }

    private Product product(String name) {
        return new Product(name, new BigDecimal("1499.00"), 20);
    }
}
