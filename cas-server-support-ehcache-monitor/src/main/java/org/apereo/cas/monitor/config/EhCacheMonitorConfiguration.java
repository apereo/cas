package org.apereo.cas.monitor.config;

import org.apereo.cas.monitor.EhCacheMonitor;
import org.apereo.cas.monitor.Monitor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link EhCacheMonitorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("ehcacheMonitorConfiguration")
public class EhCacheMonitorConfiguration {
    
    @Bean
    public Monitor ehcacheMonitor() {
        return new EhCacheMonitor();
    }
}
