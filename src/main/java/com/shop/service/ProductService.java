package com.shop.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

import com.shop.model.Product;
import com.shop.repository.ProductRepository;

public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // GET /products/{id}
    public Optional<Product> getProductById(int id)
            throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Product ID must be greater than zero");
        }

        return productRepository.findById(id);
    }

    // POST /products
    public Product createProduct(Product product)
            throws SQLException {

        validateProduct(product);

        int generatedId = productRepository.save(product);

        return new Product(
                generatedId,
                product.getName(),
                product.getPrice(),
                product.getStockQuantity());
    }

    // DELETE /products/{id}
    public boolean deleteProduct(int id)
            throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Product ID must be greater than zero");
        }

        return productRepository.deleteById(id);
    }

    private void validateProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException(
                    "Product cannot be null");
        }

        if (product.getName() == null
                || product.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "Product name is required");
        }

        if (product.getPrice() == null
                || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Product price must be greater than zero");
        }

        if (product.getStockQuantity() < 0) {
            throw new IllegalArgumentException(
                    "Stock quantity cannot be negative");
        }
    }
}
