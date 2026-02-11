package io.notifications.webhook.adapters.out.json.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * NotificationEventDto represents a single notification event entry
 * inside the external JSON snapshot file.
 * It mirrors the dataset structure and contains no domain logic.
 */
public final class NotificationEventDto {

    @JsonProperty("event_id")
    private final String eventId;

    @JsonProperty("event_type")
    private final String eventType;

    @JsonProperty("content")
    private final String content;

    @JsonProperty("delivery_date")
    private final String deliveryDate;

    @JsonProperty("delivery_status")
    private final String deliveryStatus;

    @JsonProperty("client_id")
    private final String clientId;

    public NotificationEventDto(
            String eventId,
            String eventType,
            String content,
            String deliveryDate,
            String deliveryStatus,
            String clientId
    ) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.content = content;
        this.deliveryDate = deliveryDate;
        this.deliveryStatus = deliveryStatus;
        this.clientId = clientId;
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

    public String deliveryDate() {
        return deliveryDate;
    }

    public String deliveryStatus() {
        return deliveryStatus;
    }

    public String clientId() {
        return clientId;
    }
}