package io.notifications.webhook.config;

import io.notifications.webhook.domain.model.DomainClock;
import io.notifications.webhook.domain.model.SystemDomainClock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * DomainClockConfiguration wires the DomainClock abstraction for the application context.
 *
 * The default implementation is SystemDomainClock, backed by the system clock, which is suitable for
 * production runtime wiring while keeping use cases testable through dependency injection.
 */
@Configuration(proxyBeanMethods = false)
public final class DomainClockConfiguration {

    @Bean
    public DomainClock domainClock() {
        return new SystemDomainClock();
    }
}