package io.notifications.webhook.domain.usecase;

import io.notifications.webhook.domain.model.DomainClock;
import io.notifications.webhook.domain.model.NotificationEvent;
import io.notifications.webhook.domain.model.NotificationEventNotFound;
import io.notifications.webhook.domain.model.ReplayNotAllowed;
import io.notifications.webhook.domain.ports.in.ReplayNotificationEventUseCase;
import io.notifications.webhook.domain.ports.out.NotificationEventRepository;
import io.notifications.webhook.domain.ports.out.WebhookSender;

import java.util.Objects;

/*
 * ReplayNotificationEventService implements the replay use case for a notification event.
 * It enforces tenant isolation, validates replay eligibility based on delivery status, and delegates the actual
 * delivery attempt to the WebhookSender port. This service remains independent from HTTP client details.
 */
public final class ReplayNotificationEventService implements ReplayNotificationEventUseCase {

    private final NotificationEventRepository notificationEventRepository;
    private final WebhookSender webhookSender;
    private final DomainClock domainClock;

    public ReplayNotificationEventService(
            NotificationEventRepository notificationEventRepository,
            WebhookSender webhookSender,
            DomainClock domainClock
    ) {
        this.notificationEventRepository = Objects.requireNonNull(notificationEventRepository, "notificationEventRepository must not be null");
        this.webhookSender = Objects.requireNonNull(webhookSender, "webhookSender must not be null");
        this.domainClock = Objects.requireNonNull(domainClock, "domainClock must not be null");
    }

    @Override
    public Result replay(Command command) {
        Objects.requireNonNull(command, "command must not be null");

        NotificationEvent notificationEvent = notificationEventRepository
                .findByClientIdAndId(command.clientId(), command.notificationEventId())
                .orElseThrow(() -> new NotificationEventNotFound(command.notificationEventId()));

        if (!notificationEvent.canBeReplayed()) {
            throw new ReplayNotAllowed(notificationEvent.id(), notificationEvent.deliveryStatus());
        }

        webhookSender.send(command.clientId(), notificationEvent);

        return Result.accepted(domainClock.now());
    }
}