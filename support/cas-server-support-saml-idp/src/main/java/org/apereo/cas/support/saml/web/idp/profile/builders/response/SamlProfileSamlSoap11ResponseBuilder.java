package org.apereo.cas.support.saml.web.idp.profile.builders.response;

import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The {@link SamlProfileSamlSoap11ResponseBuilder} is responsible for
 * building the final SAML assertion for the relying party.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class SamlProfileSamlSoap11ResponseBuilder extends BaseSamlProfileSamlResponseBuilder {
    private static final long serialVersionUID = -1875903354216171261L;

    @Override
    protected SAMLObject buildResponse(final Assertion assertion,
                                       final AuthnRequest authnRequest,
                                       final SamlRegisteredService service,
                                       final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                       final HttpServletRequest request,
                                       final HttpServletResponse response) throws SamlException {
        slkdjaksdjlkasd
                Maybe move stuff from the saml2 response to abstract and use that?
        return null;
    }

    /*
    @Override
    protected Response encode(final SamlRegisteredService service,
                              final Response samlResponse,
                              final HttpServletResponse httpResponse,
                              final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                              final String relayState) throws SamlException {
        try {
            final HTTPSOAP11Encoder encoder = new HTTPSOAP11Encoder();
            final MessageContext result = new MessageContext();
            result.setMessage(...);
            encoder.setHttpServletResponse(httpResponse);
            encoder.setMessageContext(result);
            encoder.initialize();
            encoder.encode();
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }
    */

}
