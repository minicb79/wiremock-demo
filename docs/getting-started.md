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

## 2. Start the Docker stacks

### Start the Grafana Observability Stack

```bash
./gradlew startGrafana
```

This starts the `grafana/otel-lgtm` container providing:

| Component  | URL                                   |
|------------|---------------------------------------|
| Grafana UI | http://localhost:3000 (admin / admin) |
| OTLP gRPC  | localhost:4317                        |
| OTLP HTTP  | localhost:4318                        |

To check the stack status:

```bash
./gradlew checkGrafana
```

### Start the WireMock Server

```bash
./gradlew startWiremock
```

This starts the `wiremock/wiremock:2.35.0` container on port `8080` with the following mappings:

| Component       | URL                                  |
|-----------------|--------------------------------------|
| WireMock        | http://localhost:8092                |
| WireMock Web UI | http://localhost:8092/__admin/webapp |

To check the stack status:

```bash
./gradlew checkWiremock
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

### Feature Flags & Startup Validation

The `order-service` includes two feature flags to control mocking behaviors:
- `feature.wiremock-via-interceptor`: Diverts specific product requests to WireMock via an HTTP client interceptor.
- `feature.wiremock-as-proxy`: Routes all external calls as a proxy.

#### Validation Rules

At application startup (during context refresh), a `FeatureFlagValidator` checks these flags:
1. **NAND Constraint**: `feature.wiremock-via-interceptor` and `feature.wiremock-as-proxy` cannot both be `true` at the same time.
2. **Production Safety**: Neither mock feature flag is allowed to be `true` when running with the `prod` Spring profile.

If any rule is violated, startup aborts immediately before binding to HTTP ports.

#### Customizing Flags at Runtime

You can pass command-line arguments to override these flags or active profiles:

```bash
# Running order-service with both flags enabled (fails fast)
./gradlew :order-service:bootRun --args="--feature.wiremock-via-interceptor=true --feature.wiremock-as-proxy=true"

# Running under prod profile with a mocking flag enabled (fails fast)
./gradlew :order-service:bootRun --args="--spring.profiles.active=prod --feature.wiremock-via-interceptor=true"
```

---

## 4. Exercise the APIs

### Check inventory for a product

```bash
curl -s http://localhost:8081/v1/inventory/PROD-001 | jq
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
curl -s -X POST http://localhost:8082/v1/orders \
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
curl -s -X POST http://localhost:8082/v1/orders \
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
curl -s -X POST http://localhost:8082/v1/orders \
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

Services can be stopped with `Ctrl+C` in each terminal.

```bash
./gradlew stopWiremock
./gradlew stopGrafana
```
