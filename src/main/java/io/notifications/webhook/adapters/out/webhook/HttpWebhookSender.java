package io.notifications.webhook.adapters.out.webhook;

import io.notifications.webhook.domain.model.ClientId;
import io.notifications.webhook.domain.model.NotificationEvent;
import io.notifications.webhook.domain.ports.out.WebhookSender;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/*
 * HttpWebhookSender is an outbound adapter that delivers notification events via HTTPS POST.
 *
 * It validates that the configured target URL uses HTTPS, posts a minimal JSON payload, and returns
 * a DeliveryResult that captures success or failure without leaking HTTP client details into the domain.
 *
 * Timeouts are configured to keep calls bounded. Exceptions and non-2xx responses are converted into
 * DeliveryResult failures.
 */
public final class HttpWebhookSender implements WebhookSender {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);
    private static final int MAX_ERROR_MESSAGE_LENGTH = 500;

    private final URI targetUri;
    private final RestClient restClient;

    public HttpWebhookSender(String targetUrl) {
        Objects.requireNonNull(targetUrl, "targetUrl must not be null");

        URI uri = URI.create(targetUrl);
        if (uri.getScheme() == null || !uri.getScheme().equalsIgnoreCase("https")) {
            throw new IllegalArgumentException("WEBHOOK_TARGET_URL must be an https URL");
        }
        this.targetUri = uri;

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(READ_TIMEOUT);

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public DeliveryResult send(ClientId clientId, NotificationEvent notificationEvent) {
        Objects.requireNonNull(clientId, "clientId must not be null");
        Objects.requireNonNull(notificationEvent, "notificationEvent must not be null");

        Instant occurredAt = Instant.now();

        WebhookPayload payload = WebhookPayload.from(clientId, notificationEvent);

        try {
            ResponseEntity<Void> response = restClient
                    .post()
                    .uri(targetUri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            int status = response.getStatusCode().value();

            if (status >= 200 && status < 300) {
                return DeliveryResult.success(status, occurredAt);
            }

            return DeliveryResult.failure(Optional.of(status), "Non-2xx response from webhook target", occurredAt);
        } catch (Exception ex) {
            String message = ex.getMessage() == null ? "Webhook delivery failed" : ex.getMessage();
            return DeliveryResult.failure(Optional.empty(), sanitize(message), occurredAt);
        }
    }

    private static String sanitize(String message) {
        String trimmed = message.trim();
        if (trimmed.length() <= MAX_ERROR_MESSAGE_LENGTH) {
            return trimmed;
        }
        return trimmed.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }
}