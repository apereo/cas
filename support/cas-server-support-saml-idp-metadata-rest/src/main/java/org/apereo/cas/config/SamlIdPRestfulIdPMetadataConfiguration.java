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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SamlIdPRestfulIdPMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("SamlIdPRestfulIdPMetadataConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.authn.saml-idp.metadata.rest", name = "idp-metadata-enabled", havingValue = "true")
@Slf4j
public class SamlIdPRestfulIdPMetadataConfiguration {

    @Autowired
    @Qualifier("samlIdPMetadataGeneratorConfigurationContext")
    private ObjectProvider<SamlIdPMetadataGeneratorConfigurationContext> samlIdPMetadataGeneratorConfigurationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("samlIdPMetadataCache")
    private ObjectProvider<Cache<String, SamlIdPMetadataDocument>> samlIdPMetadataCache;

    @Bean
    @RefreshScope
    public CipherExecutor samlIdPMetadataGeneratorCipherExecutor() {
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
    @RefreshScope
    public SamlIdPMetadataGenerator samlIdPMetadataGenerator() {
        return new RestfulSamlIdPMetadataGenerator(samlIdPMetadataGeneratorConfigurationContext.getObject());
    }

    @Bean
    @RefreshScope
    public SamlIdPMetadataLocator samlIdPMetadataLocator() {
        val idp = casProperties.getAuthn().getSamlIdp();
        return new RestfulSamlIdPMetadataLocator(
            samlIdPMetadataGeneratorCipherExecutor(),
            samlIdPMetadataCache.getObject(),
            idp.getMetadata().getRest());
    }
}
