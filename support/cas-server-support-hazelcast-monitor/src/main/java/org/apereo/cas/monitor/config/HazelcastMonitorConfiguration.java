package org.apereo.cas.monitor.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.hz.HazelcastConfigurationFactory;
import org.apereo.cas.monitor.HazelcastHealthIndicator;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link HazelcastMonitorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("hazelcastMonitorConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class HazelcastMonitorConfiguration implements DisposableBean {

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "casHealthIndicatorHazelcastInstance")
    @Bean
    public HazelcastInstance casHealthIndicatorHazelcastInstance() {
        val hz = casProperties.getTicket().getRegistry().getHazelcast();
        LOGGER.debug("Creating Hazelcast instance using properties [{}]", hz);
        return Hazelcast.newHazelcastInstance(HazelcastConfigurationFactory.build(hz));
    }

    @Bean
    @RefreshScope
    @ConditionalOnEnabledHealthIndicator("hazelcastHealthIndicator")
    public HealthIndicator hazelcastHealthIndicator() {
        val hazelcastInstance = casHealthIndicatorHazelcastInstance();
        val warn = casProperties.getMonitor().getWarn();
        return new HazelcastHealthIndicator(
            warn.getEvictionThreshold(),
            warn.getThreshold(),
            hazelcastInstance
        );
    }

    @Override
    public void destroy() {
        val hz = casHealthIndicatorHazelcastInstance();
        if (hz != null) {
            hz.shutdown();
        }
    }
}
