package org.apereo.cas.monitor.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.monitor.EhCacheHealthIndicator;

import lombok.val;
import net.sf.ehcache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link EhCacheMonitorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 * @deprecated Since 6.2, due to Ehcache 2.x being unmaintained. Other registries are available, including Ehcache 3.x.
 */
@Configuration(value = "ehcacheMonitorConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Deprecated(since = "6.2.0")
public class EhCacheMonitorConfiguration {

    @ConditionalOnEnabledHealthIndicator("ehcacheHealthIndicator")
    @Autowired
    @Bean
    public HealthIndicator ehcacheHealthIndicator(final CasConfigurationProperties casProperties,
                                                  @Qualifier("ehcacheTicketCacheManager")
                                                  final CacheManager ehcacheTicketCacheManager) {
        val warn = casProperties.getMonitor().getWarn();
        return new EhCacheHealthIndicator(ehcacheTicketCacheManager, warn.getEvictionThreshold(), warn.getThreshold());
    }
}
