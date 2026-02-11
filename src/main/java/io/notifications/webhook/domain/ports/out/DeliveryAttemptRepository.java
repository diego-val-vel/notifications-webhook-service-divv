package io.notifications.webhook.domain.ports.out;

import io.notifications.webhook.domain.model.ClientId;
import io.notifications.webhook.domain.model.DeliveryAttempt;
import io.notifications.webhook.domain.model.NotificationEventId;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/*
 * DeliveryAttemptRepository is an outbound port for persisting webhook delivery attempt metadata.
 *
 * The database is not a source of truth for notification events. It stores only attempt metadata
 * required for auditing, observability, and the challenge requirement of storing final delivery info.
 *
 * This port also supports minimal idempotency for replay:
 * findReplayAttemptedAt returns the attempted_at of an existing attempt for a given (event_id, client_id, correlation_id).
 */
public interface DeliveryAttemptRepository {

    void save(DeliveryAttempt attempt);

    Optional<Instant> findReplayAttemptedAt(ClientId clientId, NotificationEventId eventId, String correlationId);

    static void requireValid(DeliveryAttempt attempt) {
        Objects.requireNonNull(attempt, "attempt must not be null");
    }

    static void requireCorrelationId(String correlationId) {
        if (correlationId == null || correlationId.isBlank()) {
            throw new IllegalArgumentException("correlationId must not be blank");
        }
    }
}