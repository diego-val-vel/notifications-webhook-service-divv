package io.notifications.webhook.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/*
 * NotificationEventFilter represents domain-level criteria for querying notification events.
 * It supports filtering by an event creation/delivery date range and by delivery status.
 * This abstraction keeps filtering semantics independent from HTTP query parameters and persistence details.
 */
public final class NotificationEventFilter {

    private final Optional<Instant> fromInclusive;
    private final Optional<Instant> toInclusive;
    private final Optional<DeliveryStatus> deliveryStatus;

    private NotificationEventFilter(
            Optional<Instant> fromInclusive,
            Optional<Instant> toInclusive,
            Optional<DeliveryStatus> deliveryStatus
    ) {
        this.fromInclusive = Objects.requireNonNull(fromInclusive, "fromInclusive must not be null");
        this.toInclusive = Objects.requireNonNull(toInclusive, "toInclusive must not be null");
        this.deliveryStatus = Objects.requireNonNull(deliveryStatus, "deliveryStatus must not be null");

        if (this.fromInclusive.isPresent() && this.toInclusive.isPresent()) {
            Instant from = this.fromInclusive.get();
            Instant to = this.toInclusive.get();
            if (from.isAfter(to)) {
                throw new IllegalArgumentException("fromInclusive must not be after toInclusive");
            }
        }
    }

    public static NotificationEventFilter of(
            Instant fromInclusive,
            Instant toInclusive,
            DeliveryStatus deliveryStatus
    ) {
        return new NotificationEventFilter(
                Optional.ofNullable(fromInclusive),
                Optional.ofNullable(toInclusive),
                Optional.ofNullable(deliveryStatus)
        );
    }

    public static NotificationEventFilter empty() {
        return new NotificationEventFilter(Optional.empty(), Optional.empty(), Optional.empty());
    }

    public Optional<Instant> fromInclusive() {
        return fromInclusive;
    }

    public Optional<Instant> toInclusive() {
        return toInclusive;
    }

    public Optional<DeliveryStatus> deliveryStatus() {
        return deliveryStatus;
    }
}