package org.apereo.cas.config;

import org.apereo.cas.MongoDbPropertySourceLocator;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import java.util.Objects;

/**
 * This is {@link MongoDbCloudConfigBootstrapConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Configuration("mongoDbCloudConfigBootstrapConfiguration")
public class MongoDbCloudConfigBootstrapConfiguration {
    /**
     * MongoDb CAS configuration key prefix.
     */
    public static final String CAS_CONFIGURATION_MONGODB_URI = "cas.spring.cloud.mongo.uri";

    @Autowired
    private ConfigurableEnvironment environment;

    @Bean
    public MongoDbPropertySourceLocator mongoDbPropertySourceLocator() {
        try {
            val mongoTemplate = mongoDbCloudConfigurationTemplate();
            return new MongoDbPropertySourceLocator(mongoTemplate);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new BeanCreationException("mongoDbPropertySourceLocator", e.getMessage(), e);
        }
    }

    @Bean
    public MongoTemplate mongoDbCloudConfigurationTemplate() {
        try {
            val uri = Objects.requireNonNull(environment.getProperty(CAS_CONFIGURATION_MONGODB_URI));
            return new MongoTemplate(new SimpleMongoClientDatabaseFactory(uri));
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new BeanCreationException("mongoDbCloudConfigurationTemplate", e.getMessage(), e);
        }
    }
}
