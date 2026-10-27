package com.shop;

import com.shop.config.DBConfig;
import com.shop.controller.ProductHttpHandler;
import com.shop.repository.ProductRepository;
import com.shop.service.ProductService;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class App {
    private static final int PORT = 4000;

    public static void main(String[] args) throws IOException {
        DatabaseMigrator.migrate();

        ProductRepository repository = new ProductRepository();
        ProductService service = new ProductService(repository);

        HttpServer server = HttpServer.create(
                new InetSocketAddress(PORT), 0);

        server.createContext(
                "/products",
                new ProductHttpHandler(service));

        server.start();

        System.out.println("server started at " + PORT);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // stop accepting new exchanges; allow up to 1s for in-flight ones
            server.stop(1);
            DBConfig.shutdown();
            System.out.println("server stopped, connection pool closed");
        }));
    }
}
