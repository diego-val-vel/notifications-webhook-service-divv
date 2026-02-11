package io.notifications.webhook.adapters.out.json;

import tools.jackson.databind.ObjectMapper;
import io.notifications.webhook.adapters.out.json.mapper.NotificationEventJsonMapper;
import io.notifications.webhook.domain.ports.out.EventSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * JsonEventSourceConfiguration wires the JsonEventSourceAdapter as the
 * implementation of the EventSource outbound port.
 *
 * This configuration keeps infrastructure concerns outside of the domain layer
 * and ensures proper dependency injection through Spring.
 */
@Configuration
public class JsonEventSourceConfiguration {

    @Bean
    public NotificationEventJsonMapper notificationEventJsonMapper() {
        return new NotificationEventJsonMapper();
    }

    @Bean
    public EventSource jsonEventSource(ObjectMapper objectMapper,
                                       NotificationEventJsonMapper mapper) {
        return new JsonEventSourceAdapter(objectMapper, mapper);
    }
}