package org.apereo.cas.support.saml.idp.metadata.generator;

import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * A metadata generator based on a predefined template.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public abstract class BaseSamlIdPMetadataGenerator implements SamlIdPMetadataGenerator {

    private final SamlIdPMetadataGeneratorConfigurationContext samlIdPMetadataGeneratorConfigurationContext;

    @Override
    @SneakyThrows
    public SamlIdPMetadataDocument generate() {
        LOGGER.debug("Preparing to generate metadata for entityId [{}]", samlIdPMetadataGeneratorConfigurationContext.getEntityId());
        if (!samlIdPMetadataGeneratorConfigurationContext.getSamlIdPMetadataLocator().exists(Optional.empty())) {
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

        return samlIdPMetadataGeneratorConfigurationContext.getSamlIdPMetadataLocator().fetch(Optional.empty());
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
        return samlIdPMetadataGeneratorConfigurationContext.getCasServerPrefix().concat("/idp");
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
        val template = samlIdPMetadataGeneratorConfigurationContext.getResourceLoader().getResource("classpath:/template-idp-metadata.xml");
        val signingCert = SamlIdPMetadataGenerator.cleanCertificate(signing.getKey());
        val encryptionCert = SamlIdPMetadataGenerator.cleanCertificate(encryption.getKey());

        try (val writer = new StringWriter()) {
            IOUtils.copy(template.getInputStream(), writer, StandardCharsets.UTF_8);
            val metadata = writer.toString()
                .replace("${entityId}", samlIdPMetadataGeneratorConfigurationContext.getEntityId())
                .replace("${scope}", samlIdPMetadataGeneratorConfigurationContext.getScope())
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
            samlIdPMetadataGeneratorConfigurationContext.getSamlIdPCertificateAndKeyWriter().writeCertificateAndKey(keyWriter, certWriter);
            val encryptionKey = samlIdPMetadataGeneratorConfigurationContext.getMetadataCipherExecutor().encode(keyWriter.toString());
            return Pair.of(certWriter.toString(), encryptionKey);
        }
    }
}
