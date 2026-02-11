package io.notifications.webhook.adapters.out.webhook;

import io.notifications.webhook.domain.model.ClientId;
import io.notifications.webhook.domain.model.NotificationEvent;
import io.notifications.webhook.domain.ports.out.WebhookSender;

import java.time.Instant;
import java.util.Objects;

/*
 * NoOpWebhookSender is a temporary outbound adapter implementation of the WebhookSender port.
 *
 * It satisfies the outbound dependency without performing any real HTTP call.
 * The delivery is simulated as successful.
 */
public final class NoOpWebhookSender implements WebhookSender {

    @Override
    public DeliveryResult send(ClientId clientId, NotificationEvent notificationEvent) {
        Objects.requireNonNull(clientId, "clientId must not be null");
        Objects.requireNonNull(notificationEvent, "notificationEvent must not be null");

        return DeliveryResult.success(200, Instant.now());
    }
}