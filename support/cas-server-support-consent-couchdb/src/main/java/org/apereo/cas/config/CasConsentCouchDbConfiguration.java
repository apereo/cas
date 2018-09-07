package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.consent.CouchDbConsentRepository;
import org.apereo.cas.couchdb.ConsentDecisionCouchDbRepository;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;

import lombok.val;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.impl.ObjectMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasConsentCouchDbConfiguration}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Configuration("couchDbConsentConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasConsentCouchDbConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("consentCouchDbFactory")
    private CouchDbConnectorFactory consentCouchDbFactory;

    @Autowired
    @Qualifier("couchDbObjectMapperFactory")
    private ObjectMapperFactory objectMapperFactory;

    @ConditionalOnMissingBean(name = "consentCouchDbFactory")
    @RefreshScope
    @Bean
    public CouchDbConnectorFactory consentCouchDbFactory() {
        return new CouchDbConnectorFactory(casProperties.getConsent().getCouchDb(), objectMapperFactory);
    }

    @ConditionalOnMissingBean(name = "consentCouchDbInstance")
    @RefreshScope
    @Bean
    public CouchDbInstance consentCouchDbInstance() {
        return consentCouchDbFactory.createInstance();
    }

    @ConditionalOnMissingBean(name = "consentCouchDbConnector")
    @RefreshScope
    @Bean
    public CouchDbConnector consentCouchDbConnector() {
        return consentCouchDbFactory.createConnector();
    }

    @ConditionalOnMissingBean(name = "consentCouchDbRepository")
    @Bean
    @RefreshScope
    public ConsentDecisionCouchDbRepository consentCouchDbRepository() {
        val repository = new ConsentDecisionCouchDbRepository(consentCouchDbFactory.create(), casProperties.getConsent().getCouchDb().isCreateIfNotExists());
        repository.initStandardDesignDocument();
        return repository;
    }

    @ConditionalOnMissingBean(name = "couchDbConsentRepository")
    @RefreshScope
    @Bean
    public ConsentRepository consentRepository() {
        return new CouchDbConsentRepository(consentCouchDbRepository());
    }
}
