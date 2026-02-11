# DESIGN.md

## System Design -- Notifications Webhook Delivery + Self-Service API

This document describes the system design for a notifications feature
that delivers platform-generated events to client webhooks and provides
a self-service API for querying and replaying notification events. The
implementation follows Hexagonal Architecture (Ports & Adapters) and is
intentionally scoped as a take-home exercise while remaining
production-oriented in its core decisions.

------------------------------------------------------------------------

## 1. Problem Overview

### Functional Requirements

-   Deliver event notifications to client-owned HTTPS webhook endpoints.
-   Ensure strict tenant isolation: clients can only access their own
    events.
-   Provide a self-service REST API:
    -   `GET /notification_events` (filter by delivery status and date
        range)
    -   `GET /notification_events/{notification_event_id}`
    -   `POST /notification_events/{notification_event_id}/replay` (only
        when delivery failed)
-   Store final delivery attempt information (status, duration, HTTP
    status, error details).

### Non-Functional Requirements

-   Scalability: handle multiple clients and increasing event volume.
-   Resiliency: tolerate transient network failures and support replay
    semantics.
-   Observability: near real-time visibility of delivery behavior.
-   Security: mitigate common OWASP Top 10 risks for public APIs.

------------------------------------------------------------------------

## 2. High-Level Architecture

### Architectural Style

Hexagonal Architecture (Ports & Adapters): - Domain layer is
framework-free and contains business rules. - Adapters implement inbound
(REST) and outbound (JSON source, Postgres persistence, webhook sender).

### Component Diagram (Conceptual)

                           +-------------------------+
                           |        Clients          |
                           |  (API consumers)        |
                           +------------+------------+
                                        |
                                        | HTTPS (REST)
                                        v
    +------------------------+   +---------------+   +--------------------------+
    |   Inbound Adapter       |   |  Application  |   |        Domain            |
    | REST Controllers        +-->|  Use Cases    +-->| Models + Rules + Ports    |
    +------------------------+   +-------+-------+   +------------+--------------+
                                        |                        |
                                        | Outbound Ports         |
                                        v                        v
                +-------------------------------+      +-------------------------------+
                | JSON Snapshot Repository       |      | Webhook Sender (HTTPS)        |
                | (Source of truth for events)  |      | + DeliveryAttempt persistence |
                +-------------------------------+      +-------------------------------+
                                        |
                                        v
                          +-------------------------------+
                          | Postgres (delivery_attempts)  |
                          | Metadata only                 |
                          +-------------------------------+

    Observability:
    - Micrometer metrics exposed at /actuator/prometheus
    - Prometheus scrapes metrics
    - Grafana visualizes metrics

------------------------------------------------------------------------

## 3. Core Design Decisions

### 3.1 JSON as Immutable Source of Truth

-   The dataset `notification_events.json` is treated as an immutable
    snapshot.
-   All GET endpoints read exclusively from JSON and do not depend on
    Postgres.
-   Postgres is used only for delivery attempt metadata.

Rationale: - Ensures deterministic behavior for the take-home scope. -
Avoids introducing a mutable event store and related consistency
complexity.

### 3.2 Replay as Explicit Client-Driven Retry

-   Only `FAILED` events can be replayed.
-   `COMPLETED` events return conflict (no resend).
-   This replaces background retry workers and avoids async flows.

Rationale: - Preserves deterministic semantics without queues. - Keeps
the system simple while still supporting operational recovery through
replay.

### 3.3 Postgres Stores Only Delivery Attempt Metadata

Stored fields include: - attempt type (REPLAY) - attempted_at
timestamp - duration_ms - result (SUCCESS/FAILURE) - http_status /
error_message - correlation_id (idempotency) - target_url - event_id +
client_id for lookup

Rationale: - Separates event truth (JSON) from delivery metadata. -
Allows auditing, debugging, and observability without mutating event
data.

### 3.4 Minimal Idempotency via Correlation ID

