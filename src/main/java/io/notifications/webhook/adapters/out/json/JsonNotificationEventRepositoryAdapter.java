package io.notifications.webhook.adapters.out.json;

import io.notifications.webhook.domain.model.ClientId;
import io.notifications.webhook.domain.model.NotificationEvent;
import io.notifications.webhook.domain.model.NotificationEventFilter;
import io.notifications.webhook.domain.model.NotificationEventId;
import io.notifications.webhook.domain.ports.out.EventSource;
import io.notifications.webhook.domain.ports.out.NotificationEventRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/*
 * JsonNotificationEventRepositoryAdapter is an outbound adapter that implements the NotificationEventRepository port.
 * It provides a read-only view of notification events backed by a static EventSource (JSON snapshot).
 *
 * This adapter enforces tenant isolation by filtering on clientId and applies query filters in-memory.
 * It does not persist or mutate events, since the snapshot is immutable by design.
 */
public final class JsonNotificationEventRepositoryAdapter implements NotificationEventRepository {

    private final EventSource eventSource;

    public JsonNotificationEventRepositoryAdapter(EventSource eventSource) {
        this.eventSource = Objects.requireNonNull(eventSource, "eventSource must not be null");
    }

    @Override
    public List<NotificationEvent> findByClientId(ClientId clientId, NotificationEventFilter filter) {
        Objects.requireNonNull(clientId, "clientId must not be null");
        Objects.requireNonNull(filter, "filter must not be null");

        return eventSource.loadAll()
                .stream()
                .filter(event -> event.clientId().equals(clientId))
                .filter(event -> matchesFilter(event, filter))
                .toList();
    }

    @Override
    public Optional<NotificationEvent> findByClientIdAndId(ClientId clientId, NotificationEventId id) {
        Objects.requireNonNull(clientId, "clientId must not be null");
        Objects.requireNonNull(id, "id must not be null");

        return eventSource.loadAll()
                .stream()
                .filter(event -> event.clientId().equals(clientId))
                .filter(event -> event.id().equals(id))
                .findFirst();
    }

    @Override
    public void save(NotificationEvent notificationEvent) {
        throw new UnsupportedOperationException("Notification events are read-only and cannot be persisted from the JSON-backed repository");
    }

    private boolean matchesFilter(NotificationEvent event, NotificationEventFilter filter) {
        if (filter.deliveryStatus().isPresent()
                && !event.deliveryStatus().equals(filter.deliveryStatus().get())) {
            return false;
        }

        if (filter.fromInclusive().isPresent()
                && event.deliveryDate().isBefore(filter.fromInclusive().get())) {
            return false;
        }

        if (filter.toInclusive().isPresent()
                && event.deliveryDate().isAfter(filter.toInclusive().get())) {
            return false;
        }

        return true;
    }
}