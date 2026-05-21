# wiremock-demo

A multimodule Spring Boot demonstration project showcasing:

- **Hexagonal Architecture** (Ports & Adapters) across both services
- **OpenTelemetry** distributed tracing and logging via the `com.minicdesign.spring-otel-logging` convention plugin
- **OpenAPI code generation** via the `com.minicdesign.api-generation` convention plugin
- **Grafana LGTM** (Loki, Grafana, Tempo, Mimir) for unified observability

## Services

| Service | Port | Description |
|---|---|---|
| `inventory-service` | `8081` | Returns stock availability for products |
| `order-service` | `8082` | Places orders; calls inventory-service to verify stock |

## Documentation

- [Getting Started](getting-started.md) — build, run, and test locally
- [Observability](observability.md) — Grafana, Loki, Tempo queries
- [API Reference](api-reference.md) — endpoint examples with `curl`

## Architecture

Both services follow **Hexagonal Architecture**:

```
src/main/kotlin/.../
├── domain/              # Pure business logic — no framework dependencies
│   ├── model/           # Domain entities and value objects
│   └── port/
│       ├── in/          # Inbound ports (use case interfaces)
│       └── out/         # Outbound ports (repository/client interfaces)
├── application/         # Orchestrates domain logic
│   └── service/         # Use case implementations (Spring @Service)
└── adapter/             # Framework/infrastructure code
    ├── in/
    │   └── web/         # Spring REST controllers
    └── out/
        ├── http/        # RestClient HTTP adapters
        └── persistence/ # In-memory / database adapters
```
