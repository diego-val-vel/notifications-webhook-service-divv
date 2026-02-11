package io.notifications.webhook.domain.ports.out;

import io.notifications.webhook.domain.model.ClientId;
import io.notifications.webhook.domain.model.NotificationEvent;
import io.notifications.webhook.domain.model.NotificationEventFilter;
import io.notifications.webhook.domain.model.NotificationEventId;

import java.util.List;
import java.util.Optional;

/*
 * NotificationEventRepository is an outbound port that abstracts persistence for notification events.
 * It must enforce client isolation at the query level and provide retrieval operations needed by use cases.
 */
public interface NotificationEventRepository {

    List<NotificationEvent> findByClientId(ClientId clientId, NotificationEventFilter filter);

    Optional<NotificationEvent> findByClientIdAndId(ClientId clientId, NotificationEventId id);

    void save(NotificationEvent notificationEvent);
}