package io.notifications.webhook.adapters.out.json.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/*
 * NotificationEventsSnapshotDto represents the top-level JSON snapshot structure.
 * It is designed for Jackson deserialization and keeps the JSON contract isolated from the domain model.
 */
public final class NotificationEventsSnapshotDto {

    @JsonProperty("events")
    private final List<NotificationEventDto> events;

    @JsonCreator
    public NotificationEventsSnapshotDto(@JsonProperty("events") List<NotificationEventDto> events) {
        this.events = Objects.requireNonNull(events, "events must not be null");
    }

    public List<NotificationEventDto> events() {
        return events;
    }
}