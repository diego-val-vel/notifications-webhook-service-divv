package io.notifications.webhook.domain.ports.in;

import io.notifications.webhook.domain.model.ClientId;
import io.notifications.webhook.domain.model.NotificationEvent;
import io.notifications.webhook.domain.model.NotificationEventFilter;

import java.util.List;
import java.util.Objects;

/*
 * QueryNotificationEventsUseCase exposes a domain-facing operation to query notification events for a client.
 * It supports optional filtering criteria while enforcing client isolation as a first-class input.
 */
public interface QueryNotificationEventsUseCase {

    List<NotificationEvent> query(Query query);

    final class Query {

        private final ClientId clientId;
        private final NotificationEventFilter filter;

        public Query(ClientId clientId, NotificationEventFilter filter) {
            this.clientId = Objects.requireNonNull(clientId, "clientId must not be null");
            this.filter = Objects.requireNonNull(filter, "filter must not be null");
        }

        public ClientId clientId() {
            return clientId;
        }

        public NotificationEventFilter filter() {
            return filter;
        }
    }
}