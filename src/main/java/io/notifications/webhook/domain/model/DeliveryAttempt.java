package io.notifications.webhook.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/*
 * DeliveryAttempt is a domain model representing metadata for a webhook delivery attempt.
 *
 * The notification events dataset is immutable and remains the source of truth. This model exists only
 * to persist attempt metadata in Postgres, as required by the challenge, without making the database
 * the authoritative source for events.
 *
 * correlationId is used to store an optional idempotency key (when provided) to support minimal replay idempotency.
 */
public final class DeliveryAttempt {

    private static final int MAX_ERROR_MESSAGE_LENGTH = 500;

    private final UUID id;
    private final NotificationEventId eventId;
    private final ClientId clientId;
    private final String targetUrl;
    private final AttemptType attemptType;
    private final DeliveryAttemptResult result;
    private final Optional<Integer> httpStatus;
    private final Optional<String> errorMessage;
    private final Instant attemptedAt;
    private final long durationMs;
    private final Optional<String> correlationId;

    private DeliveryAttempt(
            UUID id,
            NotificationEventId eventId,
            ClientId clientId,
            String targetUrl,
            AttemptType attemptType,
            DeliveryAttemptResult result,
            Optional<Integer> httpStatus,
            Optional<String> errorMessage,
            Instant attemptedAt,
            long durationMs,
            Optional<String> correlationId
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.eventId = Objects.requireNonNull(eventId, "eventId must not be null");
        this.clientId = Objects.requireNonNull(clientId, "clientId must not be null");

        if (targetUrl == null || targetUrl.isBlank()) {
            throw new IllegalArgumentException("targetUrl must not be blank");
        }
        this.targetUrl = targetUrl;

        this.attemptType = Objects.requireNonNull(attemptType, "attemptType must not be null");
        this.result = Objects.requireNonNull(result, "result must not be null");
        this.httpStatus = Objects.requireNonNull(httpStatus, "httpStatus must not be null");
        this.errorMessage = Objects.requireNonNull(errorMessage, "errorMessage must not be null");
        this.attemptedAt = Objects.requireNonNull(attemptedAt, "attemptedAt must not be null");

        if (durationMs < 0) {
            throw new IllegalArgumentException("durationMs must be >= 0");
        }
        this.durationMs = durationMs;

        this.correlationId = Objects.requireNonNull(correlationId, "correlationId must not be null");
    }

    public static DeliveryAttempt replayAttempt(
            NotificationEventId eventId,
            ClientId clientId,
            String targetUrl,
            DeliveryAttemptResult result,
            Optional<Integer> httpStatus,
            Optional<String> errorMessage,
            Instant attemptedAt,
            long durationMs,
            Optional<String> correlationId
    ) {
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(clientId, "clientId must not be null");
        Objects.requireNonNull(result, "result must not be null");
        Objects.requireNonNull(httpStatus, "httpStatus must not be null");
        Objects.requireNonNull(errorMessage, "errorMessage must not be null");
        Objects.requireNonNull(attemptedAt, "attemptedAt must not be null");
        Objects.requireNonNull(correlationId, "correlationId must not be null");

        Optional<String> sanitizedError = errorMessage
                .map(DeliveryAttempt::sanitizeErrorMessage)
                .filter(s -> !s.isBlank());

        Optional<String> normalizedCorrelationId = correlationId
                .map(String::trim)
                .filter(s -> !s.isBlank());

        return new DeliveryAttempt(
                UUID.randomUUID(),
                eventId,
                clientId,
                targetUrl,
                AttemptType.REPLAY,
                result,
                httpStatus,
                sanitizedError,
                attemptedAt,
                durationMs,
                normalizedCorrelationId
        );
    }

    private static String sanitizeErrorMessage(String message) {
        String trimmed = message == null ? "" : message.trim();
        if (trimmed.isBlank()) {
            return "";
        }
        if (trimmed.length() <= MAX_ERROR_MESSAGE_LENGTH) {
            return trimmed;
        }
        return trimmed.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }

    public UUID id() {
        return id;
    }

    public NotificationEventId eventId() {
        return eventId;
    }

    public ClientId clientId() {
        return clientId;
    }

    public String targetUrl() {
        return targetUrl;
    }

    public AttemptType attemptType() {
        return attemptType;
    }

    public DeliveryAttemptResult result() {
        return result;
    }

    public Optional<Integer> httpStatus() {
        return httpStatus;
    }

    public Optional<String> errorMessage() {
        return errorMessage;
    }

    public Instant attemptedAt() {
        return attemptedAt;
    }

    public long durationMs() {
        return durationMs;
    }

    public Optional<String> correlationId() {
        return correlationId;
    }
}