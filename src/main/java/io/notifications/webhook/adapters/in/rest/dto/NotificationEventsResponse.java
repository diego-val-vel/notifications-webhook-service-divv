package io.notifications.webhook.adapters.in.rest.dto;

import java.util.List;
import java.util.Objects;

/*
 * NotificationEventsResponse is a REST response DTO that represents a collection of notification
 * events returned by the self-service API.
 *
 * It is a pure transport model and must not contain domain logic. The class is immutable and
 * enforces basic invariants to guarantee a consistent API contract.
 */
public final class NotificationEventsResponse {

    private final List<NotificationEventResponse> events;

    private NotificationEventsResponse(List<NotificationEventResponse> events) {
        if (events == null) {
            throw new IllegalArgumentException("events must not be null");
        }
        this.events = List.copyOf(events);
    }

    public static NotificationEventsResponse of(List<NotificationEventResponse> events) {
        return new NotificationEventsResponse(events);
    }

    public List<NotificationEventResponse> events() {
        return events;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotificationEventsResponse that)) return false;
        return Objects.equals(events, that.events);
    }

    @Override
    public int hashCode() {
        return Objects.hash(events);
    }

    @Override
    public String toString() {
        return "NotificationEventsResponse{" +
                "events=" + events +
                '}';
    }
}