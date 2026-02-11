package io.notifications.webhook.adapters.out.json;

import io.notifications.webhook.domain.ports.out.EventSource;
import io.notifications.webhook.domain.ports.out.NotificationEventRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * JsonNotificationEventRepositoryConfiguration wires a JSON-backed implementation of the
 * NotificationEventRepository outbound port.
 *
 * This configuration makes the static JSON snapshot the default source of truth for query operations
 * while keeping the domain layer independent from infrastructure details.
 */
@Configuration
public class JsonNotificationEventRepositoryConfiguration {

    @Bean
    public NotificationEventRepository jsonNotificationEventRepository(EventSource eventSource) {
        return new JsonNotificationEventRepositoryAdapter(eventSource);
    }
}