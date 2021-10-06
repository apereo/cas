package org.apereo.cas.config;

import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.aup.CouchDbAcceptableUsagePolicyRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.core.ProfileCouchDbRepository;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;

import lombok.val;
import org.ektorp.impl.ObjectMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasAcceptableUsagePolicyCouchDbConfiguration} that stores AUP decisions in a CouchDb database.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Configuration(value = "casAcceptableUsagePolicyCoucbDbConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.acceptable-usage-policy.core", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CasAcceptableUsagePolicyCouchDbConfiguration {

    @ConditionalOnMissingBean(name = "aupCouchDbFactory")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public CouchDbConnectorFactory aupCouchDbFactory(final CasConfigurationProperties casProperties,
                                                     @Qualifier("defaultObjectMapperFactory")
                                                     final ObjectMapperFactory objectMapperFactory) {
        return new CouchDbConnectorFactory(casProperties.getAcceptableUsagePolicy().getCouchDb(), objectMapperFactory);
    }

    @ConditionalOnMissingBean(name = "aupCouchDbRepository")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public ProfileCouchDbRepository aupCouchDbRepository(
        @Qualifier("aupCouchDbFactory")
        final CouchDbConnectorFactory aupCouchDbFactory, final CasConfigurationProperties casProperties) {
        val couchDb = casProperties.getAcceptableUsagePolicy().getCouchDb();
        return new ProfileCouchDbRepository(aupCouchDbFactory.getCouchDbConnector(), couchDb.isCreateIfNotExists());
    }

    @ConditionalOnMissingBean(name = "couchDbAcceptableUsagePolicyRepository")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public AcceptableUsagePolicyRepository acceptableUsagePolicyRepository(
        @Qualifier("aupCouchDbRepository")
        final ProfileCouchDbRepository profileCouchDbRepository, final CasConfigurationProperties casProperties,
        @Qualifier(TicketRegistrySupport.BEAN_NAME)
        final TicketRegistrySupport ticketRegistrySupport) {
        return new CouchDbAcceptableUsagePolicyRepository(ticketRegistrySupport, casProperties.getAcceptableUsagePolicy(), profileCouchDbRepository,
            casProperties.getAcceptableUsagePolicy().getCouchDb().getRetries());
    }
}
