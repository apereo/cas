package org.apereo.cas.monitor.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.monitor.MongoDbHealthIndicator;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * This is {@link MongoDbMonitoringConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("mongoDbMonitoringConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MongoDbMonitoringConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @RefreshScope
    public MongoTemplate mongoHealthIndicatorTemplate() {
        val factory = new MongoDbConnectionFactory();
        val mongoProps = casProperties.getMonitor().getMongo();
        return factory.buildMongoTemplate(mongoProps);
    }



    @Bean
    @RefreshScope
    public HealthIndicator mongoHealthIndicator() {
        return new MongoDbHealthIndicator(mongoHealthIndicatorTemplate(),
            casProperties.getMonitor().getWarn().getEvictionThreshold(),
            casProperties.getMonitor().getWarn().getThreshold());
    }
}
