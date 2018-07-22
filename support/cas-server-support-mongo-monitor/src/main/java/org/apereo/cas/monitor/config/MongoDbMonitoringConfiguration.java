package org.apereo.cas.monitor.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.monitor.MongoDbHealthIndicator;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public HealthIndicator mongoHealthIndicator() {
        val factory = new MongoDbConnectionFactory();
        val mongoProps = casProperties.getMonitor().getMongo();
        val mongoTemplate = factory.buildMongoTemplate(mongoProps);
        return new MongoDbHealthIndicator(mongoTemplate,
            casProperties.getMonitor().getWarn().getEvictionThreshold(),
            casProperties.getMonitor().getWarn().getThreshold());
    }
}
