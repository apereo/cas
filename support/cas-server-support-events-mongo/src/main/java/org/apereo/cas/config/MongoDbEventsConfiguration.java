package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.events.EventsProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.mongo.MongoDbCasEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * This is {@link MongoDbEventsConfiguration}, defines certain beans via configuration
 * while delegating some to Spring namespaces inside the context config file.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("mongoDbEventsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MongoDbEventsConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public PersistenceExceptionTranslationPostProcessor persistenceExceptionTranslationPostProcessor() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @RefreshScope
    @Bean
    public MongoTemplate mongoEventsTemplate() {
        final EventsProperties.MongoDb mongo = casProperties.getEvents().getMongo();
        final MongoDbConnectionFactory factory = new MongoDbConnectionFactory();
        final MongoTemplate mongoTemplate = factory.buildMongoTemplate(mongo);
        factory.createCollection(mongoTemplate, mongo.getCollection(), mongo.isDropCollection());
        return mongoTemplate;
    }

    @Bean
    public CasEventRepository casEventRepository() {
        final EventsProperties.MongoDb mongo = casProperties.getEvents().getMongo();
        return new MongoDbCasEventRepository(
                mongoEventsTemplate(),
                mongo.getCollection());
    }
}
