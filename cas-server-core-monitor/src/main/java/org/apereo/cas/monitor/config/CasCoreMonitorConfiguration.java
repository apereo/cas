package org.apereo.cas.monitor.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.monitor.HealthCheckMonitor;
import org.apereo.cas.monitor.MemoryMonitor;
import org.apereo.cas.monitor.Monitor;
import org.apereo.cas.monitor.SessionMonitor;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.Map;
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
        final Collection monitors = beans.entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());

        final HealthCheckMonitor bean = new HealthCheckMonitor();
        bean.setMonitors(monitors);
        return bean;
    }

    @RefreshScope
    @Bean
    public Monitor memoryMonitor() {
        final MemoryMonitor bean = new MemoryMonitor();
        bean.setFreeMemoryWarnThreshold(casProperties.getMonitor().getFreeMemThreshold());
        return bean;
    }

    @RefreshScope
    @Bean
    public Monitor sessionMonitor() {
        final SessionMonitor bean = new SessionMonitor();
        bean.setTicketRegistry(ticketRegistry);
        bean.setServiceTicketCountWarnThreshold(casProperties.getMonitor().getSt().getWarn().getThreshold());
        bean.setSessionCountWarnThreshold(casProperties.getMonitor().getTgt().getWarn().getThreshold());
        return bean;
    }
}
