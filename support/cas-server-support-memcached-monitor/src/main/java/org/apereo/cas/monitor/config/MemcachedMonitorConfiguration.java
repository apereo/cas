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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
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
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("componentSerializationPlan")
    private ObjectProvider<ComponentSerializationPlan> componentSerializationPlan;

    @Bean
    @ConditionalOnMissingBean(name = "memcachedMonitorTranscoder")
    @RefreshScope
    public Transcoder memcachedMonitorTranscoder() {
        val memcached = casProperties.getMonitor().getMemcached();
        return MemcachedUtils.newTranscoder(memcached, componentSerializationPlan.getObject().getRegisteredClasses());
    }

    @Bean
    @ConditionalOnEnabledHealthIndicator("memcachedHealthIndicator")
    @ConditionalOnMissingBean(name = "memcachedHealthIndicator")
    public HealthIndicator memcachedHealthIndicator() {
        val warn = casProperties.getMonitor().getWarn();
        return new MemcachedHealthIndicator(memcachedHealthClientPool(),
            warn.getEvictionThreshold(),
            warn.getThreshold());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "memcachedHealthClientPool")
    public ObjectPool<MemcachedClientIF> memcachedHealthClientPool() {
        val memcached = casProperties.getMonitor().getMemcached();
        val factory = new MemcachedPooledClientConnectionFactory(memcached, memcachedMonitorTranscoder());
        return new GenericObjectPool<>(factory);
    }
}
