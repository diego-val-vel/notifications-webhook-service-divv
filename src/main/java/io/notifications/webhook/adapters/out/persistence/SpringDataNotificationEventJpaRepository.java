package io.notifications.webhook.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/*
 * SpringDataNotificationEventJpaRepository is the Spring Data JPA repository
 * responsible for basic CRUD operations on NotificationEventEntity.
 *
 * It is intentionally limited to persistence concerns.
 * Domain translation must happen in the adapter layer.
 */
public interface SpringDataNotificationEventJpaRepository
        extends JpaRepository<NotificationEventEntity, String> {

    List<NotificationEventEntity> findByClientId(String clientId);

}