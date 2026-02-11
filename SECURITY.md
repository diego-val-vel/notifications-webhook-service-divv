# SECURITY.md

## Security Analysis -- OWASP Top 10 Alignment

This document identifies relevant OWASP Top 10 vulnerabilities that
could impact the Notifications Webhook Self-Service API and describes
mitigation strategies appropriate for a production-grade system.

The current implementation is a take-home exercise and intentionally
simplifies certain aspects (e.g., authentication), but the following
risks and mitigations describe how the system should evolve for
real-world deployment.

------------------------------------------------------------------------

## 1. A01 -- Broken Access Control

### How It Applies

The API currently accepts `client_id` as a query parameter:

-   `GET /notification_events?client_id=CLIENT001`
-   `POST /notification_events/{id}/replay?client_id=CLIENT001`

Without authentication, a malicious user could attempt to access another
client's data simply by modifying the `client_id` parameter.

### Risk

-   Unauthorized access to financial event data.
-   Replay of webhook deliveries for another tenant.
-   Multi-tenant data exposure.

### Mitigation Strategy

In a production environment:

-   Implement OAuth2 / OIDC authentication.
-   Derive `client_id` from a validated JWT token instead of accepting
    it as user input.
-   Enforce tenant isolation at the service layer.
-   Apply rate limiting and monitoring for suspicious access patterns.

**Design Principle:** Client identity must be derived from authenticated
context, never from user-provided parameters.

------------------------------------------------------------------------

## 2. A10 -- Server-Side Request Forgery (SSRF)

### How It Applies

The system performs outbound webhook delivery to a configurable HTTPS
endpoint.

If webhook targets were client-configurable in a real system, this could
enable SSRF attacks, allowing attackers to:

-   Access internal services.
-   Probe private network IP ranges.
-   Reach cloud metadata endpoints.

### Risk

-   Internal network reconnaissance.
-   Data exfiltration from internal services.
-   Infrastructure compromise.

### Mitigation Strategy

-   Enforce HTTPS-only webhook URLs.
-   Validate scheme and host format strictly.
-   Block private IP ranges (RFC1918, loopback, link-local).
-   Disable automatic redirect following.
-   Apply strict timeouts and connection limits.
-   Use outbound network firewall rules to restrict egress traffic.

**Design Principle:** All outbound network calls must be treated as
untrusted and validated defensively.

------------------------------------------------------------------------

## 3. A03 -- Injection

### How It Applies

The API accepts user input via:

-   Query parameters (`delivery_status`, `date_from`, `date_to`,
    `client_id`)
-   Path variables (`notification_event_id`)
-   Headers (`Idempotency-Key`)

Although the system uses JPA with parameterized queries (mitigating
classic SQL injection), improper validation could still introduce risk.

### Risk

-   SQL injection (if raw queries were introduced).
-   Log injection via structured logging.
-   Header-based abuse through unbounded correlation IDs.

### Mitigation Strategy

-   Use ORM parameterized queries exclusively (already implemented).
-   Enforce strict enum validation (`COMPLETED | FAILED`).
-   Normalize and truncate `Idempotency-Key` values (length limits).
-   Validate and sanitize user input.
-   Avoid dynamic query construction.

**Design Principle:** All external input is untrusted and must be
validated before use.

------------------------------------------------------------------------

## 4. A05 -- Security Misconfiguration

### How It Applies

The system exposes:

-   `/actuator/prometheus`
-   Prometheus and Grafana services (default credentials)
-   HTTP endpoints without TLS

In a real deployment, exposing observability endpoints publicly would be
unsafe.

### Risk

-   Leakage of internal metrics.
-   Exposure of system topology.
-   Unauthorized access to monitoring dashboards.
-   Credential abuse (default Grafana credentials).

### Mitigation Strategy

-   Disable non-essential Actuator endpoints in production.
-   Restrict Actuator access via firewall or authentication.
-   Enforce HTTPS termination at the load balancer or gateway.
-   Change default Grafana credentials.
-   Restrict Prometheus and Grafana to internal networks only.
-   Apply container hardening and minimal image principles.

**Design Principle:** Observability components must not be publicly
exposed without strict access controls.

------------------------------------------------------------------------

## Additional Considerations

-   Implement API rate limiting to prevent abuse.
-   Add request size limits to prevent resource exhaustion.
-   Monitor replay patterns for anomaly detection.
-   Introduce audit logging for security-relevant operations.

------------------------------------------------------------------------

## Summary

While this implementation focuses on architecture, replay semantics,
idempotency, and observability, a production-grade deployment must
address:

-   Strong authentication and authorization
-   Defensive outbound network controls
-   Strict input validation
-   Secure configuration of infrastructure components

Security is not a feature but a cross-cutting concern integrated across
all layers of the system.
