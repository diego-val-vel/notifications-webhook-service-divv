package io.notifications.webhook.config;

import io.notifications.webhook.adapters.out.persistence.DeliveryAttemptRepositoryJpaAdapter;
import io.notifications.webhook.adapters.out.persistence.SpringDataDeliveryAttemptJpaRepository;
import io.notifications.webhook.domain.ports.out.DeliveryAttemptRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * DeliveryAttemptsPersistenceConfiguration wires the JPA adapter to the DeliveryAttemptRepository domain port.
 *
 * Delivery attempts are persisted to Postgres as metadata only. Notification events remain sourced from the
 * immutable JSON snapshot. This configuration is enabled by default.
 */
@Configuration
public class DeliveryAttemptsPersistenceConfiguration {

    @Bean
    public DeliveryAttemptRepository deliveryAttemptRepository(
            SpringDataDeliveryAttemptJpaRepository jpaRepository
    ) {
        return new DeliveryAttemptRepositoryJpaAdapter(jpaRepository);
    }
}