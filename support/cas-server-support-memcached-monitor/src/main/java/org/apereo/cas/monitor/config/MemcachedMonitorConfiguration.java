package org.apereo.cas.monitor.config;

import net.spy.memcached.MemcachedClientIF;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.monitor.MemcachedMonitor;
import org.apereo.cas.monitor.Monitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link MemcachedMonitorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("memcachedMonitorConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MemcachedMonitorConfiguration {
    
    @Autowired
    @Bean
    public Monitor memcachedMonitor(@Qualifier("memcachedClient") final MemcachedClientIF memcachedClient) {
        return new MemcachedMonitor(memcachedClient);
    }
}
