package org.apereo.cas.support.saml.idp.metadata.generator;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.writer.SamlIdPCertificateAndKeyWriter;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
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

    /**
     * Cipher to encrypt metadata.
     */
    protected final CipherExecutor<String, String> metadataCipherExecutor;

    private final String entityId;
    private final ResourceLoader resourceLoader;
    private final String casServerPrefix;
    private final String scope;

    @Override
    @SneakyThrows
    public SamlIdPMetadataDocument generate() {
        LOGGER.debug("Preparing to generate metadata for entityId [{}]", this.entityId);
        if (!samlIdPMetadataLocator.exists()) {
            LOGGER.trace("Metadata does not exist. Creating...");

            LOGGER.info("Creating self-signed certificate for signing...");
            val signing = buildSelfSignedSigningCert();

            LOGGER.info("Creating self-signed certificate for encryption...");
            val encryption = buildSelfSignedEncryptionCert();

            LOGGER.info("Creating metadata...");
            val metadata = buildMetadataGeneratorParameters(signing, encryption);

            val doc = new SamlIdPMetadataDocument();
            doc.setEncryptionCertificate(encryption.getKey());
            doc.setEncryptionKey(encryption.getValue());
            doc.setSigningCertificate(signing.getKey());
            doc.setSigningKey(signing.getValue());
            doc.setMetadata(metadata);
            return finalizeMetadataDocument(doc);
        }

        return samlIdPMetadataLocator.fetch();
    }

    /**
     * Finalize metadata document saml idp metadata document.
     *
     * @param doc the doc
     * @return the saml id p metadata document
     */
    protected SamlIdPMetadataDocument finalizeMetadataDocument(final SamlIdPMetadataDocument doc) {
        return doc;
    }

    private String getIdPEndpointUrl() {
        return this.casServerPrefix.concat("/idp");
    }

    /**
     * Build self signed encryption cert.
     *
     * @return the pair
     */
    public abstract Pair<String, String> buildSelfSignedEncryptionCert();

    /**
     * Build self signed signing cert.
     *
     * @return the pair
     */
    public abstract Pair<String, String> buildSelfSignedSigningCert();

    /**
     * Build metadata generator parameters by passing the encryption,
     * signing and back-channel certs to the parameter generator.
     *
     * @param signing    the signing
     * @param encryption the encryption
     * @return the metadata
     */
    @SneakyThrows
    private String buildMetadataGeneratorParameters(final Pair<String, String> signing, final Pair<String, String> encryption) {
        val template = this.resourceLoader.getResource("classpath:/template-idp-metadata.xml");

        var signingCert = signing.getKey();
        signingCert = StringUtils.remove(signingCert, BEGIN_CERTIFICATE);
        signingCert = StringUtils.remove(signingCert, END_CERTIFICATE).trim();

        var encryptionCert = encryption.getKey();
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
            return metadata;
        }
    }

    /**
     * Write metadata.
     *
     * @param metadata the metadata
     * @return the string
     */
    protected String writeMetadata(final String metadata) {
        return metadata;
    }

    /**
     * Generate certificate and key pair.
     *
     * @return the pair where key/left is the certificate and value is the key
     */
    @SneakyThrows
    protected Pair<String, String> generateCertificateAndKey() {
        try (val certWriter = new StringWriter(); val keyWriter = new StringWriter()) {
            this.samlIdPCertificateAndKeyWriter.writeCertificateAndKey(keyWriter, certWriter);
            val encryptionKey = metadataCipherExecutor.encode(keyWriter.toString());
            return Pair.of(certWriter.toString(), encryptionKey);
        }
    }
}
