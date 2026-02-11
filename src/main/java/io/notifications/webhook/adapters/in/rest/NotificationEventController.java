package io.notifications.webhook.adapters.in.rest;

import io.notifications.webhook.adapters.in.rest.dto.NotificationEventResponse;
import io.notifications.webhook.adapters.in.rest.dto.NotificationEventsResponse;
import io.notifications.webhook.adapters.in.rest.dto.ReplayResponse;
import io.notifications.webhook.adapters.in.rest.mapper.NotificationEventRestMapper;
import io.notifications.webhook.domain.model.ClientId;
import io.notifications.webhook.domain.model.DeliveryStatus;
import io.notifications.webhook.domain.model.NotificationEvent;
import io.notifications.webhook.domain.model.NotificationEventFilter;
import io.notifications.webhook.domain.model.NotificationEventId;
import io.notifications.webhook.domain.ports.in.GetNotificationEventUseCase;
import io.notifications.webhook.domain.ports.in.QueryNotificationEventsUseCase;
import io.notifications.webhook.domain.ports.in.ReplayNotificationEventUseCase;
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/*
 * NotificationEventController exposes the self-service REST API that allows clients
 * to query their notification events and request a replay for failed deliveries.
 *
 * The controller is a thin inbound adapter that validates input, maps requests into
 * domain-level use cases, and converts domain results into transport DTOs.
 *
 * Exception translation is handled centrally by RestExceptionHandler.
 */
@RestController
@RequestMapping("/notification_events")
public final class NotificationEventController {

    private static final long ONE_NANOSECOND = 1L;

    private final QueryNotificationEventsUseCase queryNotificationEventsUseCase;
    private final GetNotificationEventUseCase getNotificationEventUseCase;
    private final ReplayNotificationEventUseCase replayNotificationEventUseCase;

    public NotificationEventController(
            QueryNotificationEventsUseCase queryNotificationEventsUseCase,
            GetNotificationEventUseCase getNotificationEventUseCase,
            ReplayNotificationEventUseCase replayNotificationEventUseCase
    ) {
        this.queryNotificationEventsUseCase = Objects.requireNonNull(queryNotificationEventsUseCase);
        this.getNotificationEventUseCase = Objects.requireNonNull(getNotificationEventUseCase);
        this.replayNotificationEventUseCase = Objects.requireNonNull(replayNotificationEventUseCase);
    }

    @GetMapping
    public NotificationEventsResponse query(
            @RequestParam("client_id") @NotBlank String clientId,
            @RequestParam(value = "delivery_status", required = false) String deliveryStatus,
            @RequestParam(value = "date_from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(value = "date_to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        DeliveryStatus parsedStatus = parseDeliveryStatus(deliveryStatus);

        Instant fromInclusive = dateFrom == null
                ? null
                : dateFrom.atStartOfDay().toInstant(ZoneOffset.UTC);

        Instant toInclusive = dateTo == null
                ? null
                : dateTo.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).minusNanos(ONE_NANOSECOND);

        NotificationEventFilter filter = buildFilter(fromInclusive, toInclusive, parsedStatus);

        QueryNotificationEventsUseCase.Query query = new QueryNotificationEventsUseCase.Query(
                ClientId.of(clientId),
                filter
        );

        List<NotificationEvent> events = queryNotificationEventsUseCase.query(query);

        return NotificationEventsResponse.of(NotificationEventRestMapper.toResponseList(events));
    }

    @GetMapping("/{notification_event_id}")
    public NotificationEventResponse getById(
            @RequestParam("client_id") @NotBlank String clientId,
            @PathVariable("notification_event_id") String notificationEventId
    ) {
        GetNotificationEventUseCase.Query query = new GetNotificationEventUseCase.Query(
                ClientId.of(clientId),
                NotificationEventId.of(notificationEventId)
        );

        NotificationEvent event = getNotificationEventUseCase.get(query);
        return NotificationEventRestMapper.toResponse(event);
    }

    @PostMapping("/{notification_event_id}/replay")
    public ReplayResponse replay(
            @RequestParam("client_id") @NotBlank String clientId,
            @PathVariable("notification_event_id") String notificationEventId
    ) {
        ReplayNotificationEventUseCase.Command command = new ReplayNotificationEventUseCase.Command(
                ClientId.of(clientId),
                NotificationEventId.of(notificationEventId)
        );

        ReplayNotificationEventUseCase.Result result = replayNotificationEventUseCase.replay(command);

        Instant processedAt = result.requestedAt().orElseGet(Instant::now);

        return result.accepted()
                ? ReplayResponse.accepted(notificationEventId, processedAt)
                : ReplayResponse.rejected(notificationEventId, processedAt);
    }

    private static NotificationEventFilter buildFilter(
            Instant fromInclusive,
            Instant toInclusive,
            DeliveryStatus deliveryStatus
    ) {
        if (fromInclusive == null && toInclusive == null && deliveryStatus == null) {
            return NotificationEventFilter.empty();
        }
        return NotificationEventFilter.of(fromInclusive, toInclusive, deliveryStatus);
    }

    private static DeliveryStatus parseDeliveryStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        return DeliveryStatus.valueOf(normalized);
    }
}