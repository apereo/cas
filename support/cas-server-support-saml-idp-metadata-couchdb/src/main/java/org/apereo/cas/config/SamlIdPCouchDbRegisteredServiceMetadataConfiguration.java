package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.saml.SamlMetadataDocumentCouchDbRepository;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.metadata.resolver.CouchDbSamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.plan.SamlRegisteredServiceMetadataResolutionPlanConfigurer;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SamlIdPCouchDbRegisteredServiceMetadataConfiguration}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Configuration("samlIdPCouchDbRegisteredServiceMetadataConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SamlIdPCouchDbRegisteredServiceMetadataConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("shibboleth.OpenSAMLConfig")
    private ObjectProvider<OpenSamlConfigBean> openSamlConfigBean;

    @Autowired
    @Qualifier("samlMetadataCouchDbFactory")
    private ObjectProvider<CouchDbConnectorFactory> samlMetadataCouchDbFactory;

    @ConditionalOnMissingBean(name = "samlMetadataDocumentCouchDbRepository")
    @Bean
    @RefreshScope
    public SamlMetadataDocumentCouchDbRepository samlMetadataDocumentCouchDbRepository() {
        val couch = casProperties.getAuthn().getSamlIdp().getMetadata().getCouchDb();
        val repository = new SamlMetadataDocumentCouchDbRepository(samlMetadataCouchDbFactory.getObject().getCouchDbConnector(), couch.isCreateIfNotExists());
        repository.initStandardDesignDocument();
        return repository;
    }

    @ConditionalOnMissingBean(name = "couchDbSamlRegisteredServiceMetadataResolver")
    @Bean
    @RefreshScope
    public SamlRegisteredServiceMetadataResolver couchDbSamlRegisteredServiceMetadataResolver() {
        val idp = casProperties.getAuthn().getSamlIdp();
        return new CouchDbSamlRegisteredServiceMetadataResolver(idp, openSamlConfigBean.getObject(), samlMetadataDocumentCouchDbRepository());
    }

    @Bean
    @ConditionalOnMissingBean(name = "couchDbSamlRegisteredServiceMetadataResolutionPlanConfigurer")
    public SamlRegisteredServiceMetadataResolutionPlanConfigurer couchDbSamlRegisteredServiceMetadataResolutionPlanConfigurer() {
        return plan -> plan.registerMetadataResolver(couchDbSamlRegisteredServiceMetadataResolver());
    }
}
