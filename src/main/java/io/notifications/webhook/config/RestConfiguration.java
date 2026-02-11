package io.notifications.webhook.config;

import io.notifications.webhook.adapters.in.rest.mapper.NotificationEventRestMapper;
import io.notifications.webhook.domain.ports.in.GetNotificationEventUseCase;
import io.notifications.webhook.domain.ports.in.QueryNotificationEventsUseCase;
import io.notifications.webhook.domain.ports.in.ReplayNotificationEventUseCase;
import io.notifications.webhook.domain.ports.out.DeliveryAttemptRepository;
import io.notifications.webhook.domain.ports.out.NotificationEventRepository;
import io.notifications.webhook.domain.ports.out.SubscriptionRegistry;
import io.notifications.webhook.domain.ports.out.WebhookSender;
import io.notifications.webhook.domain.usecase.GetNotificationEventService;
import io.notifications.webhook.domain.usecase.QueryNotificationEventsService;
import io.notifications.webhook.domain.usecase.ReplayNotificationEventService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * RestConfiguration wires inbound use cases and REST mappers.
 * It composes domain services with outbound ports while keeping the domain free of framework dependencies.
 *
 * Replay wiring includes DeliveryAttemptRepository to support minimal idempotency for replay requests.
 */
@Configuration
public class RestConfiguration {

    @Bean
    public NotificationEventRestMapper notificationEventRestMapper() {
        return new NotificationEventRestMapper();
    }

    @Bean
    public QueryNotificationEventsUseCase queryNotificationEventsUseCase(NotificationEventRepository notificationEventRepository) {
        return new QueryNotificationEventsService(notificationEventRepository);
    }

    @Bean
    public GetNotificationEventUseCase getNotificationEventUseCase(NotificationEventRepository notificationEventRepository) {
        return new GetNotificationEventService(notificationEventRepository);
    }

    @Bean
    public ReplayNotificationEventUseCase replayNotificationEventUseCase(
            NotificationEventRepository notificationEventRepository,
            WebhookSender webhookSender,
            SubscriptionRegistry subscriptionRegistry,
            DeliveryAttemptRepository deliveryAttemptRepository
    ) {
        return new ReplayNotificationEventService(
                notificationEventRepository,
                webhookSender,
                subscriptionRegistry,
                deliveryAttemptRepository
        );
    }
}