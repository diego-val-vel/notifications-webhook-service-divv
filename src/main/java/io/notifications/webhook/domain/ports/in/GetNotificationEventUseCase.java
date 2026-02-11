package io.notifications.webhook.domain.ports.in;

import io.notifications.webhook.domain.model.ClientId;
import io.notifications.webhook.domain.model.NotificationEvent;
import io.notifications.webhook.domain.model.NotificationEventId;

import java.util.Objects;

/*
 * GetNotificationEventUseCase exposes a domain-facing operation to retrieve a single notification event.
 * ClientId is required to enforce tenant isolation and prevent cross-client data access.
 */
public interface GetNotificationEventUseCase {

    NotificationEvent get(Query query);

    final class Query {

        private final ClientId clientId;
        private final NotificationEventId notificationEventId;

        public Query(ClientId clientId, NotificationEventId notificationEventId) {
            this.clientId = Objects.requireNonNull(clientId, "clientId must not be null");
            this.notificationEventId = Objects.requireNonNull(notificationEventId, "notificationEventId must not be null");
        }

        public ClientId clientId() {
            return clientId;
        }

        public NotificationEventId notificationEventId() {
            return notificationEventId;
        }
    }
}