package io.notifications.webhook.domain.model;

import java.time.Instant;

/*
 * DomainClock abstracts time acquisition for domain and use case code.
 * It enables deterministic testing and prevents hard coupling to system time.
 */
@FunctionalInterface
public interface DomainClock {

    Instant now();
}