package org.apereo.cas.monitor.config;

import net.sf.ehcache.CacheManager;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.monitor.EhCacheMonitor;
import org.apereo.cas.monitor.Monitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link EhCacheMonitorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("ehcacheMonitorConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class EhCacheMonitorConfiguration {
    
    @Autowired
    @Bean
    public Monitor ehcacheMonitor(@Qualifier("ehcacheTicketCacheManager") final CacheManager ehcacheTicketCacheManager) {
        return new EhCacheMonitor(ehcacheTicketCacheManager);
    }
}
