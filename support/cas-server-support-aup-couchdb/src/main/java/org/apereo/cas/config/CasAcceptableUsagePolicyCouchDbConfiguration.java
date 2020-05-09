package org.apereo.cas.config;

import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.aup.CouchDbAcceptableUsagePolicyRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.core.ProfileCouchDbRepository;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;

import lombok.val;
import org.ektorp.impl.ObjectMapperFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasAcceptableUsagePolicyCouchDbConfiguration} that stores AUP decisions in a CouchDb database.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Configuration(value = "casAcceptableUsagePolicyCoucbDbConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.acceptable-usage-policy", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CasAcceptableUsagePolicyCouchDbConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private ObjectProvider<TicketRegistrySupport> ticketRegistrySupport;

    @Autowired
    @Qualifier("defaultObjectMapperFactory")
    private ObjectProvider<ObjectMapperFactory> objectMapperFactory;

    @ConditionalOnMissingBean(name = "aupCouchDbFactory")
    @Bean
    @RefreshScope
    public CouchDbConnectorFactory aupCouchDbFactory() {
        return new CouchDbConnectorFactory(casProperties.getAcceptableUsagePolicy().getCouchDb(), objectMapperFactory.getObject());
    }

    @ConditionalOnMissingBean(name = "aupCouchDbRepository")
    @Bean
    @RefreshScope
    public ProfileCouchDbRepository aupCouchDbRepository(@Qualifier("aupCouchDbFactory") final CouchDbConnectorFactory aupCouchDbFactory) {
        val couchDb = casProperties.getAcceptableUsagePolicy().getCouchDb();
        val repository = new ProfileCouchDbRepository(aupCouchDbFactory.getCouchDbConnector(), couchDb.isCreateIfNotExists());
        repository.initStandardDesignDocument();
        return repository;
    }

    @ConditionalOnMissingBean(name = "couchDbAcceptableUsagePolicyRepository")
    @Bean
    @RefreshScope
    public AcceptableUsagePolicyRepository acceptableUsagePolicyRepository(
        @Qualifier("aupCouchDbRepository") final ProfileCouchDbRepository profileCouchDbRepository) {
        return new CouchDbAcceptableUsagePolicyRepository(ticketRegistrySupport.getObject(),
            casProperties.getAcceptableUsagePolicy(),
            profileCouchDbRepository,
            casProperties.getAcceptableUsagePolicy().getCouchDb().getRetries());
    }
}
