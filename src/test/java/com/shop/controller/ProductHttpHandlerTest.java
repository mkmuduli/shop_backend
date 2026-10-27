package com.shop.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.shop.DatabaseMigrator;
import com.shop.repository.ProductRepository;
import com.shop.service.ProductService;
import com.sun.net.httpserver.HttpServer;

class ProductHttpHandlerTest {

    private static HttpServer server;
    private static HttpClient client;
    private static String base;

    @BeforeAll
    static void startServer() throws IOException {
        DatabaseMigrator.migrate();

        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/products",
                new ProductHttpHandler(
                        new ProductService(new ProductRepository())));
        server.start();

        base = "http://localhost:" + server.getAddress().getPort();
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @AfterAll
    static void stopServer() {
        server.stop(0);
    }

    // ---- successful requests ----

    @Test
    void shouldCreateProductAndReturn201WithLocation() throws Exception {
        String name = uniqueName("HandlerTest");
        HttpResponse<String> response = send("POST", "/products",
                "{\"name\":\"" + name + "\",\"price\":9.99,\"stockQuantity\":3}");

        assertEquals(201, response.statusCode());
        assertTrue(response.headers().firstValue("Location")
                .orElse("").matches("/products/\\d+"));
        assertTrue(response.body().contains("\"name\":\"" + name + "\""));

        // cleanup
        String id = response.body()
                .replaceAll(".*\"id\":(\\d+).*", "$1");
        assertEquals(204, send("DELETE", "/products/" + id, null).statusCode());
    }

    @Test
    void shouldReturnProductThatExists() throws Exception {
        String name = uniqueName("HandlerTest");
        HttpResponse<String> created = send("POST", "/products",
                "{\"name\":\"" + name + "\",\"price\":1.50,\"stockQuantity\":1}");
        String id = created.body().replaceAll(".*\"id\":(\\d+).*", "$1");
        try {
            HttpResponse<String> response = send("GET", "/products/" + id, null);

            assertEquals(200, response.statusCode());
            assertTrue(response.body()
                    .startsWith("{\"id\":" + id + ","));
        } finally {
            send("DELETE", "/products/" + id, null);
        }
    }

    @Test
    void shouldListProductsWithPagination() throws Exception {
        HttpResponse<String> response = send("GET", "/products?limit=1", null);

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"items\":["));
        assertTrue(response.body().contains("\"page\":1"));
        assertTrue(response.body().contains("\"limit\":1"));
    }

    @Test
    void shouldReturn204WhenDeletingExistingProduct() throws Exception {
        String name = uniqueName("HandlerTest");
        HttpResponse<String> created = send("POST", "/products",
                "{\"name\":\"" + name + "\",\"price\":1.50,\"stockQuantity\":1}");
        String id = created.body().replaceAll(".*\"id\":(\\d+).*", "$1");

        assertEquals(204, send("DELETE", "/products/" + id, null).statusCode());
    }

    // ---- product does not exist ----

    @Test
    void shouldReturn404WhenGetProductDoesNotExist() throws Exception {
        HttpResponse<String> response =
                send("GET", "/products/999999999", null);

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Product not found"));
    }

    @Test
    void shouldReturn404WhenDeleteProductDoesNotExist() throws Exception {
        HttpResponse<String> response =
                send("DELETE", "/products/999999999", null);

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Product not found"));
    }

    // ---- invalid input ----

    @Test
    void shouldReturn400ForMalformedJsonBody() throws Exception {
        HttpResponse<String> response =
                send("POST", "/products", "{invalid json");

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Invalid JSON request body"));
    }

    @Test
    void shouldReturn400ForMissingName() throws Exception {
        HttpResponse<String> response = send("POST", "/products",
                "{\"name\":\"  \",\"price\":9.99,\"stockQuantity\":3}");

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Product name is required"));
    }

    @Test
    void shouldReturn400ForNonPositivePrice() throws Exception {
        HttpResponse<String> response = send("POST", "/products",
                "{\"name\":\"Valid\",\"price\":0,\"stockQuantity\":3}");

        assertEquals(400, response.statusCode());
        assertTrue(response.body()
                .contains("Product price must be greater than zero"));
    }

    @Test
    void shouldReturn400ForNegativeStockQuantity() throws Exception {
        HttpResponse<String> response = send("POST", "/products",
                "{\"name\":\"Valid\",\"price\":9.99,\"stockQuantity\":-1}");

        assertEquals(400, response.statusCode());
        assertTrue(response.body()
                .contains("Stock quantity cannot be negative"));
    }

    @Test
    void shouldReturn400ForNonIntegerProductId() throws Exception {
        HttpResponse<String> response = send("GET", "/products/abc", null);

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Product ID must be an integer"));
    }

    @Test
    void shouldReturn400ForNonPositiveProductId() throws Exception {
        assertEquals(400, send("GET", "/products/0", null).statusCode());
        assertTrue(send("GET", "/products/0", null).body()
                .contains("Product ID must be greater than zero"));
    }

    @Test
    void shouldReturn400ForInvalidPaginationParams() throws Exception {
        assertEquals(400, send("GET", "/products?page=abc", null).statusCode());
        assertTrue(send("GET", "/products?page=abc", null).body()
                .contains("Page must be an integer"));

        assertTrue(send("GET", "/products?page=0", null).body()
                .contains("Page must be greater than zero"));

        assertTrue(send("GET", "/products?limit=101", null).body()
                .contains("Limit must be between 1 and 100"));
    }

    @Test
    void shouldReturn405ForUnsupportedMethodOnCollection()
            throws Exception {
        HttpResponse<String> response = send("PUT", "/products", "{}");

        assertEquals(405, response.statusCode());
        assertTrue(response.body().contains("Method not allowed"));
    }

    // ---- helpers ----

    private static String uniqueName(String prefix) {
        return prefix + "-" + System.nanoTime();
    }

    private static HttpResponse<String> send(
            String method, String path, String jsonBody) throws Exception {

        HttpRequest.Builder builder = HttpRequest
                .newBuilder(URI.create(base + path))
                .timeout(Duration.ofSeconds(10));

        if (jsonBody == null) {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            builder.header("Content-Type", "application/json");
            builder.method(method,
                    HttpRequest.BodyPublishers.ofString(jsonBody));
        }

        return client.send(builder.build(),
                HttpResponse.BodyHandlers.ofString());
    }
}
