package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.saml.DefaultSamlIdPMetadataCouchDbRepository;
import org.apereo.cas.couchdb.saml.SamlIdPMetadataCouchDbRepository;
import org.apereo.cas.support.saml.idp.metadata.CouchDbSamlIdPMetadataCipherExecutor;
import org.apereo.cas.support.saml.idp.metadata.CouchDbSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.CouchDbSamlIdPMetadataLocator;
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
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.Optional;

/**
 * This is {@link CouchDbSamlIdPMetadataConfiguration}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SAMLIdentityProviderMetadata, module = "couchdb")
@AutoConfiguration
public class CouchDbSamlIdPMetadataConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.saml-idp.metadata.couch-db.idp-metadata-enabled").isTrue();

    @ConditionalOnMissingBean(name = "samlIdPMetadataCouchDbInstance")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public CouchDbInstance samlIdPMetadataCouchDbInstance(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("samlMetadataCouchDbFactory")
        final CouchDbConnectorFactory samlMetadataCouchDbFactory) {
        return BeanSupplier.of(CouchDbInstance.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(samlMetadataCouchDbFactory::getCouchDbInstance)
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "samlIdPMetadataCouchDbConnector")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public CouchDbConnector samlIdPMetadataCouchDbConnector(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("samlMetadataCouchDbFactory")
        final CouchDbConnectorFactory samlMetadataCouchDbFactory) {
        return BeanSupplier.of(CouchDbConnector.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(samlMetadataCouchDbFactory::getCouchDbConnector)
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "samlIdPMetadataCouchDbRepository")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SamlIdPMetadataCouchDbRepository samlIdPMetadataCouchDbRepository(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("samlMetadataCouchDbFactory")
        final CouchDbConnectorFactory samlMetadataCouchDbFactory) {
        return BeanSupplier.of(SamlIdPMetadataCouchDbRepository.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val repository = new DefaultSamlIdPMetadataCouchDbRepository(samlMetadataCouchDbFactory.getCouchDbConnector(),
                    casProperties.getAuthn().getSamlIdp().getMetadata().getCouchDb().isCreateIfNotExists());
                repository.initStandardDesignDocument();
                return repository;
            })
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CipherExecutor samlIdPMetadataGeneratorCipherExecutor(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(CipherExecutor.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val idp = casProperties.getAuthn().getSamlIdp();
                val crypto = idp.getMetadata().getCouchDb().getCrypto();
                if (crypto.isEnabled()) {
                    return CipherExecutorUtils.newStringCipherExecutor(crypto, CouchDbSamlIdPMetadataCipherExecutor.class);
                }
                LOGGER.info("CouchDb SAML IdP metadata encryption/signing is turned off and "
                            + "MAY NOT be safe in a production environment. "
                            + "Consider using other choices to handle encryption, signing and verification of metadata artifacts");
                return CipherExecutor.noOp();
            })
            .otherwise(CipherExecutor::noOp)
            .get();
    }

    @ConditionalOnMissingBean(name = "couchDbSamlIdPMetadataGenerator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SamlIdPMetadataGenerator samlIdPMetadataGenerator(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("samlIdPMetadataGeneratorConfigurationContext")
        final SamlIdPMetadataGeneratorConfigurationContext samlIdPMetadataGeneratorConfigurationContext,
        @Qualifier("samlIdPMetadataCouchDbRepository")
        final SamlIdPMetadataCouchDbRepository samlIdPMetadataRepository) throws Exception {
        return BeanSupplier.of(SamlIdPMetadataGenerator.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(Unchecked.supplier(() -> {
                val generator = new CouchDbSamlIdPMetadataGenerator(samlIdPMetadataGeneratorConfigurationContext, samlIdPMetadataRepository);
                generator.generate(Optional.empty());
                return generator;
            }))
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "couchDbSamlIdPMetadataLocator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SamlIdPMetadataLocator samlIdPMetadataLocator(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("samlIdPMetadataCache")
        final Cache<String, SamlIdPMetadataDocument> samlIdPMetadataCache,
        @Qualifier("samlIdPMetadataGeneratorCipherExecutor")
        final CipherExecutor samlIdPMetadataGeneratorCipherExecutor,
        @Qualifier("samlIdPMetadataCouchDbRepository")
        final SamlIdPMetadataCouchDbRepository samlIdPMetadataRepository) {
        return BeanSupplier.of(SamlIdPMetadataLocator.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> new CouchDbSamlIdPMetadataLocator(samlIdPMetadataGeneratorCipherExecutor,
                samlIdPMetadataCache, samlIdPMetadataRepository))
            .otherwiseProxy()
            .get();
    }
}
