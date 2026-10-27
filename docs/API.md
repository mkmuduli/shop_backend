# Shop API Reference

HTTP JSON API for managing products.

- **Base URL:** `http://localhost:4000`
- **Content type:** `application/json; charset=UTF-8` (all responses with a body)
- **Implementation:** `com.shop.controller.ProductHttpHandler`, registered at context `/products` in `com.shop.App`

All request and response bodies are JSON unless stated otherwise. No
authentication is required.

---

## Resources

### Product object

Returned by `POST /products` and `GET /products/{id}`.

| Field           | Type    | Constraints                              |
| --------------- | ------- | ---------------------------------------- |
| `id`            | integer | Server-generated, greater than zero      |
| `name`          | string  | Non-empty                                |
| `price`         | number  | Greater than zero (decimal)              |
| `stockQuantity` | integer | Zero or greater                          |

Example:

```json
{
  "id": 1,
  "name": "Coffee Mug",
  "price": 12.50,
  "stockQuantity": 40
}
```

### Product list response (paginated)

Returned by `GET /products`. See that endpoint for field descriptions.

```json
{
  "items": [
    { "id": 1, "name": "Coffee Mug", "price": 12.50, "stockQuantity": 40 }
  ],
  "page": 1,
  "limit": 20
}
```

### Error object

All errors use the same shape:

| Field    | Type   | Description        |
| -------- | ------ | ------------------ |
| `error`  | string | Human-readable message |

Example:

```json
{ "error": "Product not found" }
```

---

## Endpoints

### List products (paginated)

```
GET /products?page={page}&limit={limit}
```

Returns a page of products, ordered by `id` ascending. There is no total
count — clients page forward until `items` is empty (or shorter than
`limit`, which also means the last page has been reached).

**Query parameters** — both optional

| Param   | Type    | Default | Constraints |
| ------- | ------- | ------- | ----------- |
| `page`  | integer | `1`     | Must be an integer greater than `0` |
| `limit` | integer | `20`    | Must be an integer between `1` and `100` |

Blank or missing parameters fall back to their defaults.

**Response body**

| Field   | Type      | Description |
| ------- | --------- | ----------- |
| `items` | array     | `Product` objects for the current page (empty array once past the last product) |
| `page`  | integer   | Current page number, starting at `1` |
| `limit` | integer   | Page size used for this request |

**End-of-list detection** — a client has reached the last page when
`items` is empty **or** `items.length < limit`. To avoid one extra
request, stop as soon as `items.length < limit`.

**Responses**

| Status | When | Body |
| ------ | ---- | ---- |
| `200 OK` | Always (page past the end returns an empty `items` array) | `ProductListResponse` object |
| `400 Bad Request` | `page`/`limit` is not an integer (`Page must be an integer` / `Limit must be an integer`), `page` is `<= 0` (`Page must be greater than zero`), `limit` is outside `1..100` (`Limit must be between 1 and 100`), or `page` is so large that the offset overflows (`Page is out of range`) | `Error` object |
| `405 Method Not Allowed` | Method other than `GET`/`POST` on `/products` (`Method not allowed`) | `Error` object |
| `500 Internal Server Error` | Database or unexpected failure | `Error` object |

**Examples**

```http
GET /products?page=2&limit=10 HTTP/1.1
```

```http
HTTP/1.1 200 OK
Content-Type: application/json; charset=UTF-8

{
  "items": [
    {
      "id": 11,
      "name": "Coffee Mug",
      "price": 12.50,
      "stockQuantity": 40
    }
  ],
  "page": 2,
  "limit": 10
}
```

```http
GET /products HTTP/1.1
```

```http
HTTP/1.1 200 OK
Content-Type: application/json; charset=UTF-8

{ "items": [], "page": 1, "limit": 20 }
```

---

### Create a product

```
POST /products
```

Creates a new product and returns it with its generated `id`.

**Request body** — `CreateProductRequest`

