package io.notifications.webhook.config;

import io.notifications.webhook.adapters.out.persistence.NotificationEventRepositoryJpaAdapter;
import io.notifications.webhook.adapters.out.persistence.SpringDataNotificationEventJpaRepository;
import io.notifications.webhook.domain.ports.out.NotificationEventRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * PersistenceConfiguration wires the JPA adapter to the NotificationEventRepository domain port.
 *
 * This configuration is intentionally disabled by default so that the JSON snapshot remains the
 * primary source of truth for notification event queries (challenge requirement).
 *
 * To enable the JPA-backed repository for events, set:
 *   events.repository=jpa
 */
@Configuration
@ConditionalOnProperty(name = "events.repository", havingValue = "jpa")
public class PersistenceConfiguration {

    @Bean
    public NotificationEventRepository notificationEventRepository(
            SpringDataNotificationEventJpaRepository jpaRepository
    ) {
        return new NotificationEventRepositoryJpaAdapter(jpaRepository);
    }
}