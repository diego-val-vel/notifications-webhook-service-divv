package io.notifications.webhook.adapters.out.persistence;

import io.notifications.webhook.domain.model.ClientId;
import io.notifications.webhook.domain.model.NotificationEvent;
import io.notifications.webhook.domain.model.NotificationEventFilter;
import io.notifications.webhook.domain.model.NotificationEventId;
import io.notifications.webhook.domain.ports.out.NotificationEventRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/*
 * NotificationEventRepositoryJpaAdapter is the persistence adapter that bridges
 * the NotificationEventRepository domain port with Spring Data JPA.
 *
 * It enforces client isolation at the persistence level and performs
 * translation between domain aggregates and JPA entities.
 */
public class NotificationEventRepositoryJpaAdapter implements NotificationEventRepository {

    private final SpringDataNotificationEventJpaRepository jpaRepository;

    public NotificationEventRepositoryJpaAdapter(
            SpringDataNotificationEventJpaRepository jpaRepository
    ) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<NotificationEvent> findByClientId(
            ClientId clientId,
            NotificationEventFilter filter
    ) {
        return jpaRepository.findByClientId(clientId.value())
                .stream()
                .map(this::toDomain)
                .filter(event -> matchesFilter(event, filter))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<NotificationEvent> findByClientIdAndId(
            ClientId clientId,
            NotificationEventId id
    ) {
        return jpaRepository.findById(id.value())
                .filter(entity -> entity.getClientId().equals(clientId.value()))
                .map(this::toDomain);
    }

    @Override
    public void save(NotificationEvent notificationEvent) {
        jpaRepository.save(toEntity(notificationEvent));
    }

    private boolean matchesFilter(
            NotificationEvent event,
            NotificationEventFilter filter
    ) {
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

    private NotificationEvent toDomain(NotificationEventEntity entity) {
        return NotificationEvent.of(
                NotificationEventId.of(entity.getEventId()),
                ClientId.of(entity.getClientId()),
                entity.getEventType(),
                entity.getContent(),
                entity.getDeliveryDate(),
                entity.getDeliveryStatus()
        );
    }

    private NotificationEventEntity toEntity(NotificationEvent event) {
        return new NotificationEventEntity(
                event.id().value(),
                event.clientId().value(),
                event.eventType(),
                event.content(),
                event.deliveryDate(),
                event.deliveryStatus()
        );
    }
}