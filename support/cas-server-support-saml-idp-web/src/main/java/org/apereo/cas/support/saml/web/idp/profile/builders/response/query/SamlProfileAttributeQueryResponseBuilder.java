package org.apereo.cas.support.saml.web.idp.profile.builders.response.query;

import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.SamlProfileSamlResponseBuilderConfigurationContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.soap.SamlProfileSamlSoap11ResponseBuilder;

import lombok.val;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.soap.soap11.Header;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link SamlProfileAttributeQueryResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SamlProfileAttributeQueryResponseBuilder extends SamlProfileSamlSoap11ResponseBuilder {
    private static final long serialVersionUID = -5582616946993706815L;

    public SamlProfileAttributeQueryResponseBuilder(final SamlProfileSamlResponseBuilderConfigurationContext samlResponseBuilderConfigurationContext) {
        super(samlResponseBuilderConfigurationContext);
    }

    @Override
    public Envelope build(final RequestAbstractType authnRequest, final HttpServletRequest request,
                          final HttpServletResponse response, final Object casAssertion, final SamlRegisteredService service,
                          final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                          final String binding,
                          final MessageContext messageContext) throws SamlException {
        val header = newSoapObject(Header.class);
        val body = newSoapObject(Body.class);
        val query = (AttributeQuery) authnRequest;
        val saml2Response = buildSaml2Response(casAssertion, query, service,
            adaptor, request, SAMLConstants.SAML2_POST_BINDING_URI, messageContext);
        body.getUnknownXMLObjects().add(saml2Response);

        val envelope = newSoapObject(Envelope.class);
        envelope.setHeader(header);
        envelope.setBody(body);
        SamlUtils.logSamlObject(this.openSamlConfigBean, envelope);

        return encodeFinalResponse(request, response, service, adaptor, envelope,
            binding, authnRequest, casAssertion, messageContext);
    }
}
