package io.notifications.webhook.domain.ports.out;

import io.notifications.webhook.domain.model.ClientId;
import io.notifications.webhook.domain.model.NotificationEvent;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/*
 * WebhookSender is an outbound port responsible for delivering notification events via HTTPS webhooks.
 * It returns a DeliveryResult describing the outcome without leaking HTTP client details into the domain.
 */
public interface WebhookSender {

    DeliveryResult send(ClientId clientId, NotificationEvent notificationEvent);

    final class DeliveryResult {

        private final boolean delivered;
        private final Optional<Integer> httpStatus;
        private final Optional<String> errorMessage;
        private final Instant occurredAt;

        private DeliveryResult(
                boolean delivered,
                Optional<Integer> httpStatus,
                Optional<String> errorMessage,
                Instant occurredAt
        ) {
            this.delivered = delivered;
            this.httpStatus = Objects.requireNonNull(httpStatus, "httpStatus must not be null");
            this.errorMessage = Objects.requireNonNull(errorMessage, "errorMessage must not be null");
            this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        }

        public static DeliveryResult success(int httpStatus, Instant occurredAt) {
            return new DeliveryResult(true, Optional.of(httpStatus), Optional.empty(), occurredAt);
        }

        public static DeliveryResult failure(Integer httpStatus, String errorMessage, Instant occurredAt) {
            return new DeliveryResult(false, Optional.ofNullable(httpStatus), Optional.ofNullable(errorMessage), occurredAt);
        }

        public boolean delivered() {
            return delivered;
        }

        public Optional<Integer> httpStatus() {
            return httpStatus;
        }

        public Optional<String> errorMessage() {
            return errorMessage;
        }

        public Instant occurredAt() {
            return occurredAt;
        }
    }
}