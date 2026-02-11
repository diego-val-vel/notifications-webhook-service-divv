package io.notifications.webhook.adapters.out.webhook;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.notifications.webhook.domain.model.ClientId;
import io.notifications.webhook.domain.model.DeliveryAttempt;
import io.notifications.webhook.domain.model.DeliveryAttemptResult;
import io.notifications.webhook.domain.model.NotificationEvent;
import io.notifications.webhook.domain.ports.out.DeliveryAttemptRepository;
import io.notifications.webhook.domain.ports.out.WebhookSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/*
 * PersistingWebhookSender decorates another WebhookSender and records delivery attempt metadata in Postgres.
 *
 * It emits structured logs per delivery attempt and records Micrometer metrics:
 * - webhook_delivery_attempts_total{result=success|failure}
 * - webhook_delivery_latency_seconds
 *
 * Minimal idempotency correlation for replay deliveries is supported:
 * - If a correlation id (Idempotency-Key) is provided, it is persisted as correlation_id.
 * - If absent, a random UUID correlation id is generated.
 */
public final class PersistingWebhookSender implements WebhookSender {

    private static final Logger LOG = LoggerFactory.getLogger(PersistingWebhookSender.class);

    private static final int MAX_CORRELATION_ID_LENGTH = 200;

    private final WebhookSender delegate;
    private final DeliveryAttemptRepository deliveryAttemptRepository;
    private final String targetUrl;

    private final Counter deliverySuccessCounter;
    private final Counter deliveryFailureCounter;
    private final Timer deliveryLatencyTimer;

    public PersistingWebhookSender(
            WebhookSender delegate,
            DeliveryAttemptRepository deliveryAttemptRepository,
            String targetUrl,
            MeterRegistry meterRegistry
    ) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
        this.deliveryAttemptRepository = Objects.requireNonNull(deliveryAttemptRepository, "deliveryAttemptRepository must not be null");

        if (targetUrl == null || targetUrl.isBlank()) {
            throw new IllegalArgumentException("targetUrl must not be blank");
        }
        this.targetUrl = targetUrl;

        MeterRegistry registry = Objects.requireNonNull(meterRegistry, "meterRegistry must not be null");
        this.deliverySuccessCounter = Counter.builder("webhook_delivery_attempts_total")
                .tag("result", "success")
                .register(registry);
        this.deliveryFailureCounter = Counter.builder("webhook_delivery_attempts_total")
                .tag("result", "failure")
                .register(registry);
        this.deliveryLatencyTimer = Timer.builder("webhook_delivery_latency_seconds")
                .register(registry);
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
        DeliveryResult result;
        try {
            result = delegate.send(clientId, notificationEvent);
        } finally {
            long elapsedNs = System.nanoTime() - startedAtNs;
            this.deliveryLatencyTimer.record(elapsedNs, java.util.concurrent.TimeUnit.NANOSECONDS);
        }

        long durationMs = (System.nanoTime() - startedAtNs) / 1_000_000L;

        DeliveryAttemptResult attemptResult = result.delivered()
                ? DeliveryAttemptResult.SUCCESS
                : DeliveryAttemptResult.FAILURE;

        if (attemptResult == DeliveryAttemptResult.SUCCESS) {
            this.deliverySuccessCounter.increment();
        } else {
            this.deliveryFailureCounter.increment();
        }

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

        String httpStatusValue = httpStatus.map(String::valueOf).orElse("null");
        String eventTypeValue = notificationEvent.eventType() == null ? "null" : notificationEvent.eventType().toString();

        LOG.info(
                "webhook_delivery_attempt event_id={} client_id={} event_type={} result={} http_status={} duration_ms={} target_url={} correlation_id={}",
                notificationEvent.id(),
                clientId,
                eventTypeValue,
                attemptResult,
                httpStatusValue,
                durationMs,
                targetUrl,
                effectiveCorrelationId
        );

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