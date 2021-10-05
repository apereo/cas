package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.events.EventCouchDbRepository;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.CasEventRepositoryFilter;
import org.apereo.cas.support.events.CouchDbCasEventRepository;

import lombok.val;
import org.ektorp.impl.ObjectMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CouchDbEventsConfiguration}, defines certain beans via configuration
 * while delegating some to Spring namespaces inside the context config file.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Configuration(value = "couchDbEventsConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CouchDbEventsConfiguration {

    @ConditionalOnMissingBean(name = "couchDbEventRepository")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public EventCouchDbRepository couchDbEventRepository(
        @Qualifier("eventCouchDbFactory")
        final CouchDbConnectorFactory eventCouchDbFactory, final CasConfigurationProperties casProperties) {
        val repository = new EventCouchDbRepository(eventCouchDbFactory.getCouchDbConnector(), casProperties.getEvents().getCouchDb().isCreateIfNotExists());
        repository.initStandardDesignDocument();
        return repository;
    }

    @ConditionalOnMissingBean(name = "eventCouchDbFactory")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public CouchDbConnectorFactory eventCouchDbFactory(final CasConfigurationProperties casProperties,
                                                       @Qualifier("defaultObjectMapperFactory")
                                                       final ObjectMapperFactory objectMapperFactory) {
        return new CouchDbConnectorFactory(casProperties.getEvents().getCouchDb(), objectMapperFactory);
    }

    @ConditionalOnMissingBean(name = "couchDbCasEventRepository")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public CasEventRepository casEventRepository(
        @Qualifier("couchDbEventRepository")
        final EventCouchDbRepository eventCouchDbRepository,
        @Qualifier("couchDbEventRepositoryFilter")
        final CasEventRepositoryFilter couchDbEventRepositoryFilter, final CasConfigurationProperties casProperties) {
        return new CouchDbCasEventRepository(couchDbEventRepositoryFilter, eventCouchDbRepository, casProperties.getEvents().getCouchDb().isAsynchronous());
    }

    @ConditionalOnMissingBean(name = "couchDbEventRepositoryFilter")
    @Bean
    public CasEventRepositoryFilter couchDbEventRepositoryFilter() {
        return CasEventRepositoryFilter.noOp();
    }
}
