package io.notifications.webhook.adapters.out.json;

import tools.jackson.databind.ObjectMapper;
import io.notifications.webhook.adapters.out.json.dto.NotificationEventsSnapshotDto;
import io.notifications.webhook.adapters.out.json.mapper.NotificationEventJsonMapper;
import io.notifications.webhook.domain.model.NotificationEvent;
import io.notifications.webhook.domain.ports.out.EventSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

/*
 * JsonEventSourceAdapter is an outbound adapter that implements the EventSource port.
 * It loads notification events from a JSON snapshot file located in the classpath.
 *
 * The adapter is responsible only for deserialization and mapping to domain aggregates.
 * All domain invariants are enforced by delegating object creation to the domain layer.
 *
 * This class contains no persistence, REST, or business logic responsibilities.
 */
public final class JsonEventSourceAdapter implements EventSource {

    private static final String SNAPSHOT_FILE = "notification_events.json";

    private final ObjectMapper objectMapper;
    private final NotificationEventJsonMapper mapper;

    public JsonEventSourceAdapter(ObjectMapper objectMapper,
                                  NotificationEventJsonMapper mapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.mapper = Objects.requireNonNull(mapper);
    }

    @Override
    public List<NotificationEvent> loadAll() {
        NotificationEventsSnapshotDto snapshot = readSnapshot();
        return snapshot.events()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    private NotificationEventsSnapshotDto readSnapshot() {
        try (InputStream inputStream =
                     Thread.currentThread()
                             .getContextClassLoader()
                             .getResourceAsStream(SNAPSHOT_FILE)) {

            if (inputStream == null) {
                throw new IllegalStateException("Snapshot file not found in classpath: " + SNAPSHOT_FILE);
            }

            return objectMapper.readValue(inputStream, NotificationEventsSnapshotDto.class);

        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read snapshot file: " + SNAPSHOT_FILE, ex);
        }
    }
}