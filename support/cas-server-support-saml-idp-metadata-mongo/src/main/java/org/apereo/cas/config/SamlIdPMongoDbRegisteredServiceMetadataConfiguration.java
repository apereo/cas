package org.apereo.cas.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.metadata.resolver.MongoDbSamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.plan.SamlRegisteredServiceMetadataResolutionPlanConfigurer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.mongodb.core.MongoOperations;

/**
 * This is {@link SamlIdPMongoDbIdPMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SAMLServiceProviderMetadata, module = "mongo")
@Configuration(value = "SamlIdPMongoDbRegisteredServiceMetadataConfiguration", proxyBeanMethods = false)
class SamlIdPMongoDbRegisteredServiceMetadataConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "mongoDbSamlRegisteredServiceMetadataResolver")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SamlRegisteredServiceMetadataResolver mongoDbSamlRegisteredServiceMetadataResolver(
        final CasConfigurationProperties casProperties,
        @Qualifier("mongoDbSamlMetadataResolverTemplate")
        final MongoOperations mongoDbSamlMetadataResolverTemplate,
        @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
        final OpenSamlConfigBean openSamlConfigBean) {
        val idp = casProperties.getAuthn().getSamlIdp();
        return new MongoDbSamlRegisteredServiceMetadataResolver(idp, openSamlConfigBean, mongoDbSamlMetadataResolverTemplate);
    }

    @ConditionalOnMissingBean(name = "mongoDbSamlMetadataResolverTemplate")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MongoOperations mongoDbSamlMetadataResolverTemplate(
        final CasConfigurationProperties casProperties,
        @Qualifier(CasSSLContext.BEAN_NAME)
        final CasSSLContext casSslContext) {
        val mongo = casProperties.getAuthn().getSamlIdp().getMetadata().getMongo();
        val factory = new MongoDbConnectionFactory(casSslContext.getSslContext());
        val mongoTemplate = factory.buildMongoTemplate(mongo);
        MongoDbConnectionFactory.createCollection(mongoTemplate, mongo.getCollection(), mongo.isDropCollection());
        return mongoTemplate;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "mongoDbSamlRegisteredServiceMetadataResolutionPlanConfigurer")
    public SamlRegisteredServiceMetadataResolutionPlanConfigurer mongoDbSamlRegisteredServiceMetadataResolutionPlanConfigurer(
        @Qualifier("mongoDbSamlRegisteredServiceMetadataResolver")
        final SamlRegisteredServiceMetadataResolver mongoDbSamlRegisteredServiceMetadataResolver) {
        return plan -> plan.registerMetadataResolver(mongoDbSamlRegisteredServiceMetadataResolver);
    }
}
