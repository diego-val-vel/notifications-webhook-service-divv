package io.notifications.webhook.adapters.in.rest.dto;

import java.time.Instant;
import java.util.Objects;

/*
 * NotificationEventResponse is a REST response DTO that represents
 * a single notification event exposed through the self-service API.
 *
 * It is a pure transport model and must not contain domain logic.
 * The class is immutable and enforces basic invariants to guarantee
 * a consistent API contract.
 */
public final class NotificationEventResponse {

    private final String eventId;
    private final String eventType;
    private final String content;
    private final Instant deliveryDate;
    private final String deliveryStatus;
    private final String clientId;

    private NotificationEventResponse(
            String eventId,
            String eventType,
            String content,
            Instant deliveryDate,
            String deliveryStatus,
            String clientId
    ) {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("eventId must not be null or blank");
        }
        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("eventType must not be null or blank");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content must not be null or blank");
        }
        if (deliveryDate == null) {
            throw new IllegalArgumentException("deliveryDate must not be null");
        }
        if (deliveryStatus == null || deliveryStatus.isBlank()) {
            throw new IllegalArgumentException("deliveryStatus must not be null or blank");
        }
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("clientId must not be null or blank");
        }

        this.eventId = eventId;
        this.eventType = eventType;
        this.content = content;
        this.deliveryDate = deliveryDate;
        this.deliveryStatus = deliveryStatus;
        this.clientId = clientId;
    }

    public static NotificationEventResponse of(
            String eventId,
            String eventType,
            String content,
            Instant deliveryDate,
            String deliveryStatus,
            String clientId
    ) {
        return new NotificationEventResponse(
                eventId,
                eventType,
                content,
                deliveryDate,
                deliveryStatus,
                clientId
        );
    }

    public String eventId() {
        return eventId;
    }

    public String eventType() {
        return eventType;
    }

    public String content() {
        return content;
    }

    public Instant deliveryDate() {
        return deliveryDate;
    }

    public String deliveryStatus() {
        return deliveryStatus;
    }

    public String clientId() {
        return clientId;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getContent() {
        return content;
    }

    public Instant getDeliveryDate() {
        return deliveryDate;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public String getClientId() {
        return clientId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotificationEventResponse that)) return false;
        return Objects.equals(eventId, that.eventId)
                && Objects.equals(eventType, that.eventType)
                && Objects.equals(content, that.content)
                && Objects.equals(deliveryDate, that.deliveryDate)
                && Objects.equals(deliveryStatus, that.deliveryStatus)
                && Objects.equals(clientId, that.clientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, eventType, content, deliveryDate, deliveryStatus, clientId);
    }

    @Override
    public String toString() {
        return "NotificationEventResponse{" +
                "eventId='" + eventId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", content='" + content + '\'' +
                ", deliveryDate=" + deliveryDate +
                ", deliveryStatus='" + deliveryStatus + '\'' +
                ", clientId='" + clientId + '\'' +
                '}';
    }
}