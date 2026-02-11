package io.notifications.webhook.adapters.in.rest.mapper;

import io.notifications.webhook.adapters.in.rest.dto.NotificationEventResponse;
import io.notifications.webhook.domain.model.NotificationEvent;

import java.util.List;
import java.util.Objects;

/*
 * NotificationEventRestMapper is responsible for transforming domain NotificationEvent aggregates
 * into REST response DTOs.
 *
 * It keeps the REST contract isolated from domain internals and provides a stable mapping layer.
 */
public final class NotificationEventRestMapper {

    public NotificationEventRestMapper() {
    }

    public static NotificationEventResponse toResponse(NotificationEvent event) {
        Objects.requireNonNull(event, "event must not be null");

        return NotificationEventResponse.of(
                event.id().value(),
                event.eventType().externalValue(),
                event.content(),
                event.deliveryDate(),
                event.deliveryStatus().name(),
                event.clientId().value()
        );
    }

    public static List<NotificationEventResponse> toResponseList(List<NotificationEvent> events) {
        Objects.requireNonNull(events, "events must not be null");

        return events.stream()
                .map(NotificationEventRestMapper::toResponse)
                .toList();
    }
}