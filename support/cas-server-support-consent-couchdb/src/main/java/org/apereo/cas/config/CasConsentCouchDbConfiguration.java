package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.consent.CouchDbConsentRepository;
import org.apereo.cas.couchdb.consent.ConsentDecisionCouchDbRepository;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;

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
 * This is {@link CasConsentCouchDbConfiguration}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Configuration(value = "couchDbConsentConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasConsentCouchDbConfiguration {

    @ConditionalOnMissingBean(name = "consentCouchDbFactory")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Autowired
    public CouchDbConnectorFactory consentCouchDbFactory(final CasConfigurationProperties casProperties,
                                                         @Qualifier("defaultObjectMapperFactory")
                                                         final ObjectMapperFactory objectMapperFactory) {
        return new CouchDbConnectorFactory(casProperties.getConsent().getCouchDb(),
            objectMapperFactory);
    }

    @ConditionalOnMissingBean(name = "consentCouchDbRepository")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public ConsentDecisionCouchDbRepository consentCouchDbRepository(
        @Qualifier("consentCouchDbFactory")
        final CouchDbConnectorFactory consentCouchDbFactory, final CasConfigurationProperties casProperties) {
        val repository = new ConsentDecisionCouchDbRepository(consentCouchDbFactory.getCouchDbConnector(),
            casProperties.getConsent().getCouchDb().isCreateIfNotExists());
        repository.initStandardDesignDocument();
        return repository;
    }

    @ConditionalOnMissingBean(name = "couchDbConsentRepository")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public ConsentRepository consentRepository(
        @Qualifier("consentCouchDbRepository")
        final ConsentDecisionCouchDbRepository consentCouchDbRepository) {
        return new CouchDbConsentRepository(consentCouchDbRepository);
    }
}
