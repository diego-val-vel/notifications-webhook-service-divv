package io.notifications.webhook.config;

import io.notifications.webhook.domain.model.DomainClock;
import io.notifications.webhook.domain.ports.in.GetNotificationEventUseCase;
import io.notifications.webhook.domain.ports.in.QueryNotificationEventsUseCase;
import io.notifications.webhook.domain.ports.in.ReplayNotificationEventUseCase;
import io.notifications.webhook.domain.ports.out.NotificationEventRepository;
import io.notifications.webhook.domain.ports.out.WebhookSender;
import io.notifications.webhook.domain.usecase.GetNotificationEventService;
import io.notifications.webhook.domain.usecase.QueryNotificationEventsService;
import io.notifications.webhook.domain.usecase.ReplayNotificationEventService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * RestConfiguration wires the inbound self-service API use cases.
 *
 * Controllers depend on input ports, while use case implementations depend on output ports.
 * This configuration provides explicit dependency wiring consistent with hexagonal architecture
 * and keeps infrastructure concerns outside the domain layer.
 */
@Configuration(proxyBeanMethods = false)
public final class RestConfiguration {

    @Bean
    public QueryNotificationEventsUseCase queryNotificationEventsUseCase(NotificationEventRepository repository) {
        return new QueryNotificationEventsService(repository);
    }

    @Bean
    public GetNotificationEventUseCase getNotificationEventUseCase(NotificationEventRepository repository) {
        return new GetNotificationEventService(repository);
    }

    @Bean
    public ReplayNotificationEventUseCase replayNotificationEventUseCase(
            NotificationEventRepository repository,
            WebhookSender webhookSender,
            DomainClock domainClock
    ) {
        return new ReplayNotificationEventService(repository, webhookSender, domainClock);
    }
}