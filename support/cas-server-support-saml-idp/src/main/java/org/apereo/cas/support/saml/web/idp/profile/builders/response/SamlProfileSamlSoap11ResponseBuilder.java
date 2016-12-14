package org.apereo.cas.support.saml.web.idp.profile.builders.response;

import com.google.common.base.Throwables;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPSOAP11Encoder;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.ecp.Response;
import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.soap.soap11.Header;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The {@link SamlProfileSamlSoap11ResponseBuilder} is responsible for
 * building the final SAML assertion for the relying party.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class SamlProfileSamlSoap11ResponseBuilder extends BaseSamlProfileSamlResponseBuilder<Response> {
    private static final long serialVersionUID = -1875903354216171261L;

    @Override
    protected Response buildResponse(final Assertion assertion,
                                     final AuthnRequest authnRequest,
                                     final SamlRegisteredService service,
                                     final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                     final HttpServletRequest request,
                                     final HttpServletResponse response) throws SamlException {
        final Response samlResponse = newEcpResponse(adaptor.getAssertionConsumerService().getLocation());
        final Header header = newSoapObject(Header.class);
        final Body body = newSoapObject(Body.class);
        body.getUnknownXMLObjects().add(samlResponse);
        final Envelope envelope = newSoapObject(Envelope.class);
        envelope.setHeader(header);
        envelope.setBody(body);
        return samlResponse;
    }

    @Override
    protected Response encode(final SamlRegisteredService service,
                              final Response samlResponse,
                              final HttpServletResponse httpResponse,
                              final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                              final String relayState) throws SamlException {
        try {


            final HTTPSOAP11Encoder encoder = new HTTPSOAP11Encoder();
            final MessageContext result = new MessageContext();
            result.setMessage(samlResponse);
            encoder.setHttpServletResponse(httpResponse);
            encoder.setMessageContext(result);
            encoder.initialize();
            encoder.encode();
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
        return samlResponse;
    }
}