| Field           | Type    | Required | Constraints                                  |
| --------------- | ------- | -------- | -------------------------------------------- |
| `name`          | string  | yes      | Must not be null or blank                     |
| `price`         | number  | yes      | Must not be null; must be greater than zero   |
| `stockQuantity` | integer | yes      | Must be zero or greater (missing defaults to `0`) |

**Responses**

| Status | When | Body |
| ------ | ---- | ---- |
| `201 Created` | Product created | `Product` object. Includes a `Location: /products/{id}` response header. |
| `400 Bad Request` | Malformed JSON body (`Invalid JSON request body`) or validation failure (see below) | `Error` object |
| `500 Internal Server Error` | Database or unexpected failure | `Error` object (`Internal server error`) |

**Validation failure messages**

- `Product name is required`
- `Product price must be greater than zero`
- `Stock quantity cannot be negative`

**Example**

```http
POST /products HTTP/1.1
Content-Type: application/json

{
  "name": "Coffee Mug",
  "price": 12.50,
  "stockQuantity": 40
}
```

```http
HTTP/1.1 201 Created
Content-Type: application/json; charset=UTF-8
Location: /products/1

{
  "id": 1,
  "name": "Coffee Mug",
  "price": 12.50,
  "stockQuantity": 40
}
```

---

### Get a product by ID

```
GET /products/{id}
```

Returns a single product.

**Path parameters**

| Param | Type    | Constraints |
| ----- | ------- | ----------- |
| `id`  | integer | Parseable integer greater than `0` |

**Responses**

| Status | When | Body |
| ------ | ---- | ---- |
| `200 OK` | Product exists | `Product` object |
| `400 Bad Request` | `{id}` is not an integer (`Product ID must be an integer`) or is `<= 0` (`Product ID must be greater than zero`) | `Error` object |
| `404 Not Found` | No product with that ID (`Product not found`) | `Error` object |
| `405 Method Not Allowed` | Method other than `GET`/`DELETE` on `/products/{id}` (`Method not allowed`) | `Error` object |
| `500 Internal Server Error` | Database or unexpected failure | `Error` object |

**Example**

```http
GET /products/1 HTTP/1.1
```

```http
HTTP/1.1 200 OK
Content-Type: application/json; charset=UTF-8

{
  "id": 1,
  "name": "Coffee Mug",
  "price": 12.50,
  "stockQuantity": 40
}
```

---

### Delete a product by ID

```
DELETE /products/{id}
```

Deletes a product.

**Path parameters**

| Param | Type    | Constraints |
| ----- | ------- | ----------- |
| `id`  | integer | Parseable integer greater than `0` |

**Responses**

| Status | When | Body |
| ------ | ---- | ---- |
| `204 No Content` | Product deleted | None |
| `400 Bad Request` | `{id}` is not an integer (`Product ID must be an integer`) or is `<= 0` (`Product ID must be greater than zero`) | `Error` object |
| `404 Not Found` | No product with that ID (`Product not found`) | `Error` object |
| `405 Method Not Allowed` | Method other than `GET`/`DELETE` on `/products/{id}` (`Method not allowed`) | `Error` object |
| `500 Internal Server Error` | Database or unexpected failure | `Error` object |

**Example**

```http
DELETE /products/1 HTTP/1.1
```

```http
HTTP/1.1 204 No Content
```

---

## Unmatched requests

| Request | Status | Body |
| ------- | ------ | ---- |
| Any path other than `/products` or `/products/{id}` | `404 Not Found` | `{"error": "Route not found"}` |

Note: methods other than `GET`/`POST` on `/products` return
`405 Method not allowed` (including e.g. `PUT /products`).

---

## Source of truth

| Endpoint | Handler method | Service method |
| -------- | -------------- | -------------- |
| `GET /products` | `ProductHttpHandler#listProducts` | `ProductService#getProducts` |
| `POST /products` | `ProductHttpHandler#createProduct` | `ProductService#createProduct` |
| `GET /products/{id}` | `ProductHttpHandler#getProduct` | `ProductService#getProductById` |
| `DELETE /products/{id}` | `ProductHttpHandler#deleteProduct` | `ProductService#deleteProduct` |
