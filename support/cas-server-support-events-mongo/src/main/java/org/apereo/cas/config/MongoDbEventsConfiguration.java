package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.CasEventRepositoryFilter;
import org.apereo.cas.support.events.mongo.MongoDbCasEventRepository;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.mongodb.core.MongoTemplate;

import javax.net.ssl.SSLContext;

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

    @Autowired
    @Qualifier("sslContext")
    private ObjectProvider<SSLContext> sslContext;

    @RefreshScope
    @Bean
    public PersistenceExceptionTranslationPostProcessor persistenceExceptionTranslationPostProcessor() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "mongoEventsTemplate")
    public MongoTemplate mongoEventsTemplate() {
        val mongo = casProperties.getEvents().getMongo();
        val factory = new MongoDbConnectionFactory(sslContext.getObject());
        val mongoTemplate = factory.buildMongoTemplate(mongo);
        factory.createCollection(mongoTemplate, mongo.getCollection(), mongo.isDropCollection());
        return mongoTemplate;
    }

    @ConditionalOnMissingBean(name = "mongoEventRepositoryFilter")
    @Bean
    public CasEventRepositoryFilter mongoEventRepositoryFilter() {
        return CasEventRepositoryFilter.noOp();
    }

    @Bean
    public CasEventRepository casEventRepository() {
        val mongo = casProperties.getEvents().getMongo();
        return new MongoDbCasEventRepository(
            mongoEventRepositoryFilter(),
            mongoEventsTemplate(),
            mongo.getCollection());
    }
}
