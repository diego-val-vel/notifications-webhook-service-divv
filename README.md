# notifications-webhook-service-divv

Senior Software Engineer technical challenge: **event notification delivery via HTTPS webhooks** and a **self-service REST API**, implemented with **Java + Spring Boot** following **Hexagonal Architecture**.

---

## Context (from the challenge)
The platform must:
- Deliver event notifications via **webhook** to a specific URL.
- Ensure **client isolation** (events belong to the correct client).
- Handle errors with an efficient **retry strategy** (discussed in design; scoped in code).
- Store final delivery information.
- Be **observable** in a **near real-time** approach.
- Expose a self-service REST API:
  - `GET /notification_events`
  - `GET /notification_events/{notification_event_id}`
  - `POST /notification_events/{notification_event_id}/replay`

Dataset: `notification_events.json` (read-only snapshot).

---

## Local setup philosophy
**Only Docker is required on the host.**
- Java 21 and Maven run **inside a dev container**.
- PostgreSQL runs as a service in `docker-compose`.
- The application is executed **inside the container**.

> All commands below assume the repository root folder.

---

## Repository root
```bash
cd notifications-webhook-service-divv
```

---

## Quick start

### 1) Start containers
```bash
cd notifications-webhook-service-divv
docker compose up -d
docker ps
```

### 2) Enter the dev container
```bash
cd notifications-webhook-service-divv
docker compose exec dev bash
```

### 3) Build (inside the container)
```bash
mvn -q -DskipTests package
```

### 4) Run the application (inside the container)
```bash
mvn -q -DskipTests spring-boot:run
```

### 5) Health check (from host)
```bash
cd notifications-webhook-service-divv
curl -i http://localhost:8080/actuator/health
```

Expected:
```json
{"status":"UP"}
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
- `WEBHOOK_BASE_URL`

The actual webhook URL will be supplied during the presentation.

---

## Architecture (Hexagonal)

High-level structure:
- `domain/` — Core business rules and models (no framework dependencies)
- `domain/ports/in` — Use case interfaces (inbound ports)
- `domain/ports/out` — External dependencies as interfaces (outbound ports)
- `adapters/in/rest` — REST controllers
- `adapters/out/*` — JSON snapshot reader, persistence, webhook sender
- `config/` — Spring wiring and configuration

(Diagrams and deeper explanation are provided in the design document.)

---

## API (TBD)
Endpoints to be implemented:
- `GET /notification_events`
- `GET /notification_events/{notification_event_id}`
- `POST /notification_events/{notification_event_id}/replay`

Rules:
- Replay allowed **only** when delivery status is `failed`.
- The JSON dataset is **read-only**.

---

## Observability (TBD)
- Spring Boot Actuator enabled
- `/actuator/health` available
- Prometheus/Grafana discussed in design and added later

---

## Security (TBD)
At least **three OWASP Top 10** risks identified and mitigations proposed in the design document.

---

## Command reference

### Docker
```bash
cd notifications-webhook-service-divv
docker compose up -d
docker compose down
docker compose down -v
docker compose ps
docker ps
```

### Dev container
```bash
cd notifications-webhook-service-divv
docker compose exec dev bash
```

### Build & run (inside container)
```bash
mvn -q -DskipTests package
mvn -q -DskipTests spring-boot:run
```

### PostgreSQL (inside container, optional)
```bash
apt-get update -qq && apt-get install -y postgresql-client
psql -h postgres -U notifications -d notifications
```

---

## Branching
- Feature branches merged into `main` via PRs.
