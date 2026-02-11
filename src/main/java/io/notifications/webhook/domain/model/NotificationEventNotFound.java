package io.notifications.webhook.domain.model;

/*
 * NotificationEventNotFound is a domain-level exception used when a notification event cannot be located.
 * It allows application/use-case layers to convert this situation into an appropriate API response
 * without leaking persistence-specific exceptions into the domain.
 */
public final class NotificationEventNotFound extends RuntimeException {

    public NotificationEventNotFound(NotificationEventId id) {
        super("NotificationEvent not found: " + (id == null ? "null" : id.value()));
    }
}