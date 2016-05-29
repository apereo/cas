package org.apereo.cas.monitor.config;

import org.apereo.cas.monitor.MemcachedMonitor;
import org.apereo.cas.monitor.Monitor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link MemcachedMonitorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("memcachedMonitorConfiguration")
public class MemcachedMonitorConfiguration {
    
    @Bean
    public Monitor memcachedMonitor() {
        return new MemcachedMonitor();
    }
}
