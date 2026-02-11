package io.notifications.webhook.adapters.out.persistence;

import io.notifications.webhook.domain.model.ClientId;
import io.notifications.webhook.domain.model.DeliveryAttempt;
import io.notifications.webhook.domain.model.NotificationEventId;
import io.notifications.webhook.domain.ports.out.DeliveryAttemptRepository;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/*
 * DeliveryAttemptRepositoryJpaAdapter is an outbound adapter that persists DeliveryAttempt domain models using JPA.
 *
 * It translates the domain model into DeliveryAttemptEntity and delegates persistence to a Spring Data repository.
 * It also supports minimal idempotency lookups by returning the attempted_at timestamp for a matching correlation id.
 */
public final class DeliveryAttemptRepositoryJpaAdapter implements DeliveryAttemptRepository {

    private final SpringDataDeliveryAttemptJpaRepository jpaRepository;

    public DeliveryAttemptRepositoryJpaAdapter(SpringDataDeliveryAttemptJpaRepository jpaRepository) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository, "jpaRepository must not be null");
    }

    @Override
    public void save(DeliveryAttempt attempt) {
        DeliveryAttemptRepository.requireValid(attempt);

        DeliveryAttemptEntity entity = new DeliveryAttemptEntity(
                attempt.id(),
                attempt.eventId().value(),
                attempt.clientId().value(),
                attempt.targetUrl(),
                attempt.attemptType(),
                attempt.result(),
                attempt.httpStatus().orElse(null),
                attempt.errorMessage().orElse(null),
                attempt.attemptedAt(),
                attempt.durationMs(),
                attempt.correlationId().orElse(null)
        );

        jpaRepository.save(entity);
    }

    @Override
    public Optional<Instant> findReplayAttemptedAt(ClientId clientId, NotificationEventId eventId, String correlationId) {
        Objects.requireNonNull(clientId, "clientId must not be null");
        Objects.requireNonNull(eventId, "eventId must not be null");
        DeliveryAttemptRepository.requireCorrelationId(correlationId);

        return jpaRepository
                .findTopByEventIdAndClientIdAndCorrelationIdOrderByAttemptedAtDesc(
                        eventId.value(),
                        clientId.value(),
                        correlationId.trim()
                )
                .map(DeliveryAttemptEntity::getAttemptedAt);
    }
}