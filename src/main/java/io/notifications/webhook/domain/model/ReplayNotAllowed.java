package io.notifications.webhook.domain.model;

/*
 * ReplayNotAllowed is a domain-level exception thrown when a replay attempt is requested
 * for a notification event whose delivery status does not allow replay.
 * This enforces business rules around delivery lifecycle and prevents invalid operations.
 */
public final class ReplayNotAllowed extends RuntimeException {

    public ReplayNotAllowed(NotificationEventId id, DeliveryStatus status) {
        super("Replay not allowed for NotificationEvent " +
                (id == null ? "null" : id.value()) +
                " with status " +
                (status == null ? "null" : status.name()));
    }
}