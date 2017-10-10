package org.apereo.cas.monitor.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.monitor.MonitorProperties;
import org.apereo.cas.monitor.HealthCheckMonitor;
import org.apereo.cas.monitor.MemoryMonitor;
import org.apereo.cas.monitor.Monitor;
import org.apereo.cas.monitor.SessionMonitor;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link CasCoreMonitorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreMonitorConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreMonitorConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasCoreMonitorConfiguration.class);

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @ConditionalOnMissingBean(name = "healthCheckMonitor")
    @Bean
    public Monitor healthCheckMonitor() {
        final Map<String, Monitor> beans = applicationContext.getBeansOfType(Monitor.class, false, true);
        final Set<Monitor> monitors = beans.entrySet().stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());

        final int freeMemThreshold = casProperties.getMonitor().getFreeMemThreshold();
        if (freeMemThreshold > 0) {
            LOGGER.debug("Configured memory monitor with free-memory threshold [{}]", freeMemThreshold);
            monitors.add(new MemoryMonitor(freeMemThreshold));
        } else {
            LOGGER.debug("Memory monitor is disabled from the configuration");
        }

        final MonitorProperties.Warn warnSt = casProperties.getMonitor().getSt().getWarn();
        final MonitorProperties.Warn warnTgt = casProperties.getMonitor().getTgt().getWarn();
        if (warnSt.getThreshold() > 0 && warnTgt.getThreshold() > 0) {
            LOGGER.debug("Configured session monitor with service ticket threshold [{}] and session threshold [{}]",
                    warnSt.getThreshold(), warnTgt.getThreshold());
            final SessionMonitor bean = new SessionMonitor(ticketRegistry, warnSt.getThreshold(), warnTgt.getThreshold());
            monitors.add(bean);
        } else {
            LOGGER.debug("Session monitor is disabled from the configuration");
        }

        return new HealthCheckMonitor(monitors);
    }
}
