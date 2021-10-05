package org.apereo.cas.monitor.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.monitor.HazelcastHealthIndicator;

import com.hazelcast.core.HazelcastInstance;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link HazelcastMonitorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "hazelcastMonitorConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class HazelcastMonitorConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnEnabledHealthIndicator("hazelcastHealthIndicator")
    @Autowired
    public HealthIndicator hazelcastHealthIndicator(
        final CasConfigurationProperties casProperties,
        @Qualifier("casTicketRegistryHazelcastInstance")
        final HazelcastInstance casTicketRegistryHazelcastInstance) {
        val warn = casProperties.getMonitor().getWarn();
        return new HazelcastHealthIndicator(warn.getEvictionThreshold(),
            warn.getThreshold(), casTicketRegistryHazelcastInstance);
    }

    @Bean
    @Autowired
    public DisposableBean hazelcastMonitorDisposableBean(
        @Qualifier("casTicketRegistryHazelcastInstance")
        final HazelcastInstance casTicketRegistryHazelcastInstance) {
        return casTicketRegistryHazelcastInstance::shutdown;
    }
}
