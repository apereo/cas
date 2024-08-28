package org.apereo.cas.config;

import org.apereo.cas.MongoDbPropertySourceLocator;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import java.util.Objects;

/**
 * This is {@link CasMongoDbCloudConfigBootstrapAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.CasConfiguration, module = "mongo")
@AutoConfiguration
public class CasMongoDbCloudConfigBootstrapAutoConfiguration {
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
