package io.notifications.webhook.domain.ports.out;

import io.notifications.webhook.domain.model.NotificationEvent;

import java.util.List;

/*
 * EventSource is an outbound port that provides the initial set of notification events to the application.
 * Implementations may read from JSON, a database, or any other source, but the domain remains independent.
 */
public interface EventSource {

    List<NotificationEvent> loadAll();
}