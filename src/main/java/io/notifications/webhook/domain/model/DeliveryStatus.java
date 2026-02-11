package io.notifications.webhook.domain.model;

/*
 * DeliveryStatus represents the lifecycle state of a notification delivery.
 * It models business semantics instead of raw persistence values and defines
 * which states allow replay attempts.
 */
public enum DeliveryStatus {

    PENDING(false),
    COMPLETED(false),
    FAILED(true);

    private final boolean replayAllowed;

    DeliveryStatus(boolean replayAllowed) {
        this.replayAllowed = replayAllowed;
    }

    public boolean isReplayAllowed() {
        return replayAllowed;
    }

    public boolean isFinalState() {
        return this == COMPLETED || this == FAILED;
    }
}