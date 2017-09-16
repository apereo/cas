package org.apereo.cas.config;

import org.apereo.cas.MongoDbPropertySource;
import org.apereo.cas.MongoDbPropertySourceLocator;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@Configuration("mongoDbCloudConfigBootstrapConfiguration")
@ConditionalOnProperty(name = "cas.spring.cloud.mongo.uri")
public class MongoDbCloudConfigBootstrapConfiguration {
    
    
    @Autowired
    private ConfigurableEnvironment environment;

    @Bean
    public MongoDbPropertySourceLocator mongoDbPropertySourceLocator() {
        try {
            final MongoTemplate mongoTemplate = mongoDbCloudConfigurationTemplate();
            if (!mongoTemplate.collectionExists(MongoDbPropertySource.class.getSimpleName())) {
                mongoTemplate.createCollection(MongoDbPropertySource.class.getSimpleName());
            }
            return new MongoDbPropertySourceLocator(mongoTemplate);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Bean
    public MongoTemplate mongoDbCloudConfigurationTemplate() {
        final MongoDbConnectionFactory factory = new MongoDbConnectionFactory();
        final String uri = environment.getProperty("cas.spring.cloud.mongo.uri");
        return factory.buildMongoTemplate(uri);
    }
}
