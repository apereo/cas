package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.consent.CouchDbConsentRepository;
import org.apereo.cas.couchdb.consent.ConsentDecisionCouchDbRepository;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;

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
 * This is {@link CasConsentCouchDbConfiguration}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Configuration(value = "couchDbConsentConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasConsentCouchDbConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("defaultObjectMapperFactory")
    private ObjectProvider<ObjectMapperFactory> objectMapperFactory;

    @ConditionalOnMissingBean(name = "consentCouchDbFactory")
    @RefreshScope
    @Bean
    public CouchDbConnectorFactory consentCouchDbFactory() {
        return new CouchDbConnectorFactory(casProperties.getConsent().getCouchDb(), objectMapperFactory.getObject());
    }

    @ConditionalOnMissingBean(name = "consentCouchDbRepository")
    @Bean
    @RefreshScope
    public ConsentDecisionCouchDbRepository consentCouchDbRepository(@Qualifier("consentCouchDbFactory") final CouchDbConnectorFactory consentCouchDbFactory) {
        val repository = new ConsentDecisionCouchDbRepository(consentCouchDbFactory.getCouchDbConnector(),
            casProperties.getConsent().getCouchDb().isCreateIfNotExists());
        repository.initStandardDesignDocument();
        return repository;
    }

    @ConditionalOnMissingBean(name = "couchDbConsentRepository")
    @RefreshScope
    @Bean
    public ConsentRepository consentRepository(@Qualifier("consentCouchDbRepository") final ConsentDecisionCouchDbRepository consentCouchDbRepository) {
        return new CouchDbConsentRepository(consentCouchDbRepository);
    }
}
