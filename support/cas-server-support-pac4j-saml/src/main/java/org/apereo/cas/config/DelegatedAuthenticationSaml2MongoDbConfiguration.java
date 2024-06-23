package org.apereo.cas.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.mongo.CasMongoOperations;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientFactoryCustomizer;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.metadata.mongo.SAML2MongoMetadataGenerator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link DelegatedAuthenticationSaml2MongoDbConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */

@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.DelegatedAuthentication, module = "saml-mongodb", enabledByDefault = false)
@Configuration(value = "DelegatedAuthenticationSaml2MongoDbConfiguration", proxyBeanMethods = false)
@ConditionalOnClass(CasMongoOperations.class)
class DelegatedAuthenticationSaml2MongoDbConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "delegatedSaml2ClientMongoDbMetadataCustomizer")
    public DelegatedClientFactoryCustomizer delegatedSaml2ClientMongoDbMetadataCustomizer(
        final CasConfigurationProperties casProperties,
        @Qualifier(CasSSLContext.BEAN_NAME) final CasSSLContext casSslContext) {
        return client -> {
            if (client instanceof final SAML2Client saml2Client) {
                val configuration = saml2Client.getConfiguration();
                casProperties.getAuthn().getPac4j().getSaml()
                    .stream()
                    .map(saml -> saml.getMetadata().getServiceProvider().getMongo())
                    .filter(saml -> StringUtils.isNotBlank(saml.getCollection()))
                    .forEach(mongo -> {
                        val factory = new MongoDbConnectionFactory(casSslContext.getSslContext());
                        val mongoClient = factory.buildMongoDbClient(mongo);
                        val mongoTemplate = factory.buildMongoTemplate(mongoClient, mongo);
                        MongoDbConnectionFactory.createCollection(mongoTemplate, mongo.getCollection(), mongo.isDropCollection());
                        val metadataGenerator = new SAML2MongoMetadataGenerator(mongoClient, configuration.getServiceProviderEntityId());
                        metadataGenerator.setMetadataCollection(mongo.getCollection());
                        metadataGenerator.setMetadataDatabase(mongo.getDatabaseName());
                        configuration.setServiceProviderMetadataResource(ResourceUtils.NULL_RESOURCE);
                        configuration.setMetadataGenerator(metadataGenerator);
                    });
            }
        };
    }
}
