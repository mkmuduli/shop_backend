package com.shop.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.shop.model.Product;

class ProductRepositoryTest {

    @Test
    void shouldSaveAndFindProductById() {
        ProductRepository repository = new ProductRepository();
        Product product = product(1, "Keyboard");

        repository.save(product);

        assertSame(product, repository.findById(1));
    }

    @Test
    void shouldReturnNullWhenProductIdDoesNotExist() {
        ProductRepository repository = new ProductRepository();

        assertNull(repository.findById(99));
    }

    @Test
    void shouldFindAllSavedProducts() {
        ProductRepository repository = new ProductRepository();
        Product keyboard = product(1, "Keyboard");
        Product mouse = product(2, "Mouse");
        repository.save(keyboard);
        repository.save(mouse);

        assertEquals(2, repository.findAll().size());
        assertTrue(repository.findAll().contains(keyboard));
        assertTrue(repository.findAll().contains(mouse));
    }

    @Test
    void shouldDeleteProductById() {
        ProductRepository repository = new ProductRepository();
        repository.save(product(1, "Keyboard"));

        assertTrue(repository.deleteById(1));
        assertNull(repository.findById(1));
        assertFalse(repository.deleteById(1));
    }

    @Test
    void shouldUpdateExistingProduct() {
        ProductRepository repository = new ProductRepository();
        repository.save(product(1, "Keyboard"));
        Product updatedProduct = product(1, "Mechanical Keyboard");

        assertTrue(repository.update(updatedProduct));
        assertSame(updatedProduct, repository.findById(1));
    }

    @Test
    void shouldNotUpdateProductThatDoesNotExist() {
        ProductRepository repository = new ProductRepository();

        assertFalse(repository.update(product(1, "Keyboard")));
    }

    private Product product(int id, String name) {
        return new Product(id, name, new BigDecimal("1499.00"), 20);
    }
}
