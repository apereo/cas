package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.idp.metadata.RestfulSamlIdPMetadataCipherExecutor;
import org.apereo.cas.support.saml.idp.metadata.RestfulSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.RestfulSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGeneratorConfigurationContext;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link SamlIdPRestfulIdPMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.authn.saml-idp.metadata.rest", name = "idp-metadata-enabled", havingValue = "true")
@Slf4j
@Configuration(value = "SamlIdPRestfulIdPMetadataConfiguration", proxyBeanMethods = false)
public class SamlIdPRestfulIdPMetadataConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public CipherExecutor samlIdPMetadataGeneratorCipherExecutor(final CasConfigurationProperties casProperties) {
        val idp = casProperties.getAuthn().getSamlIdp();
        val crypto = idp.getMetadata().getRest().getCrypto();
        if (crypto.isEnabled()) {
            return CipherExecutorUtils.newStringCipherExecutor(crypto, RestfulSamlIdPMetadataCipherExecutor.class);
        }
        LOGGER.info("Restful SAML IdP metadata encryption/signing is turned off and "
                    + "MAY NOT be safe in a production environment. "
                    + "Consider using other choices to handle encryption, signing and verification of "
                    + "metadata artifacts");
        return CipherExecutor.noOp();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SamlIdPMetadataGenerator samlIdPMetadataGenerator(
        @Qualifier("samlIdPMetadataGeneratorConfigurationContext")
        final SamlIdPMetadataGeneratorConfigurationContext samlIdPMetadataGeneratorConfigurationContext) {
        return new RestfulSamlIdPMetadataGenerator(samlIdPMetadataGeneratorConfigurationContext);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public SamlIdPMetadataLocator samlIdPMetadataLocator(
        @Qualifier("samlIdPMetadataCache")
        final Cache<String, SamlIdPMetadataDocument> samlIdPMetadataCache,
        final CasConfigurationProperties casProperties,
        @Qualifier("samlIdPMetadataGeneratorCipherExecutor")
        final CipherExecutor samlIdPMetadataGeneratorCipherExecutor) {
        val idp = casProperties.getAuthn().getSamlIdp();
        return new RestfulSamlIdPMetadataLocator(samlIdPMetadataGeneratorCipherExecutor,
            samlIdPMetadataCache, idp.getMetadata().getRest());
    }
}
