# API Reference

---

## inventory-service — `http://localhost:8081`

### GET `/inventory/{productId}`

Returns the current stock availability for a product.

**Path Parameters**

| Parameter | Type | Description |
|---|---|---|
| `productId` | string | The unique product identifier |

**Responses**

| Status | Description |
|---|---|
| `200 OK` | Product found, returns inventory details |
| `404 Not Found` | Product does not exist |

**Example — product in stock**

```bash
curl -s http://localhost:8081/inventory/PROD-001 | jq
```

```json
{
  "productId": "PROD-001",
  "quantity": 100,
  "available": true
}
```

**Example — product out of stock**

```bash
curl -s http://localhost:8081/inventory/PROD-003 | jq
```

```json
{
  "productId": "PROD-003",
  "quantity": 0,
  "available": false
}
```

**Example — product not found**

```bash
curl -s http://localhost:8081/inventory/PROD-999 | jq
```

```json
{
  "code": "NOT_FOUND",
  "message": "Product PROD-999 not found."
}
```

**Seeded test products**

| productId | quantity | available |
|---|---|---|
| `PROD-001` | 100 | true |
| `PROD-002` | 5 | true |
| `PROD-003` | 0 | false |

---

## order-service — `http://localhost:8082`

### POST `/orders`

Places a new order. Internally calls `inventory-service` to check stock availability before confirming.

**Request Body** (`application/json`)

| Field | Type | Required | Description |
|---|---|---|---|
| `productId` | string | yes | Product to order |
| `quantity` | integer | yes | Number of units (minimum: 1) |

**Responses**

| Status | Description |
|---|---|
| `201 Created` | Order confirmed — sufficient stock available |
| `409 Conflict` | Order rejected — insufficient stock |
| `404 Not Found` | Product does not exist in inventory |

**Example — confirmed order**

```bash
curl -s -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": "PROD-001", "quantity": 5}' | jq
```

```json
{
  "orderId": "ORD-abc12345",
  "productId": "PROD-001",
  "quantity": 5,
  "status": "CONFIRMED"
}
```

**Example — rejected order (no stock)**

```bash
curl -s -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": "PROD-003", "quantity": 1}' | jq
```

```json
{
  "orderId": "ORD-xyz98765",
  "productId": "PROD-003",
  "quantity": 1,
  "status": "REJECTED"
}
```

**Example — product not found**

```bash
curl -s -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": "PROD-999", "quantity": 1}' | jq
```

```json
{
  "code": "NOT_FOUND",
  "message": "Product not found: PROD-999"
}
```

**Tip**: The `X-Trace-Id` response header contains the distributed trace ID. Use it in Grafana → Tempo to view the full cross-service trace.

```bash
curl -si -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": "PROD-001", "quantity": 2}' | grep -i x-trace-id
```
