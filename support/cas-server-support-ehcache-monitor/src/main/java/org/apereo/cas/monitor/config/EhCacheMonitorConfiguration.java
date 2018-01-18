package org.apereo.cas.monitor.config;

import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.CacheManager;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.monitor.EhCacheHealthIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.HealthIndicator;
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
@Slf4j
public class EhCacheMonitorConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Bean
    public HealthIndicator ehcacheHealthIndicator(@Qualifier("ehcacheTicketCacheManager") final CacheManager ehcacheTicketCacheManager) {
        return new EhCacheHealthIndicator(ehcacheTicketCacheManager, casProperties);
    }
}
