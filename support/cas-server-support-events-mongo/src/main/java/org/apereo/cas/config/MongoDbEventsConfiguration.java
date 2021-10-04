package org.apereo.cas.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.CasEventRepositoryFilter;
import org.apereo.cas.support.events.mongo.MongoDbCasEventRepository;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * This is {@link MongoDbEventsConfiguration}, defines certain beans via configuration
 * while delegating some to Spring namespaces inside the context config file.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "mongoDbEventsConfiguration", proxyBeanMethods = false)
public class MongoDbEventsConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public PersistenceExceptionTranslationPostProcessor persistenceExceptionTranslationPostProcessor() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "mongoEventsTemplate")
    @Autowired
    public MongoTemplate mongoEventsTemplate(final CasConfigurationProperties casProperties,
                                             @Qualifier("casSslContext")
                                             final CasSSLContext casSslContext) {
        val mongo = casProperties.getEvents().getMongo();
        val factory = new MongoDbConnectionFactory(casSslContext.getSslContext());
        val mongoTemplate = factory.buildMongoTemplate(mongo);
        MongoDbConnectionFactory.createCollection(mongoTemplate, mongo.getCollection(), mongo.isDropCollection());
        return mongoTemplate;
    }

    @ConditionalOnMissingBean(name = "mongoEventRepositoryFilter")
    @Bean
    public CasEventRepositoryFilter mongoEventRepositoryFilter() {
        return CasEventRepositoryFilter.noOp();
    }

    @Bean
    @Autowired
    public CasEventRepository casEventRepository(final CasConfigurationProperties casProperties,
                                                 @Qualifier("mongoEventRepositoryFilter")
                                                 final CasEventRepositoryFilter mongoEventRepositoryFilter,
                                                 @Qualifier("mongoEventsTemplate")
                                                 final MongoTemplate mongoEventsTemplate) {
        val mongo = casProperties.getEvents().getMongo();
        return new MongoDbCasEventRepository(mongoEventRepositoryFilter, mongoEventsTemplate, mongo.getCollection());
    }
}
