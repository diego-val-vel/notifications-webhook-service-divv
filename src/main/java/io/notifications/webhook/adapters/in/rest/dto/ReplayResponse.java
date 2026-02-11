package io.notifications.webhook.adapters.in.rest.dto;

import java.time.Instant;
import java.util.Objects;

/*
 * ReplayResponse is a REST response DTO that represents the outcome of a replay request.
 * It preserves the existing controller contract by exposing accepted(...) and rejected(...)
 * factory methods, while also providing JavaBeans getters for reliable Jackson serialization.
 */
public final class ReplayResponse {

    private final String notificationEventId;
    private final boolean accepted;
    private final Instant processedAt;

    private ReplayResponse(String notificationEventId, boolean accepted, Instant processedAt) {
        if (notificationEventId == null || notificationEventId.isBlank()) {
            throw new IllegalArgumentException("notificationEventId must not be null or blank");
        }
        if (processedAt == null) {
            throw new IllegalArgumentException("processedAt must not be null");
        }
        this.notificationEventId = notificationEventId;
        this.accepted = accepted;
        this.processedAt = processedAt;
    }

    public static ReplayResponse accepted(String notificationEventId, Instant processedAt) {
        return new ReplayResponse(notificationEventId, true, processedAt);
    }

    public static ReplayResponse rejected(String notificationEventId, Instant processedAt) {
        return new ReplayResponse(notificationEventId, false, processedAt);
    }

    public String notificationEventId() {
        return notificationEventId;
    }

    public boolean accepted() {
        return accepted;
    }

    public Instant processedAt() {
        return processedAt;
    }

    public String getNotificationEventId() {
        return notificationEventId;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReplayResponse that)) return false;
        return accepted == that.accepted
                && Objects.equals(notificationEventId, that.notificationEventId)
                && Objects.equals(processedAt, that.processedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationEventId, accepted, processedAt);
    }

    @Override
    public String toString() {
        return "ReplayResponse{" +
                "notificationEventId='" + notificationEventId + '\'' +
                ", accepted=" + accepted +
                ", processedAt=" + processedAt +
                '}';
    }
}