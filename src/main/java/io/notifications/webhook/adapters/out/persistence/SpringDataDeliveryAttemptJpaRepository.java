package io.notifications.webhook.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/*
 * SpringDataDeliveryAttemptJpaRepository is a Spring Data JPA repository for DeliveryAttemptEntity.
 *
 * This repository persists delivery attempt metadata to Postgres.
 */
public interface SpringDataDeliveryAttemptJpaRepository extends JpaRepository<DeliveryAttemptEntity, UUID> {

    Optional<DeliveryAttemptEntity> findTopByEventIdAndClientIdAndCorrelationIdOrderByAttemptedAtDesc(
            String eventId,
            String clientId,
            String correlationId
    );
}