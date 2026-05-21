# Getting Started

## Prerequisites

- **JDK 21+** (or JDK 25 for full compatibility with the conventions)
- **Docker** (for the Grafana observability stack)
- **Gradle** wrapper included (`./gradlew`)

---

## 1. Build the Project

From the `wiremock-demo` root:

```bash
./gradlew build
```

This will:
- Run OpenAPI code generation for both services (`generateOpenApiInventoryApi`, `generateOpenApiOrderApi`)
- Compile all Kotlin/Java sources
- Run unit tests

---

## 2. Start the Grafana Observability Stack

```bash
./gradlew startGrafana
```

This starts the `grafana/otel-lgtm` container providing:

| Component | URL |
|---|---|
| Grafana UI | http://localhost:3000 (admin / admin) |
| OTLP gRPC | localhost:4317 |
| OTLP HTTP | localhost:4318 |

To check the stack status:

```bash
./gradlew checkGrafana
```

---

## 3. Run the Services

In two separate terminals:

```bash
# Terminal 1 — Inventory Service (port 8081)
./gradlew :inventory-service:bootRun

# Terminal 2 — Order Service (port 8082)
./gradlew :order-service:bootRun
```

---

## 4. Exercise the APIs

### Check inventory for a product

```bash
curl -s http://localhost:8081/inventory/PROD-001 | jq
```

Expected response:
```json
{
  "productId": "PROD-001",
  "quantity": 100,
  "available": true
}
```

### Place a successful order

```bash
curl -s -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": "PROD-001", "quantity": 5}' | jq
```

Expected response:
```json
{
  "orderId": "ORD-xxxxxxxx",
  "productId": "PROD-001",
  "quantity": 5,
  "status": "CONFIRMED"
}
```

### Place an order rejected due to no stock

```bash
curl -s -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": "PROD-003", "quantity": 1}' | jq
```

Expected response (`409 Conflict`):
```json
{
  "orderId": "ORD-xxxxxxxx",
  "productId": "PROD-003",
  "quantity": 1,
  "status": "REJECTED"
}
```

### Place an order for an unknown product

```bash
curl -s -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": "PROD-999", "quantity": 1}' | jq
```

Expected response (`404 Not Found`):
```json
{
  "code": "NOT_FOUND",
  "message": "Product not found: PROD-999"
}
```

---

## 5. Stop Everything

```bash
./gradlew stopGrafana
```

Services can be stopped with `Ctrl+C` in each terminal.
