package org.apereo.cas.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.support.saml.idp.metadata.MongoDbSamlIdPMetadataCipherExecutor;
import org.apereo.cas.support.saml.idp.metadata.MongoDbSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.MongoDbSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGeneratorConfigurationContext;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
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
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SAMLIdentityProviderMetadata, module = "mongo")
@Configuration(value = "SamlIdPMongoDbIdPMetadataConfiguration", proxyBeanMethods = false)
class SamlIdPMongoDbIdPMetadataConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.saml-idp.metadata.mongo.idp-metadata-collection");

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CipherExecutor samlIdPMetadataGeneratorCipherExecutor(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(CipherExecutor.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val idp = casProperties.getAuthn().getSamlIdp();
                val crypto = idp.getMetadata().getMongo().getCrypto();
                if (crypto.isEnabled()) {
                    return CipherExecutorUtils.newStringCipherExecutor(crypto, MongoDbSamlIdPMetadataCipherExecutor.class);
                }
                LOGGER.info("MongoDb SAML IdP metadata encryption/signing is turned off and "
                            + "MAY NOT be safe in a production environment. "
                            + "Consider using other choices to handle encryption, signing and verification of metadata artifacts");
                return CipherExecutor.noOp();
            })
            .otherwise(CipherExecutor::noOp)
            .get();
    }

    @ConditionalOnMissingBean(name = "mongoDbSamlIdPMetadataTemplate")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MongoOperations mongoDbSamlIdPMetadataTemplate(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier(CasSSLContext.BEAN_NAME)
        final CasSSLContext casSslContext) {
        return BeanSupplier.of(MongoOperations.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val idp = casProperties.getAuthn().getSamlIdp();
                val mongo = idp.getMetadata().getMongo();
                val factory = new MongoDbConnectionFactory(casSslContext.getSslContext());
                val mongoTemplate = factory.buildMongoTemplate(mongo);
                MongoDbConnectionFactory.createCollection(mongoTemplate, mongo.getIdpMetadataCollection(), mongo.isDropCollection());
                return mongoTemplate;
            })
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SamlIdPMetadataGenerator samlIdPMetadataGenerator(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("mongoDbSamlIdPMetadataTemplate")
        final MongoOperations mongoDbSamlIdPMetadataTemplate,
        @Qualifier("samlIdPMetadataGeneratorConfigurationContext")
        final SamlIdPMetadataGeneratorConfigurationContext samlIdPMetadataGeneratorConfigurationContext) {
        return BeanSupplier.of(SamlIdPMetadataGenerator.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val idp = casProperties.getAuthn().getSamlIdp();
                return new MongoDbSamlIdPMetadataGenerator(samlIdPMetadataGeneratorConfigurationContext,
                    mongoDbSamlIdPMetadataTemplate, idp.getMetadata().getMongo().getIdpMetadataCollection());
            })
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SamlIdPMetadataLocator samlIdPMetadataLocator(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("samlIdPMetadataCache")
        final Cache<@NonNull String, SamlIdPMetadataDocument> samlIdPMetadataCache,
        @Qualifier("samlIdPMetadataGeneratorCipherExecutor")
        final CipherExecutor samlIdPMetadataGeneratorCipherExecutor,
        @Qualifier("mongoDbSamlIdPMetadataTemplate")
        final MongoOperations mongoDbSamlIdPMetadataTemplate) {
        return BeanSupplier.of(SamlIdPMetadataLocator.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val idp = casProperties.getAuthn().getSamlIdp();
                return new MongoDbSamlIdPMetadataLocator(samlIdPMetadataGeneratorCipherExecutor,
                    samlIdPMetadataCache, mongoDbSamlIdPMetadataTemplate,
                    applicationContext,
                    idp.getMetadata().getMongo().getIdpMetadataCollection());
            })
            .otherwiseProxy()
            .get();
    }
}
