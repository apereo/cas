package org.apereo.cas.monitor.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.monitor.MongoDbHealthIndicator;
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
@Slf4j
public class MongoDbMonitoringConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public HealthIndicator mongoHealthIndicator() {
        final var factory = new MongoDbConnectionFactory();
        final var mongoProps = casProperties.getMonitor().getMongo();
        final var mongoTemplate = factory.buildMongoTemplate(mongoProps);
        return new MongoDbHealthIndicator(mongoTemplate,
            casProperties.getMonitor().getWarn().getEvictionThreshold(),
            casProperties.getMonitor().getWarn().getThreshold());
    }
}
