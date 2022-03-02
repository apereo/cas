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
import org.apache.cxf.fediz.core.FedizConstants;
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
     * @throws Exception the exception
     */
    public static Document produceMetadataDocument(final CasConfigurationProperties config) throws Exception {
        val wsfedIdp = config.getAuthn().getWsfedIdp();
        val sts = wsfedIdp.getSts();
        val prop = CryptoUtils.getSecurityProperties(sts.getRealm().getKeystoreFile(),
            sts.getRealm().getKeystorePassword(), sts.getRealm().getKeystoreAlias());
        val crypto = CryptoFactory.getInstance(prop);
        val writer = new W3CDOMStreamWriter();
        writer.writeStartDocument(StandardCharsets.UTF_8.name(), "1.0");
        val referenceID = IDGenerator.generateID("_");
        writer.writeStartElement("md", "EntityDescriptor", FedizConstants.SAML2_METADATA_NS);
        writer.writeAttribute("ID", referenceID);
        val idpEntityId = config.getServer().getPrefix().concat(WSFederationConstants.ENDPOINT_FEDERATION_REQUEST);
        writer.writeAttribute("entityID", idpEntityId);
        writer.writeNamespace("md", FedizConstants.SAML2_METADATA_NS);
        writer.writeNamespace("fed", FedizConstants.WS_FEDERATION_NS);
        writer.writeNamespace("wsa", FedizConstants.WS_ADDRESSING_NS);
        writer.writeNamespace("auth", FedizConstants.WS_FEDERATION_NS);
        writer.writeNamespace("xsi", FedizConstants.SCHEMA_INSTANCE_NS);
        val stsUrl = config.getServer().getPrefix().concat(WSFederationConstants.BASE_ENDPOINT_STS).concat(wsfedIdp.getIdp().getRealmName());
        writeFederationMetadata(writer, idpEntityId, stsUrl, crypto);
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.close();
        val out = DOM2Writer.nodeToString(writer.getDocument());
        LOGGER.trace(out);
        return SignatureUtils.signMetaInfo(crypto, null, sts.getRealm().getKeyPassword(), writer.getDocument(), referenceID);
    }

    private static void writeFederationMetadata(final XMLStreamWriter writer, final String idpEntityId,
                                                final String ststUrl, final Crypto crypto) throws Exception {
        writer.writeStartElement("md", "RoleDescriptor", FedizConstants.WS_FEDERATION_NS);
        writer.writeAttribute(FedizConstants.SCHEMA_INSTANCE_NS, "type", "fed:SecurityTokenServiceType");
        writer.writeAttribute("protocolSupportEnumeration", FedizConstants.WS_FEDERATION_NS);
        writer.writeStartElement(StringUtils.EMPTY, "KeyDescriptor", FedizConstants.SAML2_METADATA_NS);
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
        writer.writeStartElement("fed", "SecurityTokenServiceEndpoint", FedizConstants.WS_FEDERATION_NS);
        writer.writeStartElement("wsa", "EndpointReference", FedizConstants.WS_ADDRESSING_NS);
        writer.writeStartElement("wsa", "Address", FedizConstants.WS_ADDRESSING_NS);
        writer.writeCharacters(ststUrl);
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeStartElement("fed", "PassiveRequestorEndpoint", FedizConstants.WS_FEDERATION_NS);
        writer.writeStartElement("wsa", "EndpointReference", FedizConstants.WS_ADDRESSING_NS);
        writer.writeStartElement("wsa", "Address", FedizConstants.WS_ADDRESSING_NS);
        writer.writeCharacters(idpEntityId);
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeStartElement("fed", "ClaimTypesOffered", FedizConstants.WS_FEDERATION_NS);
        Arrays.stream(WSFederationClaims.values()).forEach(Unchecked.consumer(claim -> {
            writer.writeStartElement("auth", "ClaimType", FedizConstants.WS_FEDERATION_NS);
            writer.writeAttribute("Uri", claim.getUri());
            writer.writeAttribute("Optional", "true");
            writer.writeEndElement();
        }));
        writer.writeEndElement();
        writer.writeEndElement();
    }
}
