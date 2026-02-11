package io.notifications.webhook.adapters.out.webhook;

import io.notifications.webhook.domain.model.ClientId;
import io.notifications.webhook.domain.model.NotificationEvent;

import java.time.Instant;
import java.util.Objects;

/*
 * WebhookPayload is the minimal JSON body sent to the external webhook target.
 *
 * It includes only the fields required by the challenge for replay delivery:
 * event_id, client_id, event_type, content, delivery_date.
 */
public record WebhookPayload(
        String event_id,
        String client_id,
        String event_type,
        String content,
        Instant delivery_date
) {

    public static WebhookPayload from(ClientId clientId, NotificationEvent event) {
        Objects.requireNonNull(clientId, "clientId must not be null");
        Objects.requireNonNull(event, "event must not be null");

        return new WebhookPayload(
                event.id().value(),
                clientId.value(),
                event.eventType().externalValue(),
                event.content(),
                event.deliveryDate()
        );
    }
}