package org.apereo.cas.ws.idp.metadata;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.util.CryptoUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.ws.idp.WSFederationClaims;
import org.apereo.cas.ws.idp.WSFederationConstants;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.fediz.core.util.CertsUtils;
import org.apache.cxf.fediz.core.util.SignatureUtils;
import org.apache.cxf.staxutils.W3CDOMStreamWriter;
import org.apache.wss4j.common.crypto.Crypto;
import org.apache.wss4j.common.crypto.CryptoFactory;
import org.apache.wss4j.common.util.DOM2Writer;
import org.apache.xml.security.stax.impl.util.IDGenerator;
import org.jooq.lambda.Unchecked;
import org.w3c.dom.Document;

import javax.xml.stream.XMLStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.apache.cxf.fediz.core.FedizConstants.SAML2_METADATA_NS;
import static org.apache.cxf.fediz.core.FedizConstants.SCHEMA_INSTANCE_NS;
import static org.apache.cxf.fediz.core.FedizConstants.WS_ADDRESSING_NS;
import static org.apache.cxf.fediz.core.FedizConstants.WS_FEDERATION_NS;

/**
 * This is {@link WSFederationMetadataWriter}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@NoArgsConstructor
public class WSFederationMetadataWriter {

    /**
     * Produce metadata document.
     *
     * @param config the config
     * @return the document
     */
    public static Document produceMetadataDocument(final CasConfigurationProperties config) {
        try {
            val wsfedIdp = config.getAuthn().getWsfedIdp();
            val sts = wsfedIdp.getSts();
            val prop = CryptoUtils.getSecurityProperties(sts.getRealm().getKeystoreFile(),
                sts.getRealm().getKeystorePassword(), sts.getRealm().getKeystoreAlias());
            val crypto = CryptoFactory.getInstance(prop);
            val writer = new W3CDOMStreamWriter();
            writer.writeStartDocument(StandardCharsets.UTF_8.name(), "1.0");
            val referenceID = IDGenerator.generateID("_");
            writer.writeStartElement("md", "EntityDescriptor", SAML2_METADATA_NS);
            writer.writeAttribute("ID", referenceID);
            val idpEntityId = config.getServer().getPrefix().concat(WSFederationConstants.ENDPOINT_FEDERATION_REQUEST);
            writer.writeAttribute("entityID", idpEntityId);
            writer.writeNamespace("md", SAML2_METADATA_NS);
            writer.writeNamespace("fed", WS_FEDERATION_NS);
            writer.writeNamespace("wsa", WS_ADDRESSING_NS);
            writer.writeNamespace("auth", WS_FEDERATION_NS);
            writer.writeNamespace("xsi", SCHEMA_INSTANCE_NS);
            val stsUrl = config.getServer().getPrefix().concat(WSFederationConstants.ENDPOINT_STS).concat(wsfedIdp.getIdp().getRealmName());
            writeFederationMetadata(writer, idpEntityId, stsUrl, crypto);
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.close();
            val out = DOM2Writer.nodeToString(writer.getDocument());
            LOGGER.trace(out);
            val result = SignatureUtils.signMetaInfo(crypto, null, sts.getRealm().getKeyPassword(), writer.getDocument(), referenceID);
            if (result != null) {
                return result;
            }
            throw new IllegalArgumentException("Failed to sign the metadata document");
        } catch (final Exception e) {
            throw new IllegalArgumentException("Error creating service metadata information: " + e.getMessage(), e);
        }
    }

    private static void writeFederationMetadata(final XMLStreamWriter writer, final String idpEntityId, final String ststUrl, final Crypto crypto) throws Exception {
        writer.writeStartElement("md", "RoleDescriptor", WS_FEDERATION_NS);
        writer.writeAttribute(SCHEMA_INSTANCE_NS, "type", "fed:SecurityTokenServiceType");
        writer.writeAttribute("protocolSupportEnumeration", WS_FEDERATION_NS);
        writer.writeStartElement(StringUtils.EMPTY, "KeyDescriptor", SAML2_METADATA_NS);
        writer.writeAttribute("use", "signing");
        writer.writeStartElement(StringUtils.EMPTY, "KeyInfo", "http://www.w3.org/2000/09/xmldsig#");
        writer.writeStartElement(StringUtils.EMPTY, "X509Data", "http://www.w3.org/2000/09/xmldsig#");
        writer.writeStartElement(StringUtils.EMPTY, "X509Certificate", "http://www.w3.org/2000/09/xmldsig#");

        val keyAlias = crypto.getDefaultX509Identifier();
        val cert = CertsUtils.getX509CertificateFromCrypto(crypto, keyAlias);
        writer.writeCharacters(EncodingUtils.encodeBase64(cert.getEncoded()));

        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeStartElement("fed", "SecurityTokenServiceEndpoint", WS_FEDERATION_NS);
        writer.writeStartElement("wsa", "EndpointReference", WS_ADDRESSING_NS);
        writer.writeStartElement("wsa", "Address", WS_ADDRESSING_NS);
        writer.writeCharacters(ststUrl);
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeStartElement("fed", "PassiveRequestorEndpoint", WS_FEDERATION_NS);
        writer.writeStartElement("wsa", "EndpointReference", WS_ADDRESSING_NS);
        writer.writeStartElement("wsa", "Address", WS_ADDRESSING_NS);
        writer.writeCharacters(idpEntityId);
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeStartElement("fed", "ClaimTypesOffered", WS_FEDERATION_NS);
        Arrays.stream(WSFederationClaims.values()).forEach(Unchecked.consumer(claim -> {
            writer.writeStartElement("auth", "ClaimType", WS_FEDERATION_NS);
            writer.writeAttribute("Uri", claim.getUri());
            writer.writeAttribute("Optional", "true");
            writer.writeEndElement();
        }));
        writer.writeEndElement();
        writer.writeEndElement();
    }
}
