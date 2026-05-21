# Observability

Both services export logs, traces, and metrics to a shared **Grafana LGTM** stack using OpenTelemetry.

---

## How It Works

Each service uses the `com.minicdesign.spring-otel-logging` convention plugin which:

1. Adds Spring Boot Actuator, OpenTelemetry, and Micrometer OTLP dependencies.
2. Auto-generates `InstallOpenTelemetryAppender` — routes Logback log events into the OTLP pipeline.
3. Auto-generates `TraceIdFilter` — appends `X-Trace-Id` to every HTTP response.
4. Configures a Logback `OpenTelemetryAppender` alongside the console appender.

`spring.application.name` in each service's `application.yml` is picked up by Spring Boot's auto-configuration and set as the `service.name` OTLP resource attribute. This is what Loki and Tempo use to identify which service emitted each log/trace.

---

## Grafana UI

Navigate to **http://localhost:3000** (credentials: `admin` / `admin`).

---

## Loki — Log Queries

In **Grafana → Explore → Loki**:

### All logs from inventory-service
```logql
{service_name="inventory-service"}
```

### All logs from order-service
```logql
{service_name="order-service"}
```

### Logs for a specific trace ID (copy from `X-Trace-Id` response header)
```logql
{service_name="order-service"} | json | traceId="<paste-trace-id-here>"
```

### WARNING and ERROR logs across both services
```logql
{service_name=~"order-service|inventory-service"} | json | level=~"WARN|ERROR"
```

### Logs for a specific product ID
```logql
{service_name="order-service"} |= "PROD-001"
```

---

## Tempo — Trace Search

In **Grafana → Explore → Tempo**:

### Search by trace ID
The `X-Trace-Id` header is returned on every response. Copy the value and paste it into the **TraceQL** search:

```traceql
{ traceID = "<paste-trace-id-here>" }
```

### Find all traces that span both services
```traceql
{ resource.service.name = "order-service" } >> { resource.service.name = "inventory-service" }
```

### Find slow order traces (> 500ms)
```traceql
{ resource.service.name = "order-service" && duration > 500ms }
```

### Find traces with errors in either service
```traceql
{ resource.service.name =~ "order-service|inventory-service" && status = error }
```

---

## Metrics — Prometheus / Mimir

Spring Boot Actuator exposes metrics at:

- `http://localhost:8081/actuator/metrics` — inventory-service
- `http://localhost:8082/actuator/metrics` — order-service

In **Grafana → Explore → Mimir (Prometheus)**, useful queries:

### HTTP request rate for order-service
```promql
rate(http_server_requests_seconds_count{service="order-service"}[1m])
```

### JVM heap usage
```promql
jvm_memory_used_bytes{area="heap", service_name="inventory-service"}
```
