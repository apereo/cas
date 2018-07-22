package org.apereo.cas.support.saml.idp.metadata.generator;

import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.writer.SamlIdPCertificateAndKeyWriter;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ResourceLoader;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

/**
 * A metadata generator based on a predefined template.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BaseSamlIdPMetadataGenerator implements SamlIdPMetadataGenerator {
    private static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";
    private static final String END_CERTIFICATE = "-----END CERTIFICATE-----";

    /**
     * Metadata locator.
     */
    protected final SamlIdPMetadataLocator samlIdPMetadataLocator;

    /**
     * Metadata certificate writer.
     */
    protected final SamlIdPCertificateAndKeyWriter samlIdPCertificateAndKeyWriter;

    private final String entityId;
    private final ResourceLoader resourceLoader;
    private final String casServerPrefix;
    private final String scope;

    @Override
    @SneakyThrows
    public void generate() {
        LOGGER.debug("Preparing to generate metadata for entityId [{}]", this.entityId);
        if (!samlIdPMetadataLocator.exists()) {
            LOGGER.info("Metadata does not exist. Creating...");

            LOGGER.info("Creating self-signed certificate for signing...");
            buildSelfSignedSigningCert();

            LOGGER.info("Creating self-signed certificate for encryption...");
            buildSelfSignedEncryptionCert();

            LOGGER.info("Creating metadata...");
            buildMetadataGeneratorParameters();
        }
    }

    private String getIdPEndpointUrl() {
        return this.casServerPrefix.concat("/idp");
    }

    /**
     * Build self signed encryption cert.
     */
    public abstract void buildSelfSignedEncryptionCert();

    /**
     * Build self signed signing cert.
     */
    public abstract void buildSelfSignedSigningCert();

    /**
     * Build metadata generator parameters by passing the encryption,
     * signing and back-channel certs to the parameter generator.
     */
    @SneakyThrows
    protected void buildMetadataGeneratorParameters() {
        val template = this.resourceLoader.getResource("classpath:/template-idp-metadata.xml");

        var signingCert = IOUtils.toString(this.samlIdPMetadataLocator.getSigningCertificate().getInputStream(), StandardCharsets.UTF_8);
        signingCert = StringUtils.remove(signingCert, BEGIN_CERTIFICATE);
        signingCert = StringUtils.remove(signingCert, END_CERTIFICATE).trim();

        var encryptionCert = IOUtils.toString(this.samlIdPMetadataLocator.getEncryptionCertificate().getInputStream(), StandardCharsets.UTF_8);
        encryptionCert = StringUtils.remove(encryptionCert, BEGIN_CERTIFICATE);
        encryptionCert = StringUtils.remove(encryptionCert, END_CERTIFICATE).trim();

        try (val writer = new StringWriter()) {
            IOUtils.copy(template.getInputStream(), writer, StandardCharsets.UTF_8);
            val metadata = writer.toString()
                .replace("${entityId}", this.entityId)
                .replace("${scope}", this.scope)
                .replace("${idpEndpointUrl}", getIdPEndpointUrl())
                .replace("${encryptionKey}", encryptionCert)
                .replace("${signingKey}", signingCert);

            writeMetadata(metadata);
        }
    }

    /**
     * Write metadata.
     *
     * @param metadata the metadata
     */
    protected abstract void writeMetadata(String metadata);
}
