package org.apereo.cas.monitor.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.monitor.MemoryMonitorHealthIndicator;
import org.apereo.cas.monitor.SystemMonitorHealthIndicator;
import org.apereo.cas.monitor.TicketRegistryHealthIndicator;
import org.apereo.cas.ticket.registry.TicketRegistry;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasCoreMonitorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "casCoreMonitorConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasCoreMonitorConfiguration {
    @Autowired
    @Qualifier("ticketRegistry")
    private ObjectProvider<TicketRegistry> ticketRegistry;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("metricsEndpoint")
    private ObjectProvider<MetricsEndpoint> metricsEndpoint;

    @ConditionalOnMissingBean(name = "memoryHealthIndicator")
    @Bean
    @ConditionalOnEnabledHealthIndicator("memoryHealthIndicator")
    public HealthIndicator memoryHealthIndicator() {
        val freeMemThreshold = casProperties.getMonitor().getFreeMemThreshold();
        if (freeMemThreshold > 0) {
            LOGGER.debug("Configured memory monitor with free-memory threshold [{}]", freeMemThreshold);
            return new MemoryMonitorHealthIndicator(freeMemThreshold);
        }
        return () -> Health.up().build();
    }

    @ConditionalOnMissingBean(name = "sessionHealthIndicator")
    @Bean
    @ConditionalOnEnabledHealthIndicator("sessionHealthIndicator")
    public HealthIndicator sessionHealthIndicator() {
        val warnSt = casProperties.getMonitor().getSt().getWarn();
        val warnTgt = casProperties.getMonitor().getTgt().getWarn();
        if (warnSt.getThreshold() > 0 && warnTgt.getThreshold() > 0) {
            LOGGER.debug("Configured session monitor with service ticket threshold [{}] and session threshold [{}]",
                warnSt.getThreshold(), warnTgt.getThreshold());
            return new TicketRegistryHealthIndicator(ticketRegistry.getObject(), warnSt.getThreshold(), warnTgt.getThreshold());
        }
        return () -> Health.up().build();
    }

    @ConditionalOnMissingBean(name = "systemHealthIndicator")
    @Bean
    @ConditionalOnEnabledHealthIndicator("systemHealthIndicator")
    @ConditionalOnAvailableEndpoint(endpoint = MetricsEndpoint.class)
    public HealthIndicator systemHealthIndicator() {
        val warnLoad = casProperties.getMonitor().getLoad().getWarn();
        return new SystemMonitorHealthIndicator(metricsEndpoint.getObject(), warnLoad.getThreshold());
    }
}
