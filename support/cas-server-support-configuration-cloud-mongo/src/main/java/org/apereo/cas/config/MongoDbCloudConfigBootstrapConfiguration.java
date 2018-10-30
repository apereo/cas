package org.apereo.cas.config;

import org.apereo.cas.MongoDbPropertySourceLocator;
import org.apereo.cas.mongo.MongoDbConnectionFactory;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.mongodb.core.MongoTemplate;

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
            val factory = new MongoDbConnectionFactory();
            val uri = environment.getProperty(CAS_CONFIGURATION_MONGODB_URI);
            return factory.buildMongoTemplate(uri);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new BeanCreationException("mongoDbCloudConfigurationTemplate", e.getMessage(), e);
        }
    }
}
