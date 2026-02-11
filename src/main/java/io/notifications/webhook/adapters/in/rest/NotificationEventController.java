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
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/*
 * NotificationEventController exposes the self-service REST API that allows clients
 * to query their notification events and request a replay for failed deliveries.
 *
 * The controller is a thin inbound adapter that validates input, maps requests into
 * domain-level use cases, and converts domain results into transport DTOs.
 *
 * Date filters accept either ISO local dates (YYYY-MM-DD) or ISO instants (YYYY-MM-DDTHH:mm:ssZ).
 * Local dates are interpreted as UTC day boundaries: fromInclusive at 00:00:00Z and
 * toInclusive at 23:59:59.999999999Z.
 *
 * Replay supports an optional Idempotency-Key header to prevent duplicate deliveries for the same
 * event_id and client_id when the same key is reused.
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
            @RequestParam(value = "date_from", required = false) String dateFrom,
            @RequestParam(value = "date_to", required = false) String dateTo
    ) {
        DeliveryStatus parsedStatus = parseDeliveryStatus(deliveryStatus);

        Instant fromInclusive = parseFromInclusive(dateFrom);
        Instant toInclusive = parseToInclusive(dateTo);

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
            @PathVariable("notification_event_id") String notificationEventId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        ReplayNotificationEventUseCase.Command command = new ReplayNotificationEventUseCase.Command(
                ClientId.of(clientId),
                NotificationEventId.of(notificationEventId),
                Optional.ofNullable(idempotencyKey)
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

    private static Instant parseFromInclusive(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String value = raw.trim();

        try {
            if (value.contains("T")) {
                return Instant.parse(value);
            }
            LocalDate localDate = LocalDate.parse(value);
            return localDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date_from format. Use YYYY-MM-DD or ISO-8601 instant (e.g., 2024-03-15T00:00:00Z).");
        }
    }

    private static Instant parseToInclusive(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String value = raw.trim();

        try {
            if (value.contains("T")) {
                return Instant.parse(value);
            }
            LocalDate localDate = LocalDate.parse(value);
            return localDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).minusNanos(ONE_NANOSECOND);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date_to format. Use YYYY-MM-DD or ISO-8601 instant (e.g., 2024-03-15T23:59:59Z).");
        }
    }
}