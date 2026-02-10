package io.notifications.webhook.domain.model;

import java.time.Instant;
import java.util.Objects;

/*
 * NotificationEvent is the aggregate root representing an event that can be delivered to a client via webhook.
 * It enforces client isolation, validates core invariants, and encapsulates business rules such as replay eligibility.
 */
public final class NotificationEvent {

    private final NotificationEventId id;
    private final ClientId clientId;
    private final EventType eventType;
    private final String content;
    private final Instant deliveryDate;
    private final DeliveryStatus deliveryStatus;

    private NotificationEvent(
            NotificationEventId id,
            ClientId clientId,
            EventType eventType,
            String content,
            Instant deliveryDate,
            DeliveryStatus deliveryStatus
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.clientId = Objects.requireNonNull(clientId, "clientId must not be null");
        this.eventType = Objects.requireNonNull(eventType, "eventType must not be null");
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content must not be null or blank");
        }
        this.content = content;
        this.deliveryDate = Objects.requireNonNull(deliveryDate, "deliveryDate must not be null");
        this.deliveryStatus = Objects.requireNonNull(deliveryStatus, "deliveryStatus must not be null");
    }

    public static NotificationEvent of(
            NotificationEventId id,
            ClientId clientId,
            EventType eventType,
            String content,
            Instant deliveryDate,
            DeliveryStatus deliveryStatus
    ) {
        return new NotificationEvent(id, clientId, eventType, content, deliveryDate, deliveryStatus);
    }

    public NotificationEventId id() {
        return id;
    }

    public ClientId clientId() {
        return clientId;
    }

    public EventType eventType() {
        return eventType;
    }

    public String content() {
        return content;
    }

    public Instant deliveryDate() {
        return deliveryDate;
    }

    public DeliveryStatus deliveryStatus() {
        return deliveryStatus;
    }

    public boolean canBeReplayed() {
        return deliveryStatus.isReplayAllowed();
    }

    public void assertBelongsTo(ClientId expectedClientId) {
        Objects.requireNonNull(expectedClientId, "expectedClientId must not be null");
        if (!this.clientId.equals(expectedClientId)) {
            throw new IllegalArgumentException("NotificationEvent does not belong to the given client");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotificationEvent that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}