package io.notifications.webhook.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.notifications.webhook.domain.model.NotificationEventNotFound;
import io.notifications.webhook.domain.model.ReplayNotAllowed;
import io.notifications.webhook.domain.ports.in.ReplayNotificationEventUseCase;

import java.util.Objects;

/*
 * ReplayNotificationEventUseCaseObservabilityDecorator provides Micrometer metrics for replay requests while
 * keeping the domain layer framework-free.
 *
 * Metrics:
 * - notification_replay_total{result=accepted|rejected|not_found|failure}
 */
public final class ReplayNotificationEventUseCaseObservabilityDecorator implements ReplayNotificationEventUseCase {

    private final ReplayNotificationEventUseCase delegate;

    private final Counter acceptedCounter;
    private final Counter rejectedCounter;
    private final Counter notFoundCounter;
    private final Counter failureCounter;

    public ReplayNotificationEventUseCaseObservabilityDecorator(
            ReplayNotificationEventUseCase delegate,
            MeterRegistry meterRegistry
    ) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");

        MeterRegistry registry = Objects.requireNonNull(meterRegistry, "meterRegistry must not be null");
        this.acceptedCounter = Counter.builder("notification_replay_total").tag("result", "accepted").register(registry);
        this.rejectedCounter = Counter.builder("notification_replay_total").tag("result", "rejected").register(registry);
        this.notFoundCounter = Counter.builder("notification_replay_total").tag("result", "not_found").register(registry);
        this.failureCounter = Counter.builder("notification_replay_total").tag("result", "failure").register(registry);
    }

    @Override
    public Result replay(Command command) {
        try {
            Result result = delegate.replay(command);
            acceptedCounter.increment();
            return result;
        } catch (NotificationEventNotFound ex) {
            notFoundCounter.increment();
            throw ex;
        } catch (ReplayNotAllowed ex) {
            rejectedCounter.increment();
            throw ex;
        } catch (RuntimeException ex) {
            failureCounter.increment();
            throw ex;
        }
    }
}