package org.apereo.cas.monitor.config;

import org.apereo.cas.monitor.HealthCheckMonitor;
import org.apereo.cas.monitor.MemoryMonitor;
import org.apereo.cas.monitor.Monitor;
import org.apereo.cas.monitor.SessionMonitor;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasCoreMonitorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreMonitorConfiguration")
public class CasCoreMonitorConfiguration {
    
    @Bean
    public Monitor healthCheckMonitor() {
        return new HealthCheckMonitor();
    }

    @RefreshScope
    @Bean
    public Monitor memoryMonitor() {
        return new MemoryMonitor();
    }

    @RefreshScope
    @Bean
    public Monitor sessionMonitor() {
        return new SessionMonitor();
    }
    
}
