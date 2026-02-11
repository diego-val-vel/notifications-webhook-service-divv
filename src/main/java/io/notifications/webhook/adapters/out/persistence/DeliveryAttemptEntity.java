package io.notifications.webhook.adapters.out.persistence;

import io.notifications.webhook.domain.model.AttemptType;
import io.notifications.webhook.domain.model.DeliveryAttemptResult;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/*
 * DeliveryAttemptEntity is the JPA representation for persisted webhook delivery attempt metadata.
 *
 * The notification events dataset remains immutable and authoritative in JSON. This table stores
 * only metadata for delivery attempts (e.g., replay) as required by the challenge.
 */
@Entity
@Table(name = "delivery_attempts")
public class DeliveryAttemptEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "target_url", nullable = false)
    private String targetUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "attempt_type", nullable = false)
    private AttemptType attemptType;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false)
    private DeliveryAttemptResult result;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "attempted_at", nullable = false)
    private Instant attemptedAt;

    @Column(name = "duration_ms", nullable = false)
    private long durationMs;

    @Column(name = "correlation_id")
    private String correlationId;

    protected DeliveryAttemptEntity() {
    }

    public DeliveryAttemptEntity(
            UUID id,
            String eventId,
            String clientId,
            String targetUrl,
            AttemptType attemptType,
            DeliveryAttemptResult result,
            Integer httpStatus,
            String errorMessage,
            Instant attemptedAt,
            long durationMs,
            String correlationId
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.eventId = Objects.requireNonNull(eventId, "eventId must not be null");
        this.clientId = Objects.requireNonNull(clientId, "clientId must not be null");
        this.targetUrl = Objects.requireNonNull(targetUrl, "targetUrl must not be null");
        this.attemptType = Objects.requireNonNull(attemptType, "attemptType must not be null");
        this.result = Objects.requireNonNull(result, "result must not be null");
        this.httpStatus = httpStatus;
        this.errorMessage = errorMessage;
        this.attemptedAt = Objects.requireNonNull(attemptedAt, "attemptedAt must not be null");
        if (durationMs < 0) {
            throw new IllegalArgumentException("durationMs must be >= 0");
        }
        this.durationMs = durationMs;
        this.correlationId = correlationId;
    }

    public UUID getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public AttemptType getAttemptType() {
        return attemptType;
    }

    public DeliveryAttemptResult getResult() {
        return result;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getAttemptedAt() {
        return attemptedAt;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeliveryAttemptEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}