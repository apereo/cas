package org.apereo.cas.config;

import com.mongodb.MongoClientURI;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.mongo.MongoDbCasEventRepository;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.mongodb.MongoDbFactory;
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
        return new MongoTemplate(mongoAuthNEventsDbFactory());
    }

    @RefreshScope
    @Bean
    public MongoDbFactory mongoAuthNEventsDbFactory() {
        try {
            return new SimpleMongoDbFactory(new MongoClientURI(casProperties.getEvents().getMongodb().getClientUri()));
        } catch (final Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
    }

    @Bean
    public CasEventRepository casEventRepository() {
        return new MongoDbCasEventRepository(
                mongoEventsTemplate(),
                casProperties.getEvents().getMongodb().getCollection(),
                casProperties.getEvents().getMongodb().isDropCollection());
    }
}
