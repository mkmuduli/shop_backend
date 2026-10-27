package com.shop.model;

import java.util.List;

public record ProductListResponse(
        List<Product> items,
        int page,
        int limit) {
}
