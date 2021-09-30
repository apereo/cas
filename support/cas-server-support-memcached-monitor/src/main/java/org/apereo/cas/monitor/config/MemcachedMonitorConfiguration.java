package org.apereo.cas.monitor.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.memcached.MemcachedPooledClientConnectionFactory;
import org.apereo.cas.memcached.MemcachedUtils;
import org.apereo.cas.monitor.MemcachedHealthIndicator;
import org.apereo.cas.util.serialization.ComponentSerializationPlan;

import lombok.val;
import net.spy.memcached.MemcachedClientIF;
import net.spy.memcached.transcoders.Transcoder;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link MemcachedMonitorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "memcachedMonitorConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MemcachedMonitorConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "memcachedMonitorTranscoder")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public Transcoder memcachedMonitorTranscoder(final CasConfigurationProperties casProperties,
                                                 @Qualifier("componentSerializationPlan")
                                                 final ComponentSerializationPlan componentSerializationPlan) {
        val memcached = casProperties.getMonitor().getMemcached();
        return MemcachedUtils.newTranscoder(memcached, componentSerializationPlan.getRegisteredClasses());
    }

    @Bean
    @ConditionalOnEnabledHealthIndicator("memcachedHealthIndicator")
    @ConditionalOnMissingBean(name = "memcachedHealthIndicator")
    @Autowired
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
    @Autowired
    public ObjectPool<MemcachedClientIF> memcachedHealthClientPool(
        @Qualifier("memcachedMonitorTranscoder")
        final Transcoder memcachedMonitorTranscoder,
        final CasConfigurationProperties casProperties) {
        val memcached = casProperties.getMonitor().getMemcached();
        val factory = new MemcachedPooledClientConnectionFactory(memcached, memcachedMonitorTranscoder);
        return new GenericObjectPool<>(factory);
    }
}
