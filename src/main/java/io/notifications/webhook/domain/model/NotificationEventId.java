package io.notifications.webhook.domain.model;

import java.util.Objects;

/*
 * NotificationEventId is a domain value object that represents the unique identifier of a notification event.
 * It avoids leaking primitive strings through the domain and enforces basic invariants.
 */
public final class NotificationEventId {

    private final String value;

    private NotificationEventId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("NotificationEventId value must not be null or blank");
        }
        this.value = value;
    }

    public static NotificationEventId of(String value) {
        return new NotificationEventId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotificationEventId that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}