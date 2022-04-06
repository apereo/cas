package org.apereo.cas.config;

import org.apereo.cas.MongoDbPropertySourceLocator;
import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeature;

import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import java.util.Objects;

/**
 * This is {@link MongoDbCloudConfigBootstrapConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "MongoDbCloudConfigBootstrapConfiguration", proxyBeanMethods = false)
@ConditionalOnFeature(feature = CasFeatureModule.FeatureCatalog.CasConfiguration, module = "mongo")
public class MongoDbCloudConfigBootstrapConfiguration {
    /**
     * MongoDb CAS configuration key URI.
     */
    public static final String CAS_CONFIGURATION_MONGODB_URI = "cas.spring.cloud.mongo.uri";

    @Bean
    public PropertySourceLocator mongoDbPropertySourceLocator(
        @Qualifier("mongoDbCloudConfigurationTemplate")
        final MongoOperations mongoDbCloudConfigurationTemplate) {
        return new MongoDbPropertySourceLocator(mongoDbCloudConfigurationTemplate);
    }

    @Bean
    public MongoOperations mongoDbCloudConfigurationTemplate(final ConfigurableEnvironment environment) {
        val uri = Objects.requireNonNull(environment.getProperty(CAS_CONFIGURATION_MONGODB_URI));
        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(uri));
    }
}
