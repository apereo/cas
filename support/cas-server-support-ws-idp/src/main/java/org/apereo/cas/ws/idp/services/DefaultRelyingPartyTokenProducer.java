package org.apereo.cas.ws.idp.services;

import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.SecurityTokenServiceClient;
import org.apereo.cas.authentication.SecurityTokenServiceClientBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.ws.idp.WSFederationClaims;
import org.apereo.cas.ws.idp.WSFederationConstants;
import org.apereo.cas.ws.idp.web.WSFederationRequest;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.fediz.core.exception.ProcessingException;
import org.apache.cxf.rt.security.SecurityConstants;
import org.apache.cxf.staxutils.W3CDOMStreamWriter;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.apache.cxf.ws.security.trust.STSUtils;
import org.jasig.cas.client.validation.Assertion;
import org.w3c.dom.Element;

import javax.servlet.http.HttpServletRequest;
import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.Set;

/**
 * This is {@link DefaultRelyingPartyTokenProducer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultRelyingPartyTokenProducer implements WSFederationRelyingPartyTokenProducer {
    private final SecurityTokenServiceClientBuilder clientBuilder;
    private final CipherExecutor<String, String> credentialCipherExecutor;
    private final Set<String> customClaims;

    @SneakyThrows
    private static String serializeRelyingPartyToken(final Element rpToken) {
        val sw = new StringWriter();
        val transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        val t = transformerFactory.newTransformer();
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, BooleanUtils.toStringYesNo(Boolean.TRUE));
        t.transform(new DOMSource(rpToken), new StreamResult(sw));
        return sw.toString();
    }

    private void mapAttributesToRequestedClaims(final WSFederationRegisteredService service, final SecurityTokenServiceClient sts,
                                                final Assertion assertion) {
        try {
            val writer = new W3CDOMStreamWriter();
            writer.writeStartElement("wst", "Claims", STSUtils.WST_NS_05_12);
            writer.writeNamespace("wst", STSUtils.WST_NS_05_12);
            writer.writeNamespace("ic", WSFederationConstants.HTTP_SCHEMAS_XMLSOAP_ORG_WS_2005_05_IDENTITY);
            writer.writeAttribute("Dialect", WSFederationConstants.HTTP_SCHEMAS_XMLSOAP_ORG_WS_2005_05_IDENTITY);

            val attributes = assertion.getPrincipal().getAttributes();
            LOGGER.debug("Mapping principal attributes [{}] to claims for service [{}]", attributes, service);

            attributes.forEach((k, v) -> {
                try {
                    val claimName = ProtocolAttributeEncoder.decodeAttribute(k);
                    if (WSFederationClaims.contains(claimName)) {
                        val uri = WSFederationClaims.valueOf(k).getUri();
                        LOGGER.debug("Requested claim [{}] mapped to [{}]", k, uri);
                        writeAttributeValue(writer, uri, v, service);
                    } else if (WSFederationClaims.containsUri(claimName)) {
                        LOGGER.debug("Requested claim [{}] directly mapped to [{}]", k, claimName);
                        writeAttributeValue(writer, claimName, v, service);
                    } else if (customClaims.contains(claimName)) {
                        LOGGER.debug("Requested custom claim [{}]", claimName);
                        writeAttributeValue(writer, claimName, v, service);
                    } else {
                        LOGGER.debug("Requested claim [{}] is not defined/supported by CAS", claimName);
                        writeAttributeValue(writer, WSFederationConstants.getClaimInCasNamespace(claimName), v, service);
                    }
                } catch (final Exception e) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.error(e.getMessage(), e);
                    } else {
                        LOGGER.error(e.getMessage());
                    }
                }
            });

            writer.writeEndElement();

            val claims = writer.getDocument().getDocumentElement();
            sts.setClaims(claims);
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
    }

    /**
     * Write attribute value.
     *
     * @param writer         the writer
     * @param uri            the uri
     * @param attributeValue the attribute value
     * @param service        the service
     * @throws Exception the exception
     */
    protected void writeAttributeValue(final W3CDOMStreamWriter writer, final String uri,
                                            final Object attributeValue,
                                            final WSFederationRegisteredService service) throws Exception {
        LOGGER.trace("Mapping attribute [{}] with value [{}] for service [{}]", uri, attributeValue, service.getServiceId());
        writer.writeStartElement("ic", "ClaimValue", WSFederationConstants.HTTP_SCHEMAS_XMLSOAP_ORG_WS_2005_05_IDENTITY);
        writer.writeAttribute("Uri", uri);
        writer.writeAttribute("Optional", Boolean.TRUE.toString());

        val values = CollectionUtils.toCollection(attributeValue);
        for (val value : values) {
            writer.writeStartElement("ic", "Value", WSFederationConstants.HTTP_SCHEMAS_XMLSOAP_ORG_WS_2005_05_IDENTITY);
            writer.writeCharacters(value.toString());
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    @Override
    public String produce(final SecurityToken securityToken, final WSFederationRegisteredService service,
                          final WSFederationRequest fedRequest, final HttpServletRequest request,
                          final Assertion assertion) {
        val sts = clientBuilder.buildClientForRelyingPartyTokenResponses(securityToken, service);
        mapAttributesToRequestedClaims(service, sts, assertion);
        val rpToken = requestSecurityTokenResponse(service, sts, assertion);
        return serializeRelyingPartyToken(rpToken);
    }

    @SneakyThrows
    private Element requestSecurityTokenResponse(final WSFederationRegisteredService service,
                                                 final SecurityTokenServiceClient sts,
                                                 final Assertion assertion) {
        try {
            sts.getProperties().put(SecurityConstants.USERNAME, assertion.getPrincipal().getName());
            val uid = credentialCipherExecutor.encode(assertion.getPrincipal().getName());
            sts.getProperties().put(SecurityConstants.PASSWORD, uid);

            return sts.requestSecurityTokenResponse(service.getAppliesTo());
        } catch (final SoapFault ex) {
            if (ex.getFaultCode() != null && "RequestFailed".equals(ex.getFaultCode().getLocalPart())) {
                throw new IllegalArgumentException(new ProcessingException(ProcessingException.TYPE.BAD_REQUEST));
            }
            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        }
    }
}
