package org.apereo.cas.monitor.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.monitor.MonitorProperties;
import org.apereo.cas.monitor.HealthCheckMonitor;
import org.apereo.cas.monitor.MemoryMonitor;
import org.apereo.cas.monitor.Monitor;
import org.apereo.cas.monitor.SessionMonitor;
import org.apereo.cas.ticket.registry.TicketRegistry;
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

        if (casProperties.getMonitor().getFreeMemThreshold() > 0) {
            final MemoryMonitor bean = new MemoryMonitor();
            bean.setFreeMemoryWarnThreshold(casProperties.getMonitor().getFreeMemThreshold());
            monitors.add(bean);
        }

        final MonitorProperties.Warn warn = casProperties.getMonitor().getSt().getWarn();
        if (warn.getThreshold() > 0) {
            final SessionMonitor bean = new SessionMonitor();
            bean.setTicketRegistry(ticketRegistry);
            bean.setServiceTicketCountWarnThreshold(warn.getThreshold());
            bean.setSessionCountWarnThreshold(warn.getThreshold());
            monitors.add(bean);
        }

        return new HealthCheckMonitor(monitors);
    }
}
