package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.idp.metadata.AmazonS3SamlIdPMetadataCipherExecutor;
import org.apereo.cas.support.saml.idp.metadata.AmazonS3SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.AmazonS3SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGeneratorConfigurationContext;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.writer.SamlIdPCertificateAndKeyWriter;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.amazonaws.services.s3.AmazonS3;
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

import java.util.Optional;

/**
 * This is {@link AmazonS3SamlIdPMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration("amazonS3SamlIdPMetadataConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.authn.saml-idp.metadata.amazon-s3", name = "idp-metadata-bucket-name")
@Slf4j
public class AmazonS3SamlIdPMetadataConfiguration {
    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("samlSelfSignedCertificateWriter")
    private ObjectProvider<SamlIdPCertificateAndKeyWriter> samlSelfSignedCertificateWriter;

    @Autowired
    @Qualifier("amazonS3Client")
    private ObjectProvider<AmazonS3> amazonS3Client;

    @Bean
    @ConditionalOnMissingBean(name = "amazonS3SamlIdPMetadataCipherExecutor")
    public CipherExecutor amazonS3SamlIdPMetadataCipherExecutor() {
        val idp = casProperties.getAuthn().getSamlIdp();
        val crypto = idp.getMetadata().getAmazonS3().getCrypto();

        if (crypto.isEnabled()) {
            return CipherExecutorUtils.newStringCipherExecutor(crypto, AmazonS3SamlIdPMetadataCipherExecutor.class);
        }
        LOGGER.info("Amazon S3 SAML IdP metadata encryption/signing is turned off and "
            + "MAY NOT be safe in a production environment. "
            + "Consider using other choices to handle encryption, signing and verification of "
            + "metadata artifacts");
        return CipherExecutor.noOp();
    }

    @Bean
    @SneakyThrows
    public SamlIdPMetadataGenerator samlIdPMetadataGenerator() {
        val idp = casProperties.getAuthn().getSamlIdp();
        val context = SamlIdPMetadataGeneratorConfigurationContext.builder()
            .samlIdPMetadataLocator(samlIdPMetadataLocator())
            .samlIdPCertificateAndKeyWriter(samlSelfSignedCertificateWriter.getObject())
            .resourceLoader(resourceLoader)
            .casProperties(casProperties)
            .metadataCipherExecutor(amazonS3SamlIdPMetadataCipherExecutor())
            .build();
        val generator = new AmazonS3SamlIdPMetadataGenerator(context,
            amazonS3Client.getObject(),
            idp.getMetadata().getAmazonS3().getIdpMetadataBucketName());
        generator.generate(Optional.empty());
        return generator;
    }

    @Bean
    @SneakyThrows
    public SamlIdPMetadataLocator samlIdPMetadataLocator() {
        val idp = casProperties.getAuthn().getSamlIdp();
        return new AmazonS3SamlIdPMetadataLocator(amazonS3SamlIdPMetadataCipherExecutor(),
            idp.getMetadata().getAmazonS3().getIdpMetadataBucketName(),
            amazonS3Client.getObject());
    }

}
