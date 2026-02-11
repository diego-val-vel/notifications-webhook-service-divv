package io.notifications.webhook.config;

import io.notifications.webhook.adapters.out.persistence.NotificationEventRepositoryJpaAdapter;
import io.notifications.webhook.adapters.out.persistence.SpringDataNotificationEventJpaRepository;
import io.notifications.webhook.domain.ports.out.NotificationEventRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * PersistenceConfiguration wires the JPA adapter to the domain port.
 *
 * This configuration ensures that the domain depends only on the
 * NotificationEventRepository port while the concrete implementation
 * is provided by the persistence adapter.
 */
@Configuration
public class PersistenceConfiguration {

    @Bean
    public NotificationEventRepository notificationEventRepository(
            SpringDataNotificationEventJpaRepository jpaRepository
    ) {
        return new NotificationEventRepositoryJpaAdapter(jpaRepository);
    }
}