package org.apereo.cas.support.saml.idp.metadata.generator;

import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

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

    private final SamlIdPMetadataGeneratorConfigurationContext configurationContext;

    @Override
    @SneakyThrows
    public SamlIdPMetadataDocument generate(final Optional<SamlRegisteredService> registeredService) {
        val idp = configurationContext.getCasProperties().getAuthn().getSamlIdp();
        LOGGER.debug("Preparing to generate metadata for entityId [{}]", idp.getEntityId());
        val samlIdPMetadataLocator = configurationContext.getSamlIdPMetadataLocator();
        if (!samlIdPMetadataLocator.exists(registeredService)) {
            LOGGER.trace("Metadata does not exist. Creating...");

            LOGGER.info("Creating self-signed certificate for signing...");
            val signing = buildSelfSignedSigningCert(registeredService);

            LOGGER.info("Creating self-signed certificate for encryption...");
            val encryption = buildSelfSignedEncryptionCert(registeredService);

            LOGGER.info("Creating metadata...");
            val metadata = buildMetadataGeneratorParameters(signing, encryption, registeredService);

            val doc = newSamlIdPMetadataDocument();
            doc.setEncryptionCertificate(encryption.getKey());
            doc.setEncryptionKey(encryption.getValue());
            doc.setSigningCertificate(signing.getKey());
            doc.setSigningKey(signing.getValue());
            doc.setMetadata(metadata);
            return finalizeMetadataDocument(doc, registeredService);
        }

        return samlIdPMetadataLocator.fetch(registeredService);
    }

    /**
     * Build self signed encryption cert.
     *
     * @param registeredService registered service
     * @return the pair
     */
    public abstract Pair<String, String> buildSelfSignedEncryptionCert(Optional<SamlRegisteredService> registeredService);

    /**
     * Build self signed signing cert.
     *
     * @param registeredService registered service
     * @return the pair
     */
    public abstract Pair<String, String> buildSelfSignedSigningCert(Optional<SamlRegisteredService> registeredService);

    protected SamlIdPMetadataDocument newSamlIdPMetadataDocument() {
        return new SamlIdPMetadataDocument();
    }

    /**
     * Finalize metadata document saml idp metadata document.
     *
     * @param doc               the doc
     * @param registeredService the registered service
     * @return the saml id p metadata document
     */
    protected SamlIdPMetadataDocument finalizeMetadataDocument(final SamlIdPMetadataDocument doc,
        final Optional<SamlRegisteredService> registeredService) {
        return doc;
    }

    /**
     * Write metadata.
     *
     * @param metadata          the metadata
     * @param registeredService registered service
     * @return the string
     */
    protected String writeMetadata(final String metadata, final Optional<SamlRegisteredService> registeredService) {
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
            configurationContext.getSamlIdPCertificateAndKeyWriter().writeCertificateAndKey(keyWriter, certWriter);
            val encryptionKey = configurationContext.getMetadataCipherExecutor().encode(keyWriter.toString());
            return Pair.of(certWriter.toString(), encryptionKey);
        }
    }

    private String getIdPEndpointUrl() {
        val resolver = SpringExpressionLanguageValueResolver.getInstance();
        return resolver.resolve(configurationContext.getCasProperties().getServer().getPrefix().concat("/idp"));
    }

    /**
     * Build metadata generator parameters by passing the encryption,
     * signing and back-channel certs to the parameter generator.
     *
     * @param signing           the signing
     * @param encryption        the encryption
     * @param registeredService registered service
     * @return the metadata
     */
    @SneakyThrows
    private String buildMetadataGeneratorParameters(final Pair<String, String> signing,
        final Pair<String, String> encryption,
        final Optional<SamlRegisteredService> registeredService) {
        val template = configurationContext.getResourceLoader().getResource("classpath:/template-idp-metadata.xml");
        val signingCert = SamlIdPMetadataGenerator.cleanCertificate(signing.getKey());
        val encryptionCert = SamlIdPMetadataGenerator.cleanCertificate(encryption.getKey());

        val idp = configurationContext.getCasProperties().getAuthn().getSamlIdp();
        try (val writer = new StringWriter()) {
            IOUtils.copy(template.getInputStream(), writer, StandardCharsets.UTF_8);
            val resolver = SpringExpressionLanguageValueResolver.getInstance();
            val entityId = resolver.resolve(idp.getEntityId());
            val scope = resolver.resolve(configurationContext.getCasProperties().getServer().getScope());
            val metadata = writer.toString()
                .replace("${entityId}", entityId)
                .replace("${scope}", scope)
                .replace("${idpEndpointUrl}", getIdPEndpointUrl())
                .replace("${encryptionKey}", encryptionCert)
                .replace("${signingKey}", signingCert);

            writeMetadata(metadata, registeredService);
            return metadata;
        }
    }
}
