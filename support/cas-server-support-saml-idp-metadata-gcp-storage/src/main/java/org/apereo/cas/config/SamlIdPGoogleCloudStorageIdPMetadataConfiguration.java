package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.support.saml.idp.metadata.GoogleCloudStorageSamlIdPMetadataCipherExecutor;
import org.apereo.cas.support.saml.idp.metadata.GoogleCloudStorageSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.GoogleCloudStorageSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGeneratorConfigurationContext;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import com.github.benmanes.caffeine.cache.Cache;
import com.google.cloud.storage.Storage;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link SamlIdPGoogleCloudStorageIdPMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SAMLIdentityProviderMetadata, module = "gcp")
@Configuration(value = "SamlIdPGoogleCloudStorageIdPMetadataConfiguration", proxyBeanMethods = false)
class SamlIdPGoogleCloudStorageIdPMetadataConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CipherExecutor samlIdPMetadataGeneratorCipherExecutor(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val idp = casProperties.getAuthn().getSamlIdp();
        val crypto = idp.getMetadata().getGcp().getCrypto();
        return crypto.isEnabled()
            ? CipherExecutorUtils.newStringCipherExecutor(crypto, GoogleCloudStorageSamlIdPMetadataCipherExecutor.class)
            : CipherExecutor.noOp();
    }
    
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SamlIdPMetadataGenerator samlIdPMetadataGenerator(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("storage") final Storage storage,
        @Qualifier(SamlIdPMetadataGeneratorConfigurationContext.DEFAULT_BEAN_NAME)
        final SamlIdPMetadataGeneratorConfigurationContext samlIdPMetadataGeneratorConfigurationContext) {
        return new GoogleCloudStorageSamlIdPMetadataGenerator(samlIdPMetadataGeneratorConfigurationContext, storage);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SamlIdPMetadataLocator samlIdPMetadataLocator(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("samlIdPMetadataCache")
        final Cache<String, SamlIdPMetadataDocument> samlIdPMetadataCache,
        @Qualifier("samlIdPMetadataGeneratorCipherExecutor")
        final CipherExecutor samlIdPMetadataGeneratorCipherExecutor,
        @Qualifier("storage") final Storage storage) {

        return new GoogleCloudStorageSamlIdPMetadataLocator(samlIdPMetadataGeneratorCipherExecutor,
            samlIdPMetadataCache, storage, applicationContext);
    }
}
