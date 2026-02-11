# notifications-webhook-service-divv

Senior Software Engineer technical challenge: **event notification delivery via HTTPS webhooks** and a **self-service REST API**, implemented with **Java + Spring Boot** following **Hexagonal Architecture**.

---

## Demo Video

Watch the full end-to-end demo here:

https://video-link

---

## Context (from the challenge)
The platform must:
- Deliver event notifications via **webhook** to a specific URL.
- Ensure **client isolation** (events belong to the correct client).
- Handle errors with an efficient **retry strategy** (implemented as explicit replay; no async/queues).
- Store final delivery information.
- Be **observable** in a **near real-time** approach.
- Expose a self-service REST API:
  - `GET /notification_events`
  - `GET /notification_events/{notification_event_id}`
  - `POST /notification_events/{notification_event_id}/replay`

Dataset: `src/main/resources/notification_events.json` (read-only snapshot).

---

## Frozen decisions (scope constraints)
- `notification_events.json` is immutable.
- JSON is the source of truth for events.
- No queues / no async flows.
- `delivery_status` is only `COMPLETED | FAILED` (domain).
- Postgres stores only **metadata of delivery attempts**, never event truth.
- Replay is allowed **only** for `FAILED` events.
- GET endpoints do not depend on Postgres.
- Dates normalized to UTC.
- RFC 7807 error format.

---

## Local setup philosophy
**Only Docker is required on the host.**
- Java 21 and Maven run **inside a dev container**.
- PostgreSQL, Prometheus and Grafana run as services in `docker-compose`.
- The application is executed **inside the dev container**.

> All commands below assume you are in the repository root folder.

---

## Repository root
```bash
cd /notifications-webhook-service-divv
```

---

## Quick start

### 1) Set webhook target URL (host)
```bash
export WEBHOOK_TARGET_URL="https://webhook.site/REPLACE_ME"
```

### 2) Start containers (host)
```bash
docker compose down
docker compose up -d --build
docker compose ps
```

### 3) Build and run the application (inside the dev container)
In one terminal:
```bash
docker compose exec dev mvn -q -DskipTests clean package
docker compose exec dev mvn -q -DskipTests spring-boot:run
```

### 4) Health check (host)
```bash
curl -s http://localhost:8080/actuator/health
```

---

## Configuration

### Database (PostgreSQL)
Defaults are provided via environment variables:
- `DB_URL` (default: `jdbc:postgresql://postgres:5432/notifications`)
- `DB_USER` (default: `notifications`)
- `DB_PASSWORD` (default: `notifications`)

### Webhook target
The webhook target URL is provided via environment variable:
- `WEBHOOK_TARGET_URL`

The actual webhook URL is expected to be supplied during the presentation/demo.

---

## Architecture (Hexagonal)

High-level structure:
- `domain/` — Core business rules and models (no framework dependencies)
- `domain/ports/in` — Use case interfaces (inbound ports)
- `domain/ports/out` — External dependencies as interfaces (outbound ports)
- `adapters/in/rest` — REST controllers
- `adapters/out/*` — JSON snapshot reader, persistence, webhook sender
- `config/` — Spring wiring and configuration

---

## API

### 1) List notifications (JSON source of truth)
Always requires `client_id`:
```bash
curl -i "http://localhost:8080/notification_events?client_id=CLIENT001"
```

Optional filters:
- `delivery_status=COMPLETED|FAILED`
- `date_from` and `date_to` over `delivery_date`
  - Supports `yyyy-MM-dd` and ISO-8601 Instant
  - `date_to` is inclusive

Examples:
```bash
curl -i "http://localhost:8080/notification_events?client_id=CLIENT001&delivery_status=FAILED"
curl -i "http://localhost:8080/notification_events?client_id=CLIENT001&date_from=2024-03-15&date_to=2024-03-16"
curl -i "http://localhost:8080/notification_events?client_id=CLIENT003&date_from=2024-03-15&date_to=2024-03-15"
```

### 2) Get event detail (JSON source of truth)
```bash
curl -i "http://localhost:8080/notification_events/EVT003?client_id=CLIENT002"
```

Ownership is enforced:
```bash
curl -i "http://localhost:8080/notification_events/EVT003?client_id=CLIENT001"
```
Expected: `404`.

### 3) Replay (only FAILED)
Successful replay (event must be FAILED and belong to client):
```bash
curl -i -X POST "http://localhost:8080/notification_events/EVT003/replay?client_id=CLIENT002"
```

Replay rejected (event COMPLETED):
```bash
curl -i -X POST "http://localhost:8080/notification_events/EVT001/replay?client_id=CLIENT001"
```
Expected: `409`.

---

## Idempotency (Replay)
Replay supports an optional `Idempotency-Key` header.

Behavior:
- If a previous attempt exists for `(event_id, client_id, correlation_id)`:
  - The webhook is **not resent**
  - The stored attempt timestamp is reused in the response
- Otherwise:
  - The webhook is sent
  - A new delivery attempt row is stored in Postgres

Example:
```bash
curl -i -X POST   -H "Idempotency-Key: KEY-456"   "http://localhost:8080/notification_events/EVT003/replay?client_id=CLIENT002"

curl -i -X POST   -H "Idempotency-Key: KEY-456"   "http://localhost:8080/notification_events/EVT003/replay?client_id=CLIENT002"
```

---

## Delivery attempts (Postgres metadata)
Each replay generates one row in `delivery_attempts`:
- `SUCCESS` if HTTP status is 2xx
- `FAILURE` if exception or non-2xx

Inspect:
```bash
docker compose exec postgres psql -U notifications -d notifications -c "SELECT * FROM delivery_attempts ORDER BY attempted_at DESC;"
```

---

## Metrics & Logs (Micrometer + Actuator)

### Prometheus endpoint
```bash
curl -s http://localhost:8080/actuator/prometheus | grep -E "webhook_delivery_attempts_total|webhook_delivery_latency_seconds|notification_replay_total"
```

Exported metrics:
- `webhook_delivery_attempts_total{result=success|failure}`
- `webhook_delivery_latency_seconds`
- `notification_replay_total{result=accepted|rejected|not_found|failure}`

### Structured logs
A structured log line is emitted per webhook attempt:
- `event_id`
- `client_id`
- `event_type`
- `result`
- `http_status`
- `duration_ms`
- `target_url`
- `correlation_id`

---

## Observability (Prometheus + Grafana)

### Prometheus UI
- Open: `http://localhost:9090`
- Example queries:
  - `webhook_delivery_attempts_total`
  - `notification_replay_total`

### Grafana UI
- Open: `http://localhost:3000`
- Default credentials:
  - user: `admin`
  - password: `admin`
- Prometheus datasource is auto-provisioned as default.
- Example panels:
  - `rate(webhook_delivery_attempts_total[5m])`
  - `rate(notification_replay_total[5m])`
  - `webhook_delivery_latency_seconds_sum / webhook_delivery_latency_seconds_count`

---

## Documentation

- [System Design](DESIGN.md)
- [Security Analysis](SECURITY.md)
- [AI Usage Documentation](AI_USAGE.md)

---

## Branching
- Work completed in `main`.
