package com.shop.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.model.ApiError;
import com.shop.model.CreateProductRequest;
import com.shop.model.Product;
import com.shop.model.ProductListResponse;
import com.shop.service.ProductService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ProductHttpHandler implements HttpHandler {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_LIMIT = 20;

    private final ProductService productService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProductHttpHandler(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if (path.equals("/products")) {
                if (method.equals("POST")) {
                    createProduct(exchange);
                    return;
                }

                if (method.equals("GET")) {
                    listProducts(exchange);
                    return;
                }

                sendJson(exchange, 405,
                        new ApiError("Method not allowed"));
                return;
            }

            if (path.matches("/products/[^/]+")) {
                String idPart = path.substring("/products/".length());

                int id;
                try {
                    id = Integer.parseInt(idPart);
                } catch (NumberFormatException e) {
                    sendJson(exchange, 400,
                            new ApiError("Product ID must be an integer"));
                    return;
                }

                if (method.equals("GET")) {
                    getProduct(exchange, id);
                    return;
                }

                if (method.equals("DELETE")) {
                    deleteProduct(exchange, id);
                    return;
                }

                sendJson(exchange, 405,
                        new ApiError("Method not allowed"));
                return;
            }

            sendJson(exchange, 404, new ApiError("Route not found"));
        } catch (IllegalArgumentException e) {
            sendJson(exchange, 400, new ApiError(e.getMessage()));
        } catch (SQLException e) {
            sendJson(exchange, 500,
                    new ApiError("Internal server error"));
        } catch (RuntimeException e) {
            sendJson(exchange, 500, new ApiError("Internal server error"));
        } finally {
            exchange.close();
        }
    }

    private void getProduct(HttpExchange exchange, int id)
            throws IOException, SQLException {

        Optional<Product> product = productService.getProductById(id);

        if (product.isPresent()) {
            sendJson(exchange, 200, product.get());
        } else {
            sendJson(exchange, 404,
                    new ApiError("Product not found"));
        }
    }

    private void createProduct(HttpExchange exchange)
            throws IOException, SQLException {

        CreateProductRequest request;

        try {
            request = objectMapper.readValue(
                    exchange.getRequestBody(),
                    CreateProductRequest.class);
        } catch (JsonProcessingException e) {
            sendJson(exchange, 400,
                    new ApiError("Invalid JSON request body"));
            return;
        }

        Product product = new Product(
                request.name(),
                request.price(),
                request.stockQuantity());

        Product created = productService.createProduct(product);

        exchange.getResponseHeaders().set(
                "Location", "/products/" + created.getId());

        sendJson(exchange, 201, created);
    }

    private void listProducts(HttpExchange exchange)
            throws IOException, SQLException {

        Map<String, String> params = parseQuery(
                exchange.getRequestURI().getQuery());

        int page = parseIntParam(
                params.get("page"), "Page", DEFAULT_PAGE);
        int limit = parseIntParam(
                params.get("limit"), "Limit", DEFAULT_LIMIT);

        ProductListResponse response = productService.getProducts(page, limit);

        sendJson(exchange, 200, response);
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();

        if (query == null || query.isBlank()) {
            return params;
        }

        for (String part : query.split("&")) {
            int eq = part.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            params.put(part.substring(0, eq), part.substring(eq + 1));
        }

        return params;
    }

    private int parseIntParam(String value, String name, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    name + " must be an integer");
        }
    }

    private void deleteProduct(HttpExchange exchange, int id)
            throws IOException, SQLException {

        boolean deleted = productService.deleteProduct(id);

        if (!deleted) {
            sendJson(exchange, 404,
                    new ApiError("Product not found"));
            return;
        }

        exchange.sendResponseHeaders(204, -1);
    }

    private void sendJson(
            HttpExchange exchange,
            int statusCode,
            Object body) throws IOException {

        byte[] response = objectMapper.writeValueAsBytes(body);

        exchange.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8");

        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
    }

}
