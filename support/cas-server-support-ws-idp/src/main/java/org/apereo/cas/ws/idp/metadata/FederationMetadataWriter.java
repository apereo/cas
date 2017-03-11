package org.apereo.cas.ws.idp.metadata;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.fediz.core.util.CertsUtils;
import org.apache.cxf.fediz.core.util.SignatureUtils;
import org.apache.cxf.staxutils.W3CDOMStreamWriter;
import org.apache.wss4j.common.crypto.Crypto;
import org.apache.wss4j.common.util.DOM2Writer;
import org.apache.xml.security.stax.impl.util.IDGenerator;
import org.apache.xml.security.utils.Base64;
import org.apereo.cas.ws.idp.api.RealmAwareIdentityProvider;
import org.jooq.lambda.Unchecked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.stream.XMLStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;

import static org.apache.cxf.fediz.core.FedizConstants.*;

/**
 * This is {@link FederationMetadataWriter}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class FederationMetadataWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(FederationMetadataWriter.class);

    /**
     * Produce metadata document document.
     *
     * @param config the config
     * @return the document
     */
    public Document produceMetadataDocument(final RealmAwareIdentityProvider config) {
        try {
            final Crypto crypto = CertsUtils.getCryptoFromFile(config.getCertificate().getCanonicalPath());
            final W3CDOMStreamWriter writer = new W3CDOMStreamWriter();
            writer.writeStartDocument(StandardCharsets.UTF_8.name(), "1.0");

            final String referenceID = IDGenerator.generateID("_");
            writer.writeStartElement("md", "EntityDescriptor", SAML2_METADATA_NS);
            writer.writeAttribute("ID", referenceID);
            writer.writeAttribute("entityID", config.getIdpUrl().toString());

            writer.writeNamespace("md", SAML2_METADATA_NS);
            writer.writeNamespace("fed", WS_FEDERATION_NS);
            writer.writeNamespace("wsa", WS_ADDRESSING_NS);
            writer.writeNamespace("auth", WS_FEDERATION_NS);
            writer.writeNamespace("xsi", SCHEMA_INSTANCE_NS);

            writeFederationMetadata(writer, config, crypto);

            writer.writeEndElement();
            writer.writeEndDocument();
            writer.close();


            final String out = DOM2Writer.nodeToString(writer.getDocument());
            LOGGER.debug("Produced unsigned metadata");
            LOGGER.debug(out);

            final Document result = SignatureUtils.signMetaInfo(crypto, null, config.getCertificatePassword(), writer.getDocument(), referenceID);
            if (result != null) {
                return result;
            }
            throw new RuntimeException("Failed to sign the metadata document");
        } catch (final Exception e) {
            throw new RuntimeException("Error creating service metadata information: " + e.getMessage());
        }
    }

    private void writeFederationMetadata(final XMLStreamWriter writer, final RealmAwareIdentityProvider config, final Crypto crypto) throws Exception {
        writer.writeStartElement("md", "RoleDescriptor", WS_FEDERATION_NS);
        writer.writeAttribute(SCHEMA_INSTANCE_NS, "type", "fed:SecurityTokenServiceType");
        writer.writeAttribute("protocolSupportEnumeration", WS_FEDERATION_NS);
        if (StringUtils.isNotBlank(config.getDescription())) {
            writer.writeAttribute("ServiceDescription", config.getDescription());
        }
        if (StringUtils.isNotBlank(config.getDisplayName())) {
            writer.writeAttribute("ServiceDisplayName", config.getDisplayName());
        }

        writer.writeStartElement("", "KeyDescriptor", SAML2_METADATA_NS);
        writer.writeAttribute("use", "signing");
        writer.writeStartElement("", "KeyInfo", "http://www.w3.org/2000/09/xmldsig#");
        writer.writeStartElement("", "X509Data", "http://www.w3.org/2000/09/xmldsig#");
        writer.writeStartElement("", "X509Certificate", "http://www.w3.org/2000/09/xmldsig#");

        try {
            final String keyAlias = crypto.getDefaultX509Identifier();
            final X509Certificate cert = CertsUtils.getX509CertificateFromCrypto(crypto, keyAlias);
            writer.writeCharacters(Base64.encode(cert.getEncoded()));
        } catch (final Exception ex) {
            LOGGER.error("Failed to add certificate information to metadata. Metadata incomplete", ex);
        }

        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();

        writer.writeStartElement("fed", "SecurityTokenServiceEndpoint", WS_FEDERATION_NS);
        writer.writeStartElement("wsa", "EndpointReference", WS_ADDRESSING_NS);

        writer.writeStartElement("wsa", "Address", WS_ADDRESSING_NS);
        writer.writeCharacters(config.getStsUrl().toString());

        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();

        writer.writeStartElement("fed", "PassiveRequestorEndpoint", WS_FEDERATION_NS);
        writer.writeStartElement("wsa", "EndpointReference", WS_ADDRESSING_NS);

        writer.writeStartElement("wsa", "Address", WS_ADDRESSING_NS);
        writer.writeCharacters(config.getIdpUrl().toString());

        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();


        // create ClaimsType section
        if (!config.getClaimTypesOffered().isEmpty()) {
            writer.writeStartElement("fed", "ClaimTypesOffered", WS_FEDERATION_NS);
            config.getClaimTypesOffered().forEach(Unchecked.consumer(claim -> {
                writer.writeStartElement("auth", "ClaimType", WS_FEDERATION_NS);
                writer.writeAttribute("Uri", claim.getClaimType().toString());
                writer.writeAttribute("Optional", "true");
                writer.writeEndElement();
            }));
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }
}
