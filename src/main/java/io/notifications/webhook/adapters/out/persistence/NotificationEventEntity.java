package io.notifications.webhook.adapters.out.persistence;

import io.notifications.webhook.domain.model.DeliveryStatus;
import io.notifications.webhook.domain.model.EventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

/*
 * NotificationEventEntity is the JPA representation of the NotificationEvent aggregate.
 * It mirrors the domain structure and persists domain enums using EnumType.STRING
 * to preserve readability and avoid ordinal coupling.
 */
@Entity
@Table(name = "notification_events")
public class NotificationEventEntity {

    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private String eventId;

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "delivery_date", nullable = false)
    private Instant deliveryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false)
    private DeliveryStatus deliveryStatus;

    protected NotificationEventEntity() {
    }

    public NotificationEventEntity(
            String eventId,
            String clientId,
            EventType eventType,
            String content,
            Instant deliveryDate,
            DeliveryStatus deliveryStatus
    ) {
        this.eventId = Objects.requireNonNull(eventId);
        this.clientId = Objects.requireNonNull(clientId);
        this.eventType = Objects.requireNonNull(eventType);
        this.content = Objects.requireNonNull(content);
        this.deliveryDate = Objects.requireNonNull(deliveryDate);
        this.deliveryStatus = Objects.requireNonNull(deliveryStatus);
    }

    public String getEventId() {
        return eventId;
    }

    public String getClientId() {
        return clientId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public String getContent() {
        return content;
    }

    public Instant getDeliveryDate() {
        return deliveryDate;
    }

    public DeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(DeliveryStatus deliveryStatus) {
        this.deliveryStatus = Objects.requireNonNull(deliveryStatus);
    }

    public void setDeliveryDate(Instant deliveryDate) {
        this.deliveryDate = Objects.requireNonNull(deliveryDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotificationEventEntity that)) return false;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }
}