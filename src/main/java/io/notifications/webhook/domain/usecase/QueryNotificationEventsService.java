package io.notifications.webhook.domain.usecase;

import io.notifications.webhook.domain.model.NotificationEvent;
import io.notifications.webhook.domain.ports.in.QueryNotificationEventsUseCase;
import io.notifications.webhook.domain.ports.out.NotificationEventRepository;

import java.util.List;
import java.util.Objects;

/*
 * QueryNotificationEventsService implements the query use case by delegating to the repository port.
 * It keeps the application flow in the domain layer while staying independent from HTTP and persistence details.
 */
public final class QueryNotificationEventsService implements QueryNotificationEventsUseCase {

    private final NotificationEventRepository notificationEventRepository;

    public QueryNotificationEventsService(NotificationEventRepository notificationEventRepository) {
        this.notificationEventRepository = Objects.requireNonNull(notificationEventRepository, "notificationEventRepository must not be null");
    }

    @Override
    public List<NotificationEvent> query(Query query) {
        Objects.requireNonNull(query, "query must not be null");
        return notificationEventRepository.findByClientId(query.clientId(), query.filter());
    }
}