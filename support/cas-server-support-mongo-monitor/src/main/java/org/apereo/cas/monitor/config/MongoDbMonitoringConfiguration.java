package org.apereo.cas.monitor.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.monitor.CompositeHealthIndicator;
import org.apereo.cas.monitor.MongoDbHealthIndicator;
import org.apereo.cas.util.spring.BeanContainer;

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

import java.util.stream.Collectors;

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
    public BeanContainer<MongoTemplate> mongoHealthIndicatorTemplate(
        final CasConfigurationProperties casProperties,
        @Qualifier("casSslContext")
        final CasSSLContext casSslContext) {
        return BeanContainer.of(casProperties.getMonitor().getMongo()
            .stream()
            .map(mongoProps -> {
                val factory = new MongoDbConnectionFactory(casSslContext.getSslContext());
                return factory.buildMongoTemplate(mongoProps);
            })
            .collect(Collectors.toList()));
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnEnabledHealthIndicator("mongoHealthIndicator")
    @ConditionalOnMissingBean(name = "mongoHealthIndicator")
    @Autowired
    public HealthIndicator mongoHealthIndicator(
        final CasConfigurationProperties casProperties,
        @Qualifier("mongoHealthIndicatorTemplate")
        final BeanContainer<MongoTemplate> mongoHealthIndicatorTemplate) {

        val warn = casProperties.getMonitor().getWarn();
        val results = mongoHealthIndicatorTemplate.toList()
            .stream()
            .map(template -> new MongoDbHealthIndicator(template, warn.getEvictionThreshold(), warn.getThreshold()))
            .collect(Collectors.toList());
        return new CompositeHealthIndicator(results);
    }
}
