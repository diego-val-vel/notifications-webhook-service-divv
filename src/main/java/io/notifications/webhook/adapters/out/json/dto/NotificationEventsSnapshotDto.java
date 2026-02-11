package io.notifications.webhook.adapters.out.json.dto;

import java.util.List;

/*
 * NotificationEventsSnapshotDto represents the root structure of the JSON snapshot file.
 * It mirrors the external dataset contract and contains the list of notification events.
 * This class is purely a transport structure used for deserialization purposes.
 */
public final class NotificationEventsSnapshotDto {

    private final List<NotificationEventDto> events;

    public NotificationEventsSnapshotDto(List<NotificationEventDto> events) {
        this.events = events;
    }

    public List<NotificationEventDto> events() {
        return events;
    }
}