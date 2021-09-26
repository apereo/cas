package org.apereo.cas.config;

import org.apereo.cas.MongoDbPropertySourceLocator;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
@Configuration(value = "mongoDbCloudConfigBootstrapConfiguration", proxyBeanMethods = false)
public class MongoDbCloudConfigBootstrapConfiguration {
    /**
     * MongoDb CAS configuration key prefix.
     */
    public static final String CAS_CONFIGURATION_MONGODB_URI = "cas.spring.cloud.mongo.uri";

    @Bean
    @Autowired
    public MongoDbPropertySourceLocator mongoDbPropertySourceLocator(
        @Qualifier("mongoDbCloudConfigurationTemplate")
        final MongoTemplate mongoDbCloudConfigurationTemplate) {
        return new MongoDbPropertySourceLocator(mongoDbCloudConfigurationTemplate);
    }

    @Bean
    @Autowired
    public MongoTemplate mongoDbCloudConfigurationTemplate(final ConfigurableEnvironment environment) {
        val uri = Objects.requireNonNull(environment.getProperty(CAS_CONFIGURATION_MONGODB_URI));
        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(uri));
    }
}
