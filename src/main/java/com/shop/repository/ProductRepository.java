package com.shop.repository;

import com.shop.model.Product;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductRepository {

    private Map<Integer, Product> products = new HashMap<>();

    public void save(Product product) {
        products.put(product.getId(), product);
    }

    public Product findById(int id) {
        return products.get(id);
    }

    public List<Product> findAll() {
        Collection<Product> allProduct = products.values();
        return new ArrayList<>(allProduct);
    }

    public boolean deleteById(int id){
        Product p =  products.remove(id);
        return p != null;
    }

    public boolean update(Product product){
        if(products.containsKey(product.getId())){
            products.put(product.getId(), product);
            return true;
        }
        return  false;
    }

}
