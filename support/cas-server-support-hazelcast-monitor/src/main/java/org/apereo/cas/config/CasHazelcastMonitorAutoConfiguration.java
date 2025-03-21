package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.monitor.HazelcastHealthIndicator;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import com.hazelcast.core.HazelcastInstance;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasHazelcastMonitorAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Monitoring, module = "hazelcast")
@AutoConfiguration
public class CasHazelcastMonitorAutoConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnEnabledHealthIndicator("hazelcastHealthIndicator")
    public HealthIndicator hazelcastHealthIndicator(
        final CasConfigurationProperties casProperties,
        @Qualifier("casTicketRegistryHazelcastInstance")
        final ObjectProvider<HazelcastInstance> casTicketRegistryHazelcastInstance) {
        val warn = casProperties.getMonitor().getWarn();
        return new HazelcastHealthIndicator(warn.getEvictionThreshold(),
            warn.getThreshold(), casTicketRegistryHazelcastInstance);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public DisposableBean hazelcastMonitorDisposableBean(
        @Qualifier("casTicketRegistryHazelcastInstance")
        final HazelcastInstance casTicketRegistryHazelcastInstance) {
        return casTicketRegistryHazelcastInstance::shutdown;
    }
}
