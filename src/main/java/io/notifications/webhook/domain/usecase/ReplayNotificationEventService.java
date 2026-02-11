package io.notifications.webhook.domain.usecase;

import io.notifications.webhook.domain.model.ClientId;
import io.notifications.webhook.domain.model.DeliveryStatus;
import io.notifications.webhook.domain.model.NotificationEvent;
import io.notifications.webhook.domain.model.NotificationEventId;
import io.notifications.webhook.domain.model.NotificationEventNotFound;
import io.notifications.webhook.domain.model.ReplayNotAllowed;
import io.notifications.webhook.domain.ports.in.ReplayNotificationEventUseCase;
import io.notifications.webhook.domain.ports.out.DeliveryAttemptRepository;
import io.notifications.webhook.domain.ports.out.NotificationEventRepository;
import io.notifications.webhook.domain.ports.out.SubscriptionRegistry;
import io.notifications.webhook.domain.ports.out.WebhookSender;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/*
 * ReplayNotificationEventService implements the replay use case.
 *
 * It enforces the challenge rules:
 * - Event must exist and belong to the given client (tenant isolation).
 * - Replay is allowed only for FAILED delivery_status.
 * - Subscription must be satisfied (stubbed via SubscriptionRegistry).
 *
 * Minimal idempotency is supported via an optional idempotency key:
 * - If present, it is used as correlation_id.
 * - If a prior delivery_attempts row exists for (event_id, client_id, correlation_id), the webhook is not re-sent.
 *
 * The dataset is immutable; this service does not update or persist events.
 * It delegates webhook delivery to the WebhookSender outbound port.
 */
public final class ReplayNotificationEventService implements ReplayNotificationEventUseCase {

    private final NotificationEventRepository notificationEventRepository;
    private final WebhookSender webhookSender;
    private final SubscriptionRegistry subscriptionRegistry;
    private final DeliveryAttemptRepository deliveryAttemptRepository;

    public ReplayNotificationEventService(
            NotificationEventRepository notificationEventRepository,
            WebhookSender webhookSender,
            SubscriptionRegistry subscriptionRegistry,
            DeliveryAttemptRepository deliveryAttemptRepository
    ) {
        this.notificationEventRepository = Objects.requireNonNull(notificationEventRepository, "notificationEventRepository must not be null");
        this.webhookSender = Objects.requireNonNull(webhookSender, "webhookSender must not be null");
        this.subscriptionRegistry = Objects.requireNonNull(subscriptionRegistry, "subscriptionRegistry must not be null");
        this.deliveryAttemptRepository = Objects.requireNonNull(deliveryAttemptRepository, "deliveryAttemptRepository must not be null");
    }

    @Override
    public Result replay(Command command) {
        Objects.requireNonNull(command, "command must not be null");

        ClientId clientId = command.clientId();
        NotificationEventId notificationEventId = command.notificationEventId();
        Optional<String> idempotencyKey = command.idempotencyKey();

        NotificationEvent notificationEvent = notificationEventRepository
                .findByClientIdAndId(clientId, notificationEventId)
                .orElseThrow(() -> new NotificationEventNotFound(notificationEventId));

        DeliveryStatus deliveryStatus = notificationEvent.deliveryStatus();

        if (!notificationEvent.canBeReplayed()) {
            throw new ReplayNotAllowed(notificationEventId, deliveryStatus);
        }

        if (!subscriptionRegistry.isSubscribed(clientId, notificationEvent.eventType())) {
            throw new ReplayNotAllowed(notificationEventId, deliveryStatus);
        }

        Optional<String> correlationId = idempotencyKey
                .map(String::trim)
                .filter(s -> !s.isBlank());

        if (correlationId.isPresent()) {
            Optional<Instant> attemptedAt = deliveryAttemptRepository.findReplayAttemptedAt(
                    clientId,
                    notificationEventId,
                    correlationId.get()
            );

            if (attemptedAt.isPresent()) {
                return Result.accepted(attemptedAt.get());
            }
        }

        webhookSender.send(clientId, notificationEvent, correlationId);

        return Result.accepted(Instant.now());
    }
}