package org.apereo.cas.monitor.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.monitor.MonitorWarningProperties;
import org.apereo.cas.monitor.MemoryMonitor;
import org.apereo.cas.monitor.SessionMonitor;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
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
@Configuration("casCoreMonitorConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasCoreMonitorConfiguration {


    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "memoryHealthIndicator")
    @Bean
    public HealthIndicator memoryHealthIndicator() {
        final int freeMemThreshold = casProperties.getMonitor().getFreeMemThreshold();
        if (freeMemThreshold > 0) {
            LOGGER.debug("Configured memory monitor with free-memory threshold [{}]", freeMemThreshold);
            return new MemoryMonitor(freeMemThreshold);
        }
        return () -> Health.up().build();
    }

    @ConditionalOnMissingBean(name = "sessionHealthIndicator")
    @Bean
    public HealthIndicator sessionHealthIndicator() {
        final MonitorWarningProperties warnSt = casProperties.getMonitor().getSt().getWarn();
        final MonitorWarningProperties warnTgt = casProperties.getMonitor().getTgt().getWarn();
        if (warnSt.getThreshold() > 0 && warnTgt.getThreshold() > 0) {
            LOGGER.debug("Configured session monitor with service ticket threshold [{}] and session threshold [{}]",
                warnSt.getThreshold(), warnTgt.getThreshold());
            return new SessionMonitor(ticketRegistry, warnSt.getThreshold(), warnTgt.getThreshold());
        }
        return () -> Health.up().build();
    }
}
