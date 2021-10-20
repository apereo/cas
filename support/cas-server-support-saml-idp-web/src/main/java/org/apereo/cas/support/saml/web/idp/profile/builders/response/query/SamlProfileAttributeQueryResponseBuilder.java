package org.apereo.cas.support.saml.web.idp.profile.builders.response.query;

import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.web.idp.profile.builders.AuthenticatedAssertionContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.SamlProfileSamlResponseBuilderConfigurationContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.soap.SamlProfileSamlSoap11ResponseBuilder;

import lombok.val;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.context.ScratchContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.soap.soap11.Header;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Objects;

/**
 * This is {@link SamlProfileAttributeQueryResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SamlProfileAttributeQueryResponseBuilder extends SamlProfileSamlSoap11ResponseBuilder {
    private static final long serialVersionUID = -5582616946993706815L;

    public SamlProfileAttributeQueryResponseBuilder(final SamlProfileSamlResponseBuilderConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @Override
    public Envelope build(final RequestAbstractType authnRequest,
                          final HttpServletRequest request,
                          final HttpServletResponse response,
                          final AuthenticatedAssertionContext casAssertion,
                          final SamlRegisteredService service,
                          final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                          final String binding,
                          final MessageContext messageContext) throws SamlException {

        response.setContentType(MediaType.APPLICATION_XML_VALUE);
        val header = SamlUtils.newSoapObject(Header.class);
        val body = SamlUtils.newSoapObject(Body.class);
        val query = (AttributeQuery) authnRequest;

        val scratch = messageContext.getSubcontext(ScratchContext.class, true);
        val map = (Map) Objects.requireNonNull(scratch).getMap();
        map.put(SamlProtocolConstants.PARAMETER_ENCODE_RESPONSE, Boolean.FALSE);
        val saml2Response = buildSaml2Response(casAssertion, query, service,
            adaptor, request, response, SAMLConstants.SAML2_POST_BINDING_URI, messageContext);
        body.getUnknownXMLObjects().add(saml2Response);

        val envelope = SamlUtils.newSoapObject(Envelope.class);
        envelope.setHeader(header);
        envelope.setBody(body);
        SamlUtils.logSamlObject(this.openSamlConfigBean, envelope);

        map.remove(SamlProtocolConstants.PARAMETER_ENCODE_RESPONSE);
        return encodeFinalResponse(request, response, service, adaptor, envelope,
            binding, authnRequest, casAssertion, messageContext);
    }
}
