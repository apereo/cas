package org.apereo.cas.support.saml.web.idp.profile;

import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.util.Pair;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.decoder.servlet.BaseHttpServletRequestXMLMessageDecoder;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPPostDecoder;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPRedirectDeflateDecoder;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The {@link SSOPostProfileHandlerController} is responsible for
 * handling profile requests for SAML2 Web SSO.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SSOPostProfileHandlerController extends AbstractSamlProfileHandlerController {

    /**
     * Instantiates a new redirect profile handler controller.
     */
    public SSOPostProfileHandlerController() {
    }


    /**
     * Handle SSO POST profile request.
     *
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    @RequestMapping(path = SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_REDIRECT, method = RequestMethod.GET)
    protected void handleSaml2ProfileSsoRedirectRequest(final HttpServletResponse response,
                                                        final HttpServletRequest request) throws Exception {
        handleSsoPostProfileRequest(response, request, new HTTPRedirectDeflateDecoder());
    }

    /**
     * Handle SSO POST profile request.
     *
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    @RequestMapping(path = SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_POST, method = RequestMethod.POST)
    protected void handleSaml2ProfileSsoPostRequest(final HttpServletResponse response,
                                                    final HttpServletRequest request) throws Exception {
        handleSsoPostProfileRequest(response, request, new HTTPPostDecoder());
    }

    /**
     * Handle profile request.
     *
     * @param response the response
     * @param request  the request
     * @param decoder  the decoder
     * @throws Exception the exception
     */
    protected void handleSsoPostProfileRequest(final HttpServletResponse response,
                                               final HttpServletRequest request,
                                               final BaseHttpServletRequestXMLMessageDecoder decoder) throws Exception {
        final Pair<? extends SignableSAMLObject, MessageContext> authnRequest = retrieveAuthnRequest(request, decoder);
        initiateAuthenticationRequest(authnRequest, response, request);
    }
    
    /**
     * Retrieve authn request.
     *
     * @param request the request
     * @param decoder the decoder
     * @return the authn request
     */
    protected Pair<? extends SignableSAMLObject, MessageContext> retrieveAuthnRequest(final HttpServletRequest request, 
                                                                                final BaseHttpServletRequestXMLMessageDecoder decoder) {
        return decodeRequest(request, decoder, AuthnRequest.class);
    }

}
