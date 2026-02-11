package io.notifications.webhook.adapters.out.webhook;

import io.notifications.webhook.domain.model.ClientId;
import io.notifications.webhook.domain.model.DeliveryAttempt;
import io.notifications.webhook.domain.model.DeliveryAttemptResult;
import io.notifications.webhook.domain.model.NotificationEvent;
import io.notifications.webhook.domain.ports.out.DeliveryAttemptRepository;
import io.notifications.webhook.domain.ports.out.WebhookSender;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/*
 * PersistingWebhookSender decorates another WebhookSender and records delivery attempt metadata in Postgres.
 *
 * This decorator also supports minimal idempotency correlation for replay deliveries:
 * - If a correlation id (Idempotency-Key) is provided, it is persisted as correlation_id.
 * - If absent, a random UUID correlation id is generated.
 *
 * This keeps persistence concerns out of the replay use case while ensuring that each replay delivery
 * produces a delivery_attempts row with SUCCESS/FAILURE, timing, HTTP status, and correlation id.
 */
public final class PersistingWebhookSender implements WebhookSender {

    private static final int MAX_CORRELATION_ID_LENGTH = 200;

    private final WebhookSender delegate;
    private final DeliveryAttemptRepository deliveryAttemptRepository;
    private final String targetUrl;

    public PersistingWebhookSender(
            WebhookSender delegate,
            DeliveryAttemptRepository deliveryAttemptRepository,
            String targetUrl
    ) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
        this.deliveryAttemptRepository = Objects.requireNonNull(deliveryAttemptRepository, "deliveryAttemptRepository must not be null");

        if (targetUrl == null || targetUrl.isBlank()) {
            throw new IllegalArgumentException("targetUrl must not be blank");
        }
        this.targetUrl = targetUrl;
    }

    @Override
    public DeliveryResult send(ClientId clientId, NotificationEvent notificationEvent) {
        return send(clientId, notificationEvent, Optional.empty());
    }

    @Override
    public DeliveryResult send(ClientId clientId, NotificationEvent notificationEvent, Optional<String> correlationId) {
        Objects.requireNonNull(clientId, "clientId must not be null");
        Objects.requireNonNull(notificationEvent, "notificationEvent must not be null");
        Objects.requireNonNull(correlationId, "correlationId must not be null");

        String effectiveCorrelationId = correlationId
                .flatMap(PersistingWebhookSender::normalizeCorrelationId)
                .orElseGet(() -> UUID.randomUUID().toString());

        long startedAtNs = System.nanoTime();
        DeliveryResult result = delegate.send(clientId, notificationEvent);
        long durationMs = (System.nanoTime() - startedAtNs) / 1_000_000L;

        DeliveryAttemptResult attemptResult = result.delivered()
                ? DeliveryAttemptResult.SUCCESS
                : DeliveryAttemptResult.FAILURE;

        Instant attemptedAt = result.occurredAt();

        Optional<Integer> httpStatus = result.httpStatus();
        Optional<String> errorMessage = result.errorMessage();

        DeliveryAttempt attempt = DeliveryAttempt.replayAttempt(
                notificationEvent.id(),
                clientId,
                targetUrl,
                attemptResult,
                httpStatus,
                errorMessage,
                attemptedAt,
                durationMs,
                Optional.of(effectiveCorrelationId)
        );

        deliveryAttemptRepository.save(attempt);

        return result;
    }

    private static Optional<String> normalizeCorrelationId(String raw) {
        if (raw == null) {
            return Optional.empty();
        }

        String trimmed = raw.trim();
        if (trimmed.isBlank()) {
            return Optional.empty();
        }

        if (trimmed.length() <= MAX_CORRELATION_ID_LENGTH) {
            return Optional.of(trimmed);
        }

        return Optional.of(trimmed.substring(0, MAX_CORRELATION_ID_LENGTH));
    }
}