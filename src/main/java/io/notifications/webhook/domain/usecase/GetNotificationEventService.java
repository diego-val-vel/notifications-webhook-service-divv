package io.notifications.webhook.domain.usecase;

import io.notifications.webhook.domain.model.NotificationEvent;
import io.notifications.webhook.domain.model.NotificationEventNotFound;
import io.notifications.webhook.domain.ports.in.GetNotificationEventUseCase;
import io.notifications.webhook.domain.ports.out.NotificationEventRepository;

import java.util.Objects;

/*
 * GetNotificationEventService implements the use case to retrieve a single notification event for a client.
 * It enforces tenant isolation by querying by both clientId and eventId and throws a domain exception when not found.
 */
public final class GetNotificationEventService implements GetNotificationEventUseCase {

    private final NotificationEventRepository notificationEventRepository;

    public GetNotificationEventService(NotificationEventRepository notificationEventRepository) {
        this.notificationEventRepository = Objects.requireNonNull(notificationEventRepository, "notificationEventRepository must not be null");
    }

    @Override
    public NotificationEvent get(Query query) {
        Objects.requireNonNull(query, "query must not be null");
        return notificationEventRepository
                .findByClientIdAndId(query.clientId(), query.notificationEventId())
                .orElseThrow(() -> new NotificationEventNotFound(query.notificationEventId()));
    }
}