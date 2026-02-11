package io.notifications.webhook.domain.ports.in;

import io.notifications.webhook.domain.model.ClientId;
import io.notifications.webhook.domain.model.NotificationEventId;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/*
 * ReplayNotificationEventUseCase exposes the domain operation to request a replay delivery for an event.
 * ClientId is required to enforce tenant isolation.
 *
 * This use case supports minimal idempotency via an optional idempotency key:
 * - When present, the key is used as a correlation id for a replay attempt.
 * - Implementations may return an existing attempt result without re-sending the webhook.
 *
 * The Result indicates whether the replay was accepted. requestedAt is optional to allow implementations
 * to either provide a domain clock timestamp or delegate it to the caller when appropriate.
 */
public interface ReplayNotificationEventUseCase {

    Result replay(Command command);

    final class Command {

        private final ClientId clientId;
        private final NotificationEventId notificationEventId;
        private final Optional<String> idempotencyKey;

        public Command(ClientId clientId, NotificationEventId notificationEventId, Optional<String> idempotencyKey) {
            this.clientId = Objects.requireNonNull(clientId, "clientId must not be null");
            this.notificationEventId = Objects.requireNonNull(notificationEventId, "notificationEventId must not be null");
            this.idempotencyKey = Objects.requireNonNull(idempotencyKey, "idempotencyKey must not be null");
        }

        public ClientId clientId() {
            return clientId;
        }

        public NotificationEventId notificationEventId() {
            return notificationEventId;
        }

        public Optional<String> idempotencyKey() {
            return idempotencyKey;
        }
    }

    final class Result {

        private final boolean accepted;
        private final Optional<Instant> requestedAt;

        private Result(boolean accepted, Optional<Instant> requestedAt) {
            this.accepted = accepted;
            this.requestedAt = Objects.requireNonNull(requestedAt, "requestedAt must not be null");
        }

        public static Result accepted(Instant requestedAt) {
            return new Result(true, Optional.ofNullable(requestedAt));
        }

        public static Result rejected(Instant requestedAt) {
            return new Result(false, Optional.ofNullable(requestedAt));
        }

        public boolean accepted() {
            return accepted;
        }

        public Optional<Instant> requestedAt() {
            return requestedAt;
        }
    }
}