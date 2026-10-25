package com.shop.model;

import java.math.BigDecimal;

public class Product {
    private int id;
    private String name;
    private BigDecimal price;
    private int stockQuantity;

    public Product(int id, String name, BigDecimal price, int stockQuantity) {

        if(name == null || name.isEmpty()){
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if(price == null || price.compareTo(BigDecimal.ZERO) < 0){
            throw new IllegalArgumentException("Product price cannot be null or negative");
        }
        if( stockQuantity < 0){
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }

        this.id = id;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", stockQuantity=" + stockQuantity +
                '}';
    }

    public Integer getId() {
        return this.id;
    }
}
