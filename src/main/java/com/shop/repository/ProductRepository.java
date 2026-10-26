package com.shop.repository;

import com.shop.config.DBConfig;
import com.shop.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductRepository {

    public void save(Product product) throws SQLException {
        String sql = """
                INSERT INTO products ( name, price, stock_quantity)
                VALUES ( ?, ?, ?)
                """;

        try (Connection c1 = DBConfig.getConnection();
                PreparedStatement statement = c1.prepareStatement(sql);) {

            statement.setString(1, product.getName());
            statement.setBigDecimal(2, product.getPrice());
            statement.setInt(3, product.getStockQuantity());

            statement.executeUpdate();
        }

    }

    public Optional<Product> findById(int id) throws SQLException {
        String sql = """
                SELECT id, name, price, stock_quantity
                FROM products
                WHERE id = ?
                """;
        try (Connection c1 = DBConfig.getConnection();
                PreparedStatement statement = c1.prepareStatement(sql);) {
            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery();) {
                if (rs.next()) {
                    Product p1 = new Product(rs.getInt("id"), rs.getString("name"), rs.getBigDecimal("price"),
                            rs.getInt("stock_quantity"));
                    return Optional.of(p1);
                }
                return Optional.empty();
            }

        }
    }

    public List<Product> findAll() throws SQLException {
        return findAll(20, 0);
    }

    public List<Product> findAll(int limit, int offset) throws SQLException {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }

        if (offset < 0) {
            throw new IllegalArgumentException("Offset cannot be negative");
        }

        String sql = """
                SELECT id, name, price, stock_quantity
                FROM products
                ORDER BY id
                LIMIT ? OFFSET ?
                """;
        try (Connection c1 = DBConfig.getConnection();
                PreparedStatement statement = c1.prepareStatement(sql);) {
            statement.setInt(1, limit);
            statement.setInt(2, offset);

            try (ResultSet rs = statement.executeQuery();) {
                List<Product> productList = new ArrayList<>();
                while (rs.next()) {
                    productList.add(new Product(rs.getInt("id"), rs.getString("name"), rs.getBigDecimal("price"),
                            rs.getInt("stock_quantity")));

                }
                return productList;
            }

        }
    }

    public boolean deleteById(int id) throws SQLException {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection c1 = DBConfig.getConnection();
                PreparedStatement statement = c1.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean update(Product product) throws SQLException {
        String sql = """
                UPDATE products
                SET name = ?, price = ?, stock_quantity = ?
                WHERE id = ?
                """;
        try (Connection c1 = DBConfig.getConnection();
                PreparedStatement statement = c1.prepareStatement(sql)) {
            statement.setString(1, product.getName());
            statement.setBigDecimal(2, product.getPrice());
            statement.setInt(3, product.getStockQuantity());
            statement.setInt(4, product.getId());
            return statement.executeUpdate() > 0;
        }
    }

}
