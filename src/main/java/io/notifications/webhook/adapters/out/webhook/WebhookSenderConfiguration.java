package io.notifications.webhook.adapters.out.webhook;

import io.notifications.webhook.domain.ports.out.WebhookSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/*
 * WebhookSenderConfiguration provides the outbound WebhookSender port implementation.
 *
 * For the current stage of the project, a No-Op implementation is used to keep the
 * application runnable and the replay use case executable without performing real
 * HTTP delivery. This preserves hexagonal architecture by satisfying the outbound
 * dependency while keeping infrastructure concerns isolated.
 *
 * A concrete HTTP implementation can later replace this bean without impacting the
 * domain or inbound adapters.
 */
@Configuration(proxyBeanMethods = false)
public final class WebhookSenderConfiguration {

    @Bean
    @Primary
    public WebhookSender webhookSender() {
        return new NoOpWebhookSender();
    }
}