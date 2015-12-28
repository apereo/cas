package org.jasig.cas.support.saml.web.idp.profile;

import org.jasig.cas.support.saml.SamlProtocolConstants;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPPostDecoder;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPRedirectDeflateDecoder;
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
 * @since 4.3
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
    @RequestMapping(path = SamlProtocolConstants.ENDPOINT_SAML2_SSO_PROFILE_REDIRECT, method = RequestMethod.GET)
    protected void handleSaml2ProfileSsoRedirectRequest(final HttpServletResponse response,
                                                        final HttpServletRequest request) throws Exception {
        handleProfileRequest(response, request, new HTTPRedirectDeflateDecoder());
    }

    /**
     * Handle SSO POST profile request.
     *
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    @RequestMapping(path = SamlProtocolConstants.ENDPOINT_SAML2_SSO_PROFILE_POST, method = RequestMethod.POST)
    protected void handleSaml2ProfileSsoPostRequest(final HttpServletResponse response,
                                                    final HttpServletRequest request) throws Exception {
        handleProfileRequest(response, request, new HTTPPostDecoder());
    }

}
