package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.saml.SamlMetadataDocumentCouchDbRepository;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.metadata.resolver.CouchDbSamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.plan.SamlRegisteredServiceMetadataResolutionPlan;
import org.apereo.cas.support.saml.services.idp.metadata.plan.SamlRegisteredServiceMetadataResolutionPlanConfigurator;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SamlIdPCouchDbMetadataConfiguration}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Configuration("samlIdPCouchDbMetadataConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class SamlIdPCouchDbMetadataConfiguration implements SamlRegisteredServiceMetadataResolutionPlanConfigurator {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("shibboleth.OpenSAMLConfig")
    private OpenSamlConfigBean openSamlConfigBean;

    @Autowired
    @Qualifier("samlMetadataCouchDbFactory")
    private CouchDbConnectorFactory samlMetadataCouchDbFactory;

    @ConditionalOnMissingBean(name = "samlMetadataDocumentCouchDbRepository")
    @Bean
    @RefreshScope
    public SamlMetadataDocumentCouchDbRepository samlMetadataDocumentCouchDbRepository() {
        val couch = casProperties.getAuthn().getSamlIdp().getMetadata().getCouchDb();
        val repository = new SamlMetadataDocumentCouchDbRepository(samlMetadataCouchDbFactory.getCouchDbConnector(), couch.isCreateIfNotExists());
        repository.initStandardDesignDocument();
        return repository;
    }

    @ConditionalOnMissingBean(name = "couchDbSamlRegisteredServiceMetadataResolver")
    @Bean
    @RefreshScope
    public SamlRegisteredServiceMetadataResolver couchDbSamlRegisteredServiceMetadataResolver() {
        val idp = casProperties.getAuthn().getSamlIdp();
        return new CouchDbSamlRegisteredServiceMetadataResolver(idp, openSamlConfigBean, samlMetadataDocumentCouchDbRepository());
    }

    @Override
    public void configureMetadataResolutionPlan(final SamlRegisteredServiceMetadataResolutionPlan plan) {
        plan.registerMetadataResolver(couchDbSamlRegisteredServiceMetadataResolver());
    }
}
