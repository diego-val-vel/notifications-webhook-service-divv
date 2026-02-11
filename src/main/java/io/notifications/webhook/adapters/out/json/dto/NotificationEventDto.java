package io.notifications.webhook.adapters.out.json.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.JsonNode;

import java.util.Objects;

/*
 * NotificationEventDto represents a single notification event as defined in the JSON snapshot.
 * It is a Jackson-friendly DTO that preserves the snapshot contract and is later mapped into domain objects.
 */
public final class NotificationEventDto {

    @JsonProperty("event_id")
    private final String eventId;

    @JsonProperty("event_type")
    private final String eventType;

    @JsonProperty("content")
    private final JsonNode content;

    @JsonProperty("delivery_date")
    private final String deliveryDate;

    @JsonProperty("delivery_status")
    private final String deliveryStatus;

    @JsonProperty("client_id")
    private final String clientId;

    @JsonCreator
    public NotificationEventDto(
            @JsonProperty("event_id") String eventId,
            @JsonProperty("event_type") String eventType,
            @JsonProperty("content") JsonNode content,
            @JsonProperty("delivery_date") String deliveryDate,
            @JsonProperty("delivery_status") String deliveryStatus,
            @JsonProperty("client_id") String clientId
    ) {
        this.eventId = Objects.requireNonNull(eventId, "eventId must not be null");
        this.eventType = Objects.requireNonNull(eventType, "eventType must not be null");
        this.content = Objects.requireNonNull(content, "content must not be null");
        this.deliveryDate = Objects.requireNonNull(deliveryDate, "deliveryDate must not be null");
        this.deliveryStatus = Objects.requireNonNull(deliveryStatus, "deliveryStatus must not be null");
        this.clientId = Objects.requireNonNull(clientId, "clientId must not be null");
    }

    public String eventId() {
        return eventId;
    }

    public String eventType() {
        return eventType;
    }

    public JsonNode content() {
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