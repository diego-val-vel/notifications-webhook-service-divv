package io.notifications.webhook.adapters.out.webhook;

import io.micrometer.core.instrument.MeterRegistry;
import io.notifications.webhook.domain.ports.out.DeliveryAttemptRepository;
import io.notifications.webhook.domain.ports.out.WebhookSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/*
 * WebhookSenderConfiguration provides the outbound WebhookSender port implementation.
 *
 * Runtime default is a real HTTP sender that delivers notification events to the configured HTTPS target URL.
 * A persisting decorator records delivery attempt metadata in Postgres for each delivery, and emits metrics/logs.
 *
 * A No-Op implementation can be enabled explicitly via configuration for local runs or tests.
 */
@Configuration(proxyBeanMethods = false)
public final class WebhookSenderConfiguration {

    @Bean
    @Primary
    @ConditionalOnProperty(name = "app.webhook.sender", havingValue = "http", matchIfMissing = true)
    public WebhookSender httpWebhookSender(
            @Value("${app.webhook.target-url}") String targetUrl,
            DeliveryAttemptRepository deliveryAttemptRepository,
            MeterRegistry meterRegistry
    ) {
        WebhookSender httpSender = new HttpWebhookSender(targetUrl);
        return new PersistingWebhookSender(httpSender, deliveryAttemptRepository, targetUrl, meterRegistry);
    }

    @Bean
    @ConditionalOnProperty(name = "app.webhook.sender", havingValue = "noop")
    public WebhookSender noOpWebhookSender() {
        return new NoOpWebhookSender();
    }
}