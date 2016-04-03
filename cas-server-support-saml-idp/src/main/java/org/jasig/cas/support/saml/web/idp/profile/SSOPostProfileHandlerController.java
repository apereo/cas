package org.jasig.cas.support.saml.web.idp.profile;

import org.jasig.cas.support.saml.SamlIdPConstants;
import org.jasig.cas.support.saml.SamlIdPUtils;
import org.jasig.cas.support.saml.services.SamlRegisteredService;
import org.jasig.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.opensaml.messaging.decoder.servlet.BaseHttpServletRequestXMLMessageDecoder;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPPostDecoder;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPRedirectDeflateDecoder;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.springframework.stereotype.Controller;
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
@Controller("ssoPostProfileHandlerController")
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
        final AuthnRequest authnRequest = decodeRequest(request, decoder, AuthnRequest.class);
        final AssertionConsumerService acs =
                SamlIdPUtils.getAssertionConsumerServiceFor(authnRequest, this.servicesManager, 
                        samlRegisteredServiceCachingMetadataResolver);
        final SamlRegisteredService registeredService = verifySamlRegisteredService(acs.getLocation());
        final SamlRegisteredServiceServiceProviderMetadataFacade adaptor =
                SamlRegisteredServiceServiceProviderMetadataFacade.get(this.samlRegisteredServiceCachingMetadataResolver,
                        registeredService, authnRequest);


        if (!authnRequest.isSigned()) {
            if (adaptor.isAuthnRequestsSigned()) {
                logger.error("Metadata for [{}] says authentication requests are signed, yet this authentication request is not",
                        adaptor.getEntityId());
                throw new SAMLException("AuthN request is not signed but should be");
            }
            logger.info("Authentication request is not signed, so there is no need to verify its signature.");
        } else {
            this.samlObjectSigner.verifySamlProfileRequestIfNeeded(authnRequest, adaptor.getMetadataResolver());
        }

        SamlIdPUtils.logSamlObject(this.configBean, authnRequest);
        issueAuthenticationRequestRedirect(authnRequest, request, response);
    }

}
