package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.idp.metadata.RestfulSamlIdPMetadataCipherExecutor;
import org.apereo.cas.support.saml.idp.metadata.RestfulSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.RestfulSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGeneratorConfigurationContext;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.writer.SamlIdPCertificateAndKeyWriter;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

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
    private ResourceLoader resourceLoader;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("samlSelfSignedCertificateWriter")
    private ObjectProvider<SamlIdPCertificateAndKeyWriter> samlSelfSignedCertificateWriter;

    @Bean
    @ConditionalOnMissingBean(name = "restfulSamlIdPMetadataCipherExecutor")
    public CipherExecutor restfulSamlIdPMetadataCipherExecutor() {
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
    @SneakyThrows
    public SamlIdPMetadataGenerator samlIdPMetadataGenerator() {
        val context = SamlIdPMetadataGeneratorConfigurationContext.builder()
            .samlIdPMetadataLocator(samlIdPMetadataLocator())
            .samlIdPCertificateAndKeyWriter(samlSelfSignedCertificateWriter.getObject())
            .resourceLoader(resourceLoader)
            .casProperties(casProperties)
            .metadataCipherExecutor(restfulSamlIdPMetadataCipherExecutor())
            .build();
        return new RestfulSamlIdPMetadataGenerator(context);
    }

    @Bean
    @SneakyThrows
    public SamlIdPMetadataLocator samlIdPMetadataLocator() {
        val idp = casProperties.getAuthn().getSamlIdp();
        return new RestfulSamlIdPMetadataLocator(
            restfulSamlIdPMetadataCipherExecutor(),
            idp.getMetadata().getRest());
    }
}
