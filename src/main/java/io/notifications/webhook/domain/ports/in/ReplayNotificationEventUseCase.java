package io.notifications.webhook.domain.ports.in;

import io.notifications.webhook.domain.model.ClientId;
import io.notifications.webhook.domain.model.NotificationEventId;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/*
 * ReplayNotificationEventUseCase exposes a domain-facing operation to request a replay delivery for an event.
 * ClientId is required to enforce tenant isolation. The result includes whether the replay was accepted and
 * (optionally) a timestamp representing when the replay was requested.
 */
public interface ReplayNotificationEventUseCase {

    Result replay(Command command);

    final class Command {

        private final ClientId clientId;
        private final NotificationEventId notificationEventId;

        public Command(ClientId clientId, NotificationEventId notificationEventId) {
            this.clientId = Objects.requireNonNull(clientId, "clientId must not be null");
            this.notificationEventId = Objects.requireNonNull(notificationEventId, "notificationEventId must not be null");
        }

        public ClientId clientId() {
            return clientId;
        }

        public NotificationEventId notificationEventId() {
            return notificationEventId;
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

        public static Result rejected() {
            return new Result(false, Optional.empty());
        }

        public boolean accepted() {
            return accepted;
        }

        public Optional<Instant> requestedAt() {
            return requestedAt;
        }
    }
}