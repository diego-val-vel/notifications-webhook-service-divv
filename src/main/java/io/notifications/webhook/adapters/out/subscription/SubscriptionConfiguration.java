package io.notifications.webhook.adapters.out.subscription;

import io.notifications.webhook.domain.ports.out.SubscriptionRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * SubscriptionConfiguration wires the SubscriptionRegistry outbound port to a minimal
 * in-memory stub implementation.
 *
 * The challenge dataset does not include subscription information, so the default behavior
 * is to consider all clients subscribed to all event types.
 */
@Configuration
public class SubscriptionConfiguration {

    @Bean
    public SubscriptionRegistry subscriptionRegistry() {
        return new InMemorySubscriptionRegistry();
    }
}