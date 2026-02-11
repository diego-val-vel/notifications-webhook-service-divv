package io.notifications.webhook.domain.model;

import java.time.Instant;

/*
 * SystemDomainClock is the default DomainClock implementation backed by the system clock.
 * It centralizes time acquisition for production wiring while keeping use cases deterministic in tests.
 */
public final class SystemDomainClock implements DomainClock {

    @Override
    public Instant now() {
        return Instant.now();
    }
}