package io.notifications.webhook.domain.ports.out;

import io.notifications.webhook.domain.model.ClientId;
import io.notifications.webhook.domain.model.EventType;

/*
 * SubscriptionRegistry is an outbound domain port that abstracts subscription checks.
 * The take-home challenge does not provide a subscription dataset, so this port enables
 * a simple stub implementation without introducing additional persistence or services.
 */
public interface SubscriptionRegistry {

    boolean isSubscribed(ClientId clientId, EventType eventType);
}