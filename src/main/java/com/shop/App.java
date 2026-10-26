package com.shop;

import com.shop.controller.ProductHttpHandler;
import com.shop.repository.ProductRepository;
import com.shop.service.ProductService;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class App {
    public static void main(String[] args) throws IOException {
        ProductRepository repository = new ProductRepository();
        ProductService service = new ProductService(repository);

        HttpServer server = HttpServer.create(
                new InetSocketAddress(4000), 0);

        server.createContext(
                "/products",
                new ProductHttpHandler(service));

        server.start();

        System.out.println("server started at 4000");

    }
}
