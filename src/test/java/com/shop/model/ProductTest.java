package com.shop.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

public class ProductTest {

    @Test
    void shouldCreateProductWithValidDetails() {
        Product product = new Product(1, "Keyboard", new BigDecimal("1499.00"), 20);

        assertEquals(1, product.getId());
    }

    @Test
    void shouldNotCreateProductWithNegativeStockQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> new Product(1, "Keyboard", new BigDecimal("1499.00"), -20));
    }

    @Test
    void shouldNotCreateProductWithNegativePrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new Product(1, "Keyboard", new BigDecimal("-1.00"), 20));
    }

    @Test
    void shouldNotCreateProductWithBlankName() {
        assertThrows(IllegalArgumentException.class,
                () -> new Product(1, "", new BigDecimal("1499.00"), 20));
    }
}
