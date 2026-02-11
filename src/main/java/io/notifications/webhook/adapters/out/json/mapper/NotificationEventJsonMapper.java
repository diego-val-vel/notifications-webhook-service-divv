package io.notifications.webhook.adapters.out.json.mapper;

import io.notifications.webhook.adapters.out.json.dto.NotificationEventDto;
import io.notifications.webhook.domain.model.ClientId;
import io.notifications.webhook.domain.model.DeliveryStatus;
import io.notifications.webhook.domain.model.EventType;
import io.notifications.webhook.domain.model.NotificationEvent;
import io.notifications.webhook.domain.model.NotificationEventId;

import java.time.Instant;
import java.util.Locale;

/*
 * NotificationEventJsonMapper is responsible for converting JSON DTO structures
 * into fully validated domain aggregates.
 *
 * This mapper enforces domain invariants by delegating object creation to the
 * appropriate value object factories and aggregate factory method.
 *
 * No persistence, framework, or infrastructure concerns are handled here.
 */
public final class NotificationEventJsonMapper {

    public NotificationEvent toDomain(NotificationEventDto dto) {

        NotificationEventId eventId = NotificationEventId.of(dto.eventId());
        ClientId clientId = ClientId.of(dto.clientId());
        EventType eventType = EventType.fromExternalValue(dto.eventType());
        Instant deliveryDate = Instant.parse(dto.deliveryDate());
        DeliveryStatus deliveryStatus = mapDeliveryStatus(dto.deliveryStatus());

        return NotificationEvent.of(
                eventId,
                clientId,
                eventType,
                dto.content().toString(),
                deliveryDate,
                deliveryStatus
        );
    }

    private DeliveryStatus mapDeliveryStatus(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("delivery_status must not be null or blank");
        }

        String normalized = value.toUpperCase(Locale.ROOT);

        try {
            return DeliveryStatus.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported delivery_status value: " + value);
        }
    }
}