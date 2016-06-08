package org.apereo.cas.config;

import com.mongodb.MongoClientURI;
import org.apereo.cas.configuration.model.core.events.EventsProperties;
import org.apereo.cas.support.events.dao.CasEventRepository;
import org.apereo.cas.support.events.mongo.MongoDbCasEventRepository;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

/**
 * This is {@link MongoDbEventsConfiguration}, defines certain beans via configuration
 * while delegating some to Spring namespaces inside the context config file.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("mongoDbEventsConfiguration")
@EnableConfigurationProperties(EventsProperties.class)
public class MongoDbEventsConfiguration {

    @Autowired
    private EventsProperties eventsProperties;

    /**
     * Persistence exception translation post processor persistence exception translation post processor.
     *
     * @return the persistence exception translation post processor
     */
    @RefreshScope
    @Bean
    public PersistenceExceptionTranslationPostProcessor persistenceExceptionTranslationPostProcessor() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    /**
     * Mongo events template mongo template.
     *
     * @return the mongo template
     */
    @RefreshScope
    @Bean
    public MongoTemplate mongoEventsTemplate() {
        return new MongoTemplate(mongoAuthNEventsDbFactory());
    }

    /**
     * Mongo auth n events db factory simple mongo db factory.
     *
     * @return the simple mongo db factory
     */
    @RefreshScope
    @Bean
    public SimpleMongoDbFactory mongoAuthNEventsDbFactory() {
        try {
            return new SimpleMongoDbFactory(new MongoClientURI(this.eventsProperties.getMongodb().getClientUri()));
        }
        catch (final Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
    }

    @Bean
    public CasEventRepository casEventRepository() {
        return new MongoDbCasEventRepository(
                mongoEventsTemplate(),
                this.eventsProperties.getMongodb().getCollection(),
                this.eventsProperties.getMongodb().isDropCollection());
    }
}