-   Optional `Idempotency-Key` header is used as `correlation_id`.
-   If an attempt already exists for
    `(event_id, client_id, correlation_id)`:
    -   Do not resend the webhook.
    -   Reuse the stored attempt timestamp in the response.

Rationale: - Prevents duplicate deliveries for retried client
requests. - Keeps idempotency deterministic and easy to reason about.

------------------------------------------------------------------------

## 4. Scalability Strategy (Production Perspective)

This implementation is intentionally scoped, but scales naturally due to
its stateless design.

### 4.1 Stateless API Layer

-   The REST API is stateless and can be horizontally scaled behind a
    load balancer.
-   JSON snapshot access is local file-based for the take-home, but
    could be externalized:
    -   Object storage (S3/GCS) + caching
    -   A dedicated event store service in a real implementation

### 4.2 Postgres as Shared Metadata Store

-   Delivery attempt metadata is centralized in Postgres.
-   Scales via standard approaches:
    -   Connection pooling
    -   Indexing on `(event_id, client_id, correlation_id)`
    -   Read replicas (if needed for analytics)

### 4.3 Webhook Delivery Throughput

For production: - Webhook deliveries typically benefit from async
workers and queues. - This take-home intentionally avoids that, but the
architecture can evolve: - Add an outbox/queue adapter - Add a worker
consuming delivery jobs - Preserve the same domain ports and replay
rules

------------------------------------------------------------------------

## 5. Resiliency Strategy

### 5.1 Outbound Network Controls

-   HTTPS-only webhook targets.
-   Strict timeouts and error handling for 4xx/5xx and exceptions.
-   Delivery metadata stored for each attempt.

### 5.2 Deterministic Replay Semantics

-   Replay is allowed only when delivery has failed.
-   Idempotency prevents duplicate sends on repeated replay calls.

### 5.3 Operational Recovery

-   Failures are visible via:
    -   Postgres attempt metadata
    -   Metrics exposed to Prometheus/Grafana
    -   Structured logs per attempt

------------------------------------------------------------------------

## 6. Observability (Near Real-Time)

### Metrics (Micrometer)

Exposed at `/actuator/prometheus`: -
`webhook_delivery_attempts_total{result=success|failure}` -
`webhook_delivery_latency_seconds` -
`notification_replay_total{result=accepted|rejected|not_found|failure}`

### Logs

Structured log per delivery attempt includes: - event_id, client_id,
event_type - result, http_status, duration_ms - target_url,
correlation_id

### Monitoring

-   Prometheus scrapes metrics at 15s intervals.
-   Grafana provides visualization of:
    -   delivery attempt rates
    -   replay rates
    -   latency trends

------------------------------------------------------------------------

## 7. Security Considerations

This API is assumed public-facing. Key risks and mitigations are
documented in `SECURITY.md`, including: - Broken Access Control - SSRF
risks from outbound webhook delivery - Injection risks from input
handling - Observability endpoint exposure (security misconfiguration)

------------------------------------------------------------------------

## 8. Trade-offs and Future Enhancements

### Trade-offs Made for the Take-Home Scope

-   No background retry workers and no queues.
-   JSON snapshot is immutable and acts as the event truth.
-   Postgres stores delivery attempts only.

### Potential Production Enhancements

-   Authentication and authorization (OAuth2/JWT).
-   Subscription registry backed by a durable store.
-   Circuit breakers and exponential backoff for retries.
-   Async delivery pipeline with queue + workers.
-   Alerting rules in Prometheus (error rate, latency p95/p99).
-   Dashboard JSON provisioning for Grafana.

------------------------------------------------------------------------

## Summary

This solution delivers a deterministic, testable, and observable
notification replay capability within the constraints of the exercise.
It demonstrates strong separation of concerns via hexagonal
architecture, clear replay and idempotency semantics, and near real-time
observability through metrics and logs. The design can evolve toward a
production-grade asynchronous delivery pipeline without breaking domain
boundaries.
