package io.notifications.webhook.domain.model;

/*
 * DeliveryStatus represents the delivery outcome state of a notification event within the scope of the challenge.
 * Only COMPLETED and FAILED are supported, matching the immutable JSON dataset contract.
 *
 * The enum also defines whether a status allows replay attempts.
 */
public enum DeliveryStatus {

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
        return true;
    }
}