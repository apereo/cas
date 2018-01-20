package org.apereo.cas.ws.idp.services;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.fediz.core.exception.ProcessingException;
import org.apache.cxf.rt.security.SecurityConstants;
import org.apache.cxf.staxutils.W3CDOMStreamWriter;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.apache.cxf.ws.security.trust.STSUtils;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.SecurityTokenServiceClient;
import org.apereo.cas.authentication.SecurityTokenServiceClientBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.ws.idp.WSFederationClaims;
import org.apereo.cas.ws.idp.WSFederationConstants;
import org.apereo.cas.ws.idp.web.WSFederationRequest;
import org.jasig.cas.client.validation.Assertion;
import org.w3c.dom.Element;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.Collection;

/**
 * This is {@link DefaultRelyingPartyTokenProducer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@AllArgsConstructor
public class DefaultRelyingPartyTokenProducer implements WSFederationRelyingPartyTokenProducer {
    private final SecurityTokenServiceClientBuilder clientBuilder;
    private final CipherExecutor<String, String> credentialCipherExecutor;

    @Override
    public String produce(final SecurityToken securityToken, final WSFederationRegisteredService service,
                          final WSFederationRequest fedRequest, final HttpServletRequest request,
                          final Assertion assertion) {
        final SecurityTokenServiceClient sts = clientBuilder.buildClientForRelyingPartyTokenResponses(securityToken, service);
        mapAttributesToRequestedClaims(service, sts, assertion);
        final Element rpToken = requestSecurityTokenResponse(service, sts, assertion);
        return serializeRelyingPartyToken(rpToken);
    }

    @SneakyThrows
    private static String serializeRelyingPartyToken(final Element rpToken) {
        final StringWriter sw = new StringWriter();
        final Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, BooleanUtils.toStringYesNo(Boolean.TRUE));
        t.transform(new DOMSource(rpToken), new StreamResult(sw));
        return sw.toString();
    }

    private static void mapAttributesToRequestedClaims(final WSFederationRegisteredService service, final SecurityTokenServiceClient sts,
                                                       final Assertion assertion) {
        try {
            final W3CDOMStreamWriter writer = new W3CDOMStreamWriter();
            writer.writeStartElement("wst", "Claims", STSUtils.WST_NS_05_12);
            writer.writeNamespace("wst", STSUtils.WST_NS_05_12);
            writer.writeNamespace("ic", WSFederationConstants.HTTP_SCHEMAS_XMLSOAP_ORG_WS_2005_05_IDENTITY);
            writer.writeAttribute("Dialect", WSFederationConstants.HTTP_SCHEMAS_XMLSOAP_ORG_WS_2005_05_IDENTITY);

            assertion.getPrincipal().getAttributes().forEach((k, v) -> {
                try {
                    if (WSFederationClaims.contains(k)) {
                        final String uri = WSFederationClaims.valueOf(k).getUri();
                        LOGGER.debug("Requesting claim [{}] mapped to [{}]", k, uri);
                        writer.writeStartElement("ic", "ClaimValue", WSFederationConstants.HTTP_SCHEMAS_XMLSOAP_ORG_WS_2005_05_IDENTITY);
                        writer.writeAttribute("Uri", uri);
                        writer.writeAttribute("Optional", Boolean.TRUE.toString());

                        final Collection vv = CollectionUtils.toCollection(v);
                        for (final Object value : vv) {
                            if (value instanceof String) {
                                writer.writeStartElement("ic", "Value", WSFederationConstants.HTTP_SCHEMAS_XMLSOAP_ORG_WS_2005_05_IDENTITY);
                                writer.writeCharacters((String) value);
                                writer.writeEndElement();
                            }
                        }

                        writer.writeEndElement();
                    }
                } catch (final Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            });

            writer.writeEndElement();

            final Element claims = writer.getDocument().getDocumentElement();
            sts.setClaims(claims);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @SneakyThrows
    private Element requestSecurityTokenResponse(final WSFederationRegisteredService service,
                                                 final SecurityTokenServiceClient sts,
                                                 final Assertion assertion) {
        try {
            sts.getProperties().put(SecurityConstants.USERNAME, assertion.getPrincipal().getName());
            final String uid = credentialCipherExecutor.encode(assertion.getPrincipal().getName());
            sts.getProperties().put(SecurityConstants.PASSWORD, uid);

            return sts.requestSecurityTokenResponse(service.getAppliesTo());
        } catch (final SoapFault ex) {
            if (ex.getFaultCode() != null && "RequestFailed".equals(ex.getFaultCode().getLocalPart())) {
                throw new IllegalArgumentException(new ProcessingException(ProcessingException.TYPE.BAD_REQUEST));
            }
            throw ex;
        }
    }
}
