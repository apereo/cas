package org.apereo.cas.config;

import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.aup.CouchDbAcceptableUsagePolicyRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.core.DefaultCouchDbConnectorFactory;
import org.apereo.cas.couchdb.core.DefaultProfileCouchDbRepository;
import org.apereo.cas.couchdb.core.ProfileCouchDbRepository;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.val;
import org.ektorp.impl.ObjectMapperFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasAcceptableUsagePolicyCouchDbConfiguration} that stores AUP decisions in a CouchDb database.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AcceptableUsagePolicy, module = "couchdb")
@AutoConfiguration
public class CasAcceptableUsagePolicyCouchDbConfiguration {

    @ConditionalOnMissingBean(name = "aupCouchDbFactory")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CouchDbConnectorFactory aupCouchDbFactory(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("defaultObjectMapperFactory")
        final ObjectMapperFactory objectMapperFactory) throws Exception {
        return BeanSupplier.of(CouchDbConnectorFactory.class)
            .when(AcceptableUsagePolicyRepository.CONDITION_AUP_ENABLED.given(applicationContext.getEnvironment()))
            .supply(() -> new DefaultCouchDbConnectorFactory(casProperties.getAcceptableUsagePolicy().getCouchDb(), objectMapperFactory))
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "aupCouchDbRepository")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ProfileCouchDbRepository aupCouchDbRepository(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("aupCouchDbFactory")
        final CouchDbConnectorFactory aupCouchDbFactory,
        final CasConfigurationProperties casProperties) throws Exception {
        return BeanSupplier.of(ProfileCouchDbRepository.class)
            .when(AcceptableUsagePolicyRepository.CONDITION_AUP_ENABLED.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val couchDb = casProperties.getAcceptableUsagePolicy().getCouchDb();
                return new DefaultProfileCouchDbRepository(aupCouchDbFactory.getCouchDbConnector(), couchDb.isCreateIfNotExists());
            })
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "couchDbAcceptableUsagePolicyRepository")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AcceptableUsagePolicyRepository acceptableUsagePolicyRepository(
        @Qualifier("aupCouchDbRepository")
        final ProfileCouchDbRepository profileCouchDbRepository,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier(TicketRegistrySupport.BEAN_NAME)
        final TicketRegistrySupport ticketRegistrySupport) throws Exception {
        return BeanSupplier.of(AcceptableUsagePolicyRepository.class)
            .when(AcceptableUsagePolicyRepository.CONDITION_AUP_ENABLED.given(applicationContext.getEnvironment()))
            .supply(() -> new CouchDbAcceptableUsagePolicyRepository(ticketRegistrySupport,
                casProperties.getAcceptableUsagePolicy(), profileCouchDbRepository,
                casProperties.getAcceptableUsagePolicy().getCouchDb().getRetries()))
            .otherwise(AcceptableUsagePolicyRepository::noOp)
            .get();
    }
}
