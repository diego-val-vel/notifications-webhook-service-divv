# AI_USAGE.md

## AI Usage Documentation

This document describes how AI tools were used during the development of
this project, in alignment with the exercise requirement to document
AI-assisted workflows.

The goal of using AI was to enhance productivity and accelerate
iteration, while maintaining full architectural control and human
validation over all decisions.

------------------------------------------------------------------------

## 1. Scope of AI Assistance

AI was used in the following areas:

### Architecture & Structure

-   Refinement of hexagonal architecture boundaries (ports and
    adapters).
-   Validation of separation between domain and infrastructure layers.
-   Suggestions for structuring configuration classes and decorators.

### Webhook Delivery & Idempotency

-   Drafting of the HTTP webhook sender implementation.
-   Structuring idempotency logic using correlation IDs.
-   Reviewing replay semantics and edge cases.

### Observability

-   Generating Micrometer metric naming conventions.
-   Structuring Prometheus + Grafana integration.
-   Validating metric tagging strategy.

### Security Analysis

-   Drafting OWASP Top 10 vulnerability analysis.
-   Structuring mitigation strategies specific to this API design.

### Documentation

-   Improving README structure.
-   Structuring SECURITY.md and this AI_USAGE.md file.

------------------------------------------------------------------------

## 2. Validation Process

All AI-generated suggestions were:

-   Manually reviewed before integration.
-   Adjusted to comply with frozen architectural decisions.
-   Tested via curl commands.
-   Validated against Postgres state (delivery_attempts table).
-   Verified using Prometheus metrics.
-   Confirmed through Grafana dashboards.

No AI-generated code was merged without manual inspection and execution
validation.

------------------------------------------------------------------------

## 3. Human-Driven Architectural Decisions

The following decisions were intentionally made by the author and not
delegated to AI:

-   Freezing JSON as the immutable source of truth.
-   Avoiding asynchronous queues and background retry workers.
-   Enforcing replay-only retry semantics.
-   Keeping the domain layer framework-free.
-   Persisting only delivery attempt metadata in Postgres.
-   Introducing structured logs and minimal observability scope.
-   Maintaining deterministic idempotency behavior.

These decisions reflect deliberate trade-offs appropriate for a
take-home system design exercise.

------------------------------------------------------------------------

## 4. What AI Did NOT Do

AI was not used to:

-   Make security or architectural decisions autonomously.
-   Replace understanding of business logic.
-   Generate blind boilerplate without validation.
-   Define the replay semantics or domain rules.

AI was used as a productivity assistant, not as a decision-maker.

------------------------------------------------------------------------

## 5. Summary

AI tools were used to accelerate implementation and improve clarity,
while all critical reasoning, validation, and architectural decisions
remained under direct human control.

The final system behavior, trade-offs, and constraints were explicitly
reviewed and verified by the author.
