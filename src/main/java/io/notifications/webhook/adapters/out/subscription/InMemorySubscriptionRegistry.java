package io.notifications.webhook.adapters.out.subscription;

import io.notifications.webhook.domain.model.ClientId;
import io.notifications.webhook.domain.model.EventType;
import io.notifications.webhook.domain.ports.out.SubscriptionRegistry;

import java.util.Objects;

/*
 * InMemorySubscriptionRegistry is a minimal stub implementation of SubscriptionRegistry.
 * For the take-home challenge, subscriptions are not present in the immutable JSON dataset.
 *
 * This adapter intentionally implements the simplest behavior: all clients are subscribed
 * to all event types. The decision is documented and can be replaced later without
 * changing the domain use case code.
 */
public final class InMemorySubscriptionRegistry implements SubscriptionRegistry {

    @Override
    public boolean isSubscribed(ClientId clientId, EventType eventType) {
        Objects.requireNonNull(clientId, "clientId must not be null");
        Objects.requireNonNull(eventType, "eventType must not be null");
        return true;
    }
}