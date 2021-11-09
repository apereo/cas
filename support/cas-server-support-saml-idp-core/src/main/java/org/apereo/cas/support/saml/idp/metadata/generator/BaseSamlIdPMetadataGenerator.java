package org.apereo.cas.support.saml.idp.metadata.generator;

import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.velocity.VelocityContext;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.io.Serializable;
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
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class BaseSamlIdPMetadataGenerator implements SamlIdPMetadataGenerator {

    private final SamlIdPMetadataGeneratorConfigurationContext configurationContext;

    @Override
    @SneakyThrows
    public SamlIdPMetadataDocument generate(final Optional<SamlRegisteredService> registeredService) {
        val idp = configurationContext.getCasProperties().getAuthn().getSamlIdp();
        LOGGER.debug("Preparing to generate metadata for entityId [{}]", idp.getCore().getEntityId());
        val samlIdPMetadataLocator = configurationContext.getSamlIdPMetadataLocator();
        if (!samlIdPMetadataLocator.exists(registeredService)) {
            val owner = SamlIdPMetadataGenerator.getAppliesToFor(registeredService);
            LOGGER.trace("Metadata does not exist for [{}]", owner);

            if (samlIdPMetadataLocator.shouldGenerateMetadataFor(registeredService)) {
                LOGGER.trace("Creating metadata artifacts for [{}]...", owner);

                LOGGER.info("Creating self-signed certificate for signing...");
                val signing = buildSelfSignedSigningCert(registeredService);

                LOGGER.info("Creating self-signed certificate for encryption...");
                val encryption = buildSelfSignedEncryptionCert(registeredService);

                LOGGER.info("Creating SAML2 metadata for identity provider...");
                val metadata = buildMetadataGeneratorParameters(signing, encryption, registeredService);

                val doc = newSamlIdPMetadataDocument();
                doc.setEncryptionCertificate(encryption.getKey());
                doc.setEncryptionKey(encryption.getValue());
                doc.setSigningCertificate(signing.getKey());
                doc.setSigningKey(signing.getValue());
                doc.setMetadata(metadata);
                return finalizeMetadataDocument(doc, registeredService);
            } else {
                LOGGER.debug("Skipping metadata generation process for [{}]", owner);
            }
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

    /**
     * New saml id p metadata document.
     *
     * @return the saml id p metadata document
     */
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

    @SuperBuilder
    @Getter
    public static class IdPMetadataTemplateContext implements Serializable {
        private static final long serialVersionUID = -8084689071916142718L;

        private final String entityId;

        private final String scope;

        private final String endpointUrl;

        private final String encryptionCertificate;

        private final String signingCertificate;

        private final boolean ssoServicePostBindingEnabled;

        private final boolean ssoServicePostSimpleSignBindingEnabled;

        private final boolean ssoServiceRedirectBindingEnabled;

        private final boolean ssoServiceSoapBindingEnabled;

        private final boolean sloServicePostBindingEnabled;

        private final boolean sloServiceRedirectBindingEnabled;

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
    private String buildMetadataGeneratorParameters(final Pair<String, String> signing,
                                                    final Pair<String, String> encryption,
                                                    final Optional<SamlRegisteredService> registeredService) throws Exception {

        val signingCert = SamlIdPMetadataGenerator.cleanCertificate(signing.getKey());
        val encryptionCert = SamlIdPMetadataGenerator.cleanCertificate(encryption.getKey());

        val idp = configurationContext.getCasProperties().getAuthn().getSamlIdp();
        try (val writer = new StringWriter()) {
            val resolver = SpringExpressionLanguageValueResolver.getInstance();
            val entityId = resolver.resolve(idp.getCore().getEntityId());
            val scope = resolver.resolve(configurationContext.getCasProperties().getServer().getScope());

            val metadataCore = idp.getMetadata().getCore();
            val context = IdPMetadataTemplateContext.builder()
                .encryptionCertificate(encryptionCert)
                .signingCertificate(signingCert)
                .entityId(entityId)
                .scope(scope)
                .endpointUrl(getIdPEndpointUrl())
                .ssoServicePostBindingEnabled(metadataCore.isSsoServicePostBindingEnabled())
                .ssoServicePostSimpleSignBindingEnabled(metadataCore.isSsoServicePostSimpleSignBindingEnabled())
                .ssoServiceRedirectBindingEnabled(metadataCore.isSsoServiceRedirectBindingEnabled())
                .ssoServiceSoapBindingEnabled(metadataCore.isSsoServiceSoapBindingEnabled())
                .sloServicePostBindingEnabled(metadataCore.isSloServicePostBindingEnabled())
                .sloServiceRedirectBindingEnabled(metadataCore.isSloServiceRedirectBindingEnabled())
                .build();

            val template = configurationContext.getVelocityEngine()
                .getTemplate("/template-idp-metadata.vm", StandardCharsets.UTF_8.name());

            val velocityContext = new VelocityContext();
            velocityContext.put("context", context);
            template.merge(velocityContext, writer);
            var metadata = writer.toString();

            val customizers = configurationContext.getApplicationContext()
                .getBeansOfType(SamlIdPMetadataCustomizer.class).values();
            if (!customizers.isEmpty()) {
                val openSamlConfigBean = configurationContext.getOpenSamlConfigBean();
                val entityDescriptor = SamlUtils.transformSamlObject(openSamlConfigBean, metadata, EntityDescriptor.class);
                customizers.stream()
                    .sorted(AnnotationAwareOrderComparator.INSTANCE)
                    .forEach(customizer -> customizer.customize(entityDescriptor, registeredService));
                metadata = SamlUtils.transformSamlObject(openSamlConfigBean, entityDescriptor).toString();
            }
            writeMetadata(metadata, registeredService);
            return metadata;
        }
    }

}
