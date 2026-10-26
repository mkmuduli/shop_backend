package com.shop;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.shop.config.DBConfig;
import com.shop.model.Product;
import com.shop.repository.ProductRepository;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) throws SQLException {

        ProductRepository pr = new ProductRepository();

        Product p2 = new Product("Keyboard", new BigDecimal("1499.00"), 20);

        pr.save(p2);

        // Optional<Product> result = pr.findById(1);

        // if (result.isPresent()) {
        // Product p1 = result.get();
        // System.out.println(p1);
        // }

        // Product p3 = result.orElse(p2);
        // System.out.println(p3);

        // result.map(Product::getId).ifPresent(System.out::println);

        List<Product> products = pr.findAll();
        System.out.println(products.size());

        List<Product> nextPage = pr.findAll(5, 5);
        System.out.println(nextPage.size());

    }
}
