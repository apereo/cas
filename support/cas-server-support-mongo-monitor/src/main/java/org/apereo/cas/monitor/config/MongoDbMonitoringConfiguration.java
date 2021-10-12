package org.apereo.cas.monitor.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.monitor.MongoDbHealthIndicator;

import lombok.val;
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
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * This is {@link MongoDbMonitoringConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "mongoDbMonitoringConfiguration", proxyBeanMethods = false)
public class MongoDbMonitoringConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "mongoHealthIndicatorTemplate")
    @Autowired
    public MongoTemplate mongoHealthIndicatorTemplate(
        final CasConfigurationProperties casProperties,
        @Qualifier("casSslContext")
        final CasSSLContext casSslContext) {
        val factory = new MongoDbConnectionFactory(casSslContext.getSslContext());
        val mongoProps = casProperties.getMonitor().getMongo();
        return factory.buildMongoTemplate(mongoProps);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnEnabledHealthIndicator("mongoHealthIndicator")
    @ConditionalOnMissingBean(name = "mongoHealthIndicator")
    @Autowired
    public HealthIndicator mongoHealthIndicator(
        final CasConfigurationProperties casProperties,
        @Qualifier("mongoHealthIndicatorTemplate")
        final MongoTemplate mongoHealthIndicatorTemplate) {
        val warn = casProperties.getMonitor().getWarn();
        return new MongoDbHealthIndicator(mongoHealthIndicatorTemplate, warn.getEvictionThreshold(), warn.getThreshold());
    }
}
