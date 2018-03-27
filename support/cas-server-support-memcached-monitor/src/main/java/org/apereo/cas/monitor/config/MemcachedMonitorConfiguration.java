package org.apereo.cas.monitor.config;

import lombok.extern.slf4j.Slf4j;
import net.spy.memcached.MemcachedClientIF;
import net.spy.memcached.transcoders.Transcoder;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apereo.cas.ComponentSerializationPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.monitor.MonitorProperties;
import org.apereo.cas.memcached.MemcachedPooledClientConnectionFactory;
import org.apereo.cas.memcached.MemcachedUtils;
import org.apereo.cas.monitor.MemcachedHealthIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.HealthIndicator;
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
@Slf4j
public class MemcachedMonitorConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("componentSerializationPlan")
    private ComponentSerializationPlan componentSerializationPlan;

    @Bean
    public Transcoder memcachedMonitorTranscoder() {
        final MonitorProperties.Memcached memcached = casProperties.getMonitor().getMemcached();
        return MemcachedUtils.newTranscoder(memcached, componentSerializationPlan.getRegisteredClasses());
    }

    @Bean
    public HealthIndicator memcachedHealthIndicator() {
        final MonitorProperties.Memcached memcached = casProperties.getMonitor().getMemcached();
        final MemcachedPooledClientConnectionFactory factory = new MemcachedPooledClientConnectionFactory(memcached, memcachedMonitorTranscoder());
        final ObjectPool<MemcachedClientIF> pool = new GenericObjectPool<>(factory);
        return new MemcachedHealthIndicator(pool, casProperties);
    }
}
