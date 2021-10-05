package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
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

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
@ConditionalOnProperty(prefix = "cas.authn.saml-idp.metadata.couch-db", name = "idp-metadata-enabled", havingValue = "true")
@Configuration(value = "ouchDbSamlIdPMetadataConfiguration", proxyBeanMethods = false)
public class CouchDbSamlIdPMetadataConfiguration {

    @ConditionalOnMissingBean(name = "samlIdPMetadataCouchDbInstance")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public CouchDbInstance samlIdPMetadataCouchDbInstance(
        @Qualifier("samlMetadataCouchDbFactory")
        final CouchDbConnectorFactory samlMetadataCouchDbFactory) {
        return samlMetadataCouchDbFactory.getCouchDbInstance();
    }

    @ConditionalOnMissingBean(name = "samlIdPMetadataCouchDbConnector")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public CouchDbConnector samlIdPMetadataCouchDbConnector(
        @Qualifier("samlMetadataCouchDbFactory")
        final CouchDbConnectorFactory samlMetadataCouchDbFactory) {
        return samlMetadataCouchDbFactory.getCouchDbConnector();
    }

    @ConditionalOnMissingBean(name = "samlIdPMetadataCouchDbRepository")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public SamlIdPMetadataCouchDbRepository samlIdPMetadataCouchDbRepository(
        final CasConfigurationProperties casProperties,
        @Qualifier("samlMetadataCouchDbFactory")
        final CouchDbConnectorFactory samlMetadataCouchDbFactory) {
        val repository = new SamlIdPMetadataCouchDbRepository(samlMetadataCouchDbFactory.getCouchDbConnector(),
            casProperties.getAuthn().getSamlIdp().getMetadata().getCouchDb().isCreateIfNotExists());
        repository.initStandardDesignDocument();
        return repository;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public CipherExecutor samlIdPMetadataGeneratorCipherExecutor(final CasConfigurationProperties casProperties) {
        val idp = casProperties.getAuthn().getSamlIdp();
        val crypto = idp.getMetadata().getCouchDb().getCrypto();
        if (crypto.isEnabled()) {
            return CipherExecutorUtils.newStringCipherExecutor(crypto, CouchDbSamlIdPMetadataCipherExecutor.class);
        }
        LOGGER.info("CouchDb SAML IdP metadata encryption/signing is turned off and "
                    + "MAY NOT be safe in a production environment. "
                    + "Consider using other choices to handle encryption, signing and verification of metadata artifacts");
        return CipherExecutor.noOp();
    }

    @ConditionalOnMissingBean(name = "couchDbSamlIdPMetadataGenerator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SamlIdPMetadataGenerator samlIdPMetadataGenerator(
        @Qualifier("samlIdPMetadataGeneratorConfigurationContext")
        final SamlIdPMetadataGeneratorConfigurationContext samlIdPMetadataGeneratorConfigurationContext,
        @Qualifier("samlIdPMetadataCouchDbRepository")
        final SamlIdPMetadataCouchDbRepository samlIdPMetadataRepository) {
        val generator = new CouchDbSamlIdPMetadataGenerator(samlIdPMetadataGeneratorConfigurationContext, samlIdPMetadataRepository);
        generator.generate(Optional.empty());
        return generator;
    }

    @ConditionalOnMissingBean(name = "couchDbSamlIdPMetadataLocator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SamlIdPMetadataLocator samlIdPMetadataLocator(
        @Qualifier("samlIdPMetadataCache")
        final Cache<String, SamlIdPMetadataDocument> samlIdPMetadataCache,
        @Qualifier("samlIdPMetadataGeneratorCipherExecutor")
        final CipherExecutor samlIdPMetadataGeneratorCipherExecutor,
        @Qualifier("samlIdPMetadataCouchDbRepository")
        final SamlIdPMetadataCouchDbRepository samlIdPMetadataRepository) {
        return new CouchDbSamlIdPMetadataLocator(samlIdPMetadataGeneratorCipherExecutor,
            samlIdPMetadataCache, samlIdPMetadataRepository);
    }
}
