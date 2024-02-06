package org.apereo.cas.ws.idp.services;

import org.apereo.cas.authentication.SecurityTokenServiceClient;
import org.apereo.cas.authentication.SecurityTokenServiceClientBuilder;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.validation.TicketValidationResult;
import org.apereo.cas.ws.idp.WSFederationConstants;
import org.apereo.cas.ws.idp.web.WSFederationRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.fediz.core.exception.ProcessingException;
import org.apache.cxf.rt.security.SecurityConstants;
import org.apache.cxf.staxutils.W3CDOMStreamWriter;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.apache.cxf.ws.security.trust.STSUtils;
import org.w3c.dom.Element;

import jakarta.servlet.http.HttpServletRequest;
import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.StringWriter;

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

    private final WSFederationRelyingPartyAttributeWriter attributeWriter;

    private static String serializeRelyingPartyToken(final Element rpToken) throws Exception {
        val sw = new StringWriter();
        val transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        val t = transformerFactory.newTransformer();
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, BooleanUtils.toStringYesNo(Boolean.TRUE));
        t.transform(new DOMSource(rpToken), new StreamResult(sw));
        return sw.toString();
    }

    @Override
    public String produce(final SecurityToken securityToken, final WSFederationRegisteredService service,
                          final WSFederationRequest fedRequest, final HttpServletRequest request,
                          final TicketValidationResult assertion) throws Exception {
        val sts = clientBuilder.buildClientForRelyingPartyTokenResponses(securityToken, service);
        mapAttributesToRequestedClaims(service, sts, assertion);
        val rpToken = requestSecurityTokenResponse(service, sts, assertion);
        return serializeRelyingPartyToken(rpToken);
    }


    protected void mapAttributesToRequestedClaims(final WSFederationRegisteredService service,
                                                  final SecurityTokenServiceClient sts,
                                                  final TicketValidationResult assertion) throws Exception {
        val writer = new W3CDOMStreamWriter();
        writer.writeStartElement("wst", "Claims", STSUtils.WST_NS_05_12);
        writer.writeNamespace("wst", STSUtils.WST_NS_05_12);
        writer.writeNamespace("ic", WSFederationConstants.HTTP_SCHEMAS_XMLSOAP_ORG_WS_2005_05_IDENTITY);
        writer.writeAttribute("Dialect", WSFederationConstants.HTTP_SCHEMAS_XMLSOAP_ORG_WS_2005_05_IDENTITY);

        writeAttributes(writer, assertion, service);

        writer.writeEndElement();
        val claims = writer.getDocument().getDocumentElement();
        sts.setClaims(claims);
    }

    protected void writeAttributes(final XMLStreamWriter writer,
                                   final TicketValidationResult assertion,
                                   final WSFederationRegisteredService service) {
        attributeWriter.write(writer, assertion.getPrincipal(), service);
    }

    private Element requestSecurityTokenResponse(final WSFederationRegisteredService service,
                                                 final SecurityTokenServiceClient sts,
                                                 final TicketValidationResult assertion) {
        try {
            val properties = sts.getProperties();
            properties.put(SecurityConstants.USERNAME, assertion.getPrincipal().getId());
            val uid = credentialCipherExecutor.encode(assertion.getPrincipal().getId());
            properties.put(SecurityConstants.PASSWORD, uid);
            return FunctionUtils.doUnchecked(() -> sts.requestSecurityTokenResponse(service.getAppliesTo()));
        } catch (final SoapFault ex) {
            if (ex.getFaultCode() != null && "RequestFailed".equals(ex.getFaultCode().getLocalPart())) {
                throw new IllegalArgumentException(new ProcessingException(ProcessingException.TYPE.BAD_REQUEST));
            }
            LoggingUtils.error(LOGGER, ex);
            throw ex;
        }
    }
}
