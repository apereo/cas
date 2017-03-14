package org.apereo.cas.ws.idp.services;

import com.google.common.base.Throwables;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.fediz.core.exception.ProcessingException;
import org.apache.cxf.fediz.core.util.DOMUtils;
import org.apache.cxf.rt.security.SecurityConstants;
import org.apache.cxf.staxutils.W3CDOMStreamWriter;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.apache.cxf.ws.security.trust.STSUtils;
import org.apache.wss4j.dom.WSConstants;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.SecurityTokenServiceClient;
import org.apereo.cas.ws.idp.IdentityProviderConfigurationService;
import org.apereo.cas.ws.idp.WSFederationConstants;
import org.apereo.cas.ws.idp.web.WSFederationRequest;
import org.jasig.cas.client.validation.Assertion;
import org.jooq.lambda.Unchecked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * This is {@link DefaultRelyingPartyTokenProducer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DefaultRelyingPartyTokenProducer implements WSFederationRelyingPartyTokenProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRelyingPartyTokenProducer.class);
    private static final String CERTIFICATE_REQUEST_ATTRIBUTE = "javax.servlet.request.X509Certificate";

    private final CipherExecutor<String, String> credentialCipherExecutor;
    private final IdentityProviderConfigurationService identityProviderConfigurationService;

    public DefaultRelyingPartyTokenProducer(final CipherExecutor<String, String> credentialCipherExecutor, 
                                            final IdentityProviderConfigurationService identityProviderConfigurationService) {
        this.credentialCipherExecutor = credentialCipherExecutor;
        this.identityProviderConfigurationService = identityProviderConfigurationService;
    }
    
    @Override
    public String produce(final SecurityToken securityToken, final WSFederationRegisteredService service,
                          final WSFederationRequest fedRequest, final HttpServletRequest servletRequest,
                          final Assertion assertion) {
        final Bus cxfBus = BusFactory.getDefaultBus();
        final SecurityTokenServiceClient sts = new SecurityTokenServiceClient(cxfBus);
        sts.setAddressingNamespace(StringUtils.defaultIfBlank(service.getAddressingNamespace(), WSFederationConstants.HTTP_WWW_W3_ORG_2005_08_ADDRESSING));

        final Pair<String, String> pair = prepareSecurityTokenServiceTokenKeyType(service, fedRequest, sts);
        handlePublicKeyTypeIfNecessary(servletRequest, sts, pair);

        //sts.setWsdlLocation("https://mmoayyed.unicon.net:8443/cas/ws/sts/REALMA/STSServiceTransport?wsdl");
        sts.setWsdlLocation(service.getWsdlLocation());

        final String namespace = StringUtils.defaultIfBlank(service.getNamespace(), WSFederationConstants.HTTP_DOCS_OASIS_OPEN_ORG_WS_SX_WS_TRUST_200512);
        sts.setServiceQName(new QName(namespace, service.getWsdlService()));
        sts.setEndpointQName(new QName(namespace, service.getWsdlEndpoint()));

        mapAttributesToRequestedClaims(service, sts);

        if (service.getLifetime() > 0) {
            sts.setEnableLifetime(true);
            sts.setTtl(Long.valueOf(service.getLifetime()).intValue());
        }
        sts.setEnableAppliesTo(StringUtils.isNotBlank(service.getAppliesTo()));
        sts.setOnBehalfOf(securityToken.getToken());

        final Element rpToken = requestSecurityTokenResponse(service, sts, assertion);
        return serializeRelyingPartyToken(rpToken);
    }

    private void handlePublicKeyTypeIfNecessary(final HttpServletRequest servletRequest, final SecurityTokenServiceClient sts,
                                                final Pair<String, String> pair) {
        if (WSFederationConstants.HTTP_DOCS_OASIS_OPEN_ORG_WS_SX_WS_TRUST_200512_PUBLICKEY.equals(pair.getValue())) {
            final X509Certificate[] certs = (X509Certificate[]) servletRequest.getAttribute(CERTIFICATE_REQUEST_ATTRIBUTE);
            if (certs != null && certs.length > 0) {
                sts.setUseCertificateForConfirmationKeyInfo(true);
                sts.setUseKeyCertificate(certs[0]);
            } else {
                LOGGER.info("Cannot send a PublicKey KeyType as no client certs are available");
                sts.setKeyType(WSFederationConstants.HTTP_DOCS_OASIS_OPEN_ORG_WS_SX_WS_TRUST_200512_BEARER);
            }
        }
    }

    private String serializeRelyingPartyToken(final Element rpToken) {
        final StringWriter sw = new StringWriter();
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.transform(new DOMSource(rpToken), new StreamResult(sw));
        } catch (final TransformerException e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return StringEscapeUtils.escapeXml11(sw.toString());
    }

    private void mapAttributesToRequestedClaims(final WSFederationRegisteredService service, final SecurityTokenServiceClient sts) {
        if (service.getAttributeReleasePolicy() instanceof WSFederationClaimsReleasePolicy) {
            final WSFederationClaimsReleasePolicy policy = (WSFederationClaimsReleasePolicy) service.getAttributeReleasePolicy();
            final Element claims = createClaimsElement(policy.getAllowedAttributes());
            if (claims != null) {
                sts.setClaims(claims);
            }
        }
    }

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
                throw new RuntimeException(new ProcessingException(ProcessingException.TYPE.BAD_REQUEST));
            }
            throw ex;
        } catch (final Exception ex) {
            throw Throwables.propagate(ex);
        }
    }

    private Element createClaimsElement(final List<String> realmClaims) {
        try {
            if (realmClaims == null || realmClaims.isEmpty()) {
                return null;
            }

            final W3CDOMStreamWriter writer = new W3CDOMStreamWriter();
            writer.writeStartElement("wst", "Claims", STSUtils.WST_NS_05_12);
            writer.writeNamespace("wst", STSUtils.WST_NS_05_12);
            writer.writeNamespace("ic", WSFederationConstants.HTTP_SCHEMAS_XMLSOAP_ORG_WS_2005_05_IDENTITY);
            writer.writeAttribute("Dialect", WSFederationConstants.HTTP_SCHEMAS_XMLSOAP_ORG_WS_2005_05_IDENTITY);

            realmClaims.forEach(Unchecked.consumer(a -> {
                LOGGER.debug("Requesting claim [{}]", a);
                writer.writeStartElement("ic", "ClaimType", WSFederationConstants.HTTP_SCHEMAS_XMLSOAP_ORG_WS_2005_05_IDENTITY);
                writer.writeAttribute("Uri", a);
                writer.writeAttribute("Optional", Boolean.TRUE.toString());
                writer.writeEndElement();
            }));

            writer.writeEndElement();
            return writer.getDocument().getDocumentElement();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    private Pair<String, String> prepareSecurityTokenServiceTokenKeyType(final WSFederationRegisteredService service,
                                                                         final WSFederationRequest fedRequest,
                                                                         final SecurityTokenServiceClient sts) {
        String stsTokenType = null;
        String stsKeyType = null;

        if (StringUtils.isNotBlank(fedRequest.getWreq())) {
            try {
                final Document wreqDoc = DOMUtils.readXml(new StringReader(fedRequest.getWreq()));
                final Element wreqElement = wreqDoc.getDocumentElement();
                if (wreqElement != null && "RequestSecurityToken".equals(wreqElement.getLocalName())
                        && (STSUtils.WST_NS_05_12.equals(wreqElement.getNamespaceURI())
                        || WSFederationConstants.HTTP_SCHEMAS_XMLSOAP_ORG_WS_2005_02_TRUST.equals(wreqElement.getNamespaceURI()))) {

                    final Element tokenTypeElement = DOMUtils.getFirstChildWithName(wreqElement, wreqElement.getNamespaceURI(), "TokenType");
                    if (tokenTypeElement != null) {
                        stsTokenType = tokenTypeElement.getTextContent();
                    }
                    final Element keyTypeElement = DOMUtils.getFirstChildWithName(wreqElement, wreqElement.getNamespaceURI(), "KeyType");
                    if (keyTypeElement != null) {
                        stsKeyType = keyTypeElement.getTextContent();
                    }
                }
            } catch (final Exception e) {
                LOGGER.warn("Error parsing {} parameter: [{}]", WSFederationConstants.WREQ, e.getMessage());
                throw new RuntimeException(new ProcessingException(ProcessingException.TYPE.BAD_REQUEST));
            }
        }

        if (StringUtils.isNotBlank(stsTokenType)) {
            sts.setTokenType(stsTokenType);
        } else {
            sts.setTokenType(StringUtils.defaultIfBlank(service.getTokenType(), WSConstants.WSS_SAML2_TOKEN_TYPE));
        }

        if (StringUtils.isNotBlank(service.getPolicyNamespace())) {
            sts.setWspNamespace(service.getPolicyNamespace());
        }
        sts.setKeyType(WSFederationConstants.HTTP_DOCS_OASIS_OPEN_ORG_WS_SX_WS_TRUST_200512_BEARER);
        return Pair.of(sts.getTokenType(), stsKeyType);
    }
}
