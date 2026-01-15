package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.memcached.MemcachedPooledClientConnectionFactory;
import org.apereo.cas.memcached.MemcachedUtils;
import org.apereo.cas.monitor.MemcachedHealthIndicator;
import org.apereo.cas.util.serialization.ComponentSerializationPlan;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import net.spy.memcached.MemcachedClientIF;
import net.spy.memcached.transcoders.Transcoder;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.health.autoconfigure.contributor.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasMemcachedMonitorAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 * @deprecated Since 7.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Monitoring, module = "memcached")
@AutoConfiguration
@Deprecated(since = "7.0.0")
public class CasMemcachedMonitorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "memcachedMonitorTranscoder")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Transcoder memcachedMonitorTranscoder(final CasConfigurationProperties casProperties,
                                                 @Qualifier("componentSerializationPlan")
                                                 final ComponentSerializationPlan componentSerializationPlan) {
        val memcached = casProperties.getMonitor().getMemcached();
        return MemcachedUtils.newTranscoder(memcached, componentSerializationPlan.getRegisteredClasses());
    }

    @Bean
    @ConditionalOnEnabledHealthIndicator("memcachedHealthIndicator")
    @ConditionalOnMissingBean(name = "memcachedHealthIndicator")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public HealthIndicator memcachedHealthIndicator(
        @Qualifier("memcachedHealthClientPool")
        final ObjectPool<MemcachedClientIF> memcachedHealthClientPool,
        final CasConfigurationProperties casProperties) {
        val warn = casProperties.getMonitor().getWarn();
        return new MemcachedHealthIndicator(memcachedHealthClientPool,
            warn.getEvictionThreshold(), warn.getThreshold());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "memcachedHealthClientPool")
    public ObjectPool<MemcachedClientIF> memcachedHealthClientPool(
        @Qualifier("memcachedMonitorTranscoder")
        final Transcoder memcachedMonitorTranscoder,
        final CasConfigurationProperties casProperties) {
        val memcached = casProperties.getMonitor().getMemcached();
        val factory = new MemcachedPooledClientConnectionFactory(memcached, memcachedMonitorTranscoder);
        return new GenericObjectPool<>(factory);
    }
}
