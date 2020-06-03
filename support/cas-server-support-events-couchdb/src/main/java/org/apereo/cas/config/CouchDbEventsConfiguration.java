package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.events.EventCouchDbRepository;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.CasEventRepositoryFilter;
import org.apereo.cas.support.events.CouchDbCasEventRepository;

import lombok.val;
import org.ektorp.impl.ObjectMapperFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CouchDbEventsConfiguration}, defines certain beans via configuration
 * while delegating some to Spring namespaces inside the context config file.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Configuration(value = "couchDbEventsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CouchDbEventsConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("defaultObjectMapperFactory")
    private ObjectProvider<ObjectMapperFactory> objectMapperFactory;

    @ConditionalOnMissingBean(name = "couchDbEventRepository")
    @Bean
    @RefreshScope
    public EventCouchDbRepository couchDbEventRepository(@Qualifier("eventCouchDbFactory") final CouchDbConnectorFactory eventCouchDbFactory) {
        val repository = new EventCouchDbRepository(eventCouchDbFactory.getCouchDbConnector(), casProperties.getEvents().getCouchDb().isCreateIfNotExists());
        repository.initStandardDesignDocument();
        return repository;
    }

    @ConditionalOnMissingBean(name = "eventCouchDbFactory")
    @Bean
    @RefreshScope
    public CouchDbConnectorFactory eventCouchDbFactory() {
        return new CouchDbConnectorFactory(casProperties.getEvents().getCouchDb(), objectMapperFactory.getObject());
    }

    @ConditionalOnMissingBean(name = "couchDbCasEventRepository")
    @Bean
    @RefreshScope
    public CasEventRepository casEventRepository(@Qualifier("couchDbEventRepository") final EventCouchDbRepository eventCouchDbRepository) {
        return new CouchDbCasEventRepository(couchDbEventRepositoryFilter(), eventCouchDbRepository, casProperties.getEvents().getCouchDb().isAsynchronous());
    }

    @ConditionalOnMissingBean(name = "couchDbEventRepositoryFilter")
    @Bean
    public CasEventRepositoryFilter couchDbEventRepositoryFilter() {
        return CasEventRepositoryFilter.noOp();
    }
}
