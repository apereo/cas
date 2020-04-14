package org.apereo.cas.support.saml.web.idp.profile.sso;

import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.web.idp.profile.AbstractSamlIdPProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.SamlProfileHandlerConfigurationContext;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.opensaml.messaging.decoder.servlet.BaseHttpServletRequestXMLMessageDecoder;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The {@link SSOSamlIdPPostProfileHandlerController} is responsible for
 * handling profile requests for SAML2 Web SSO.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class SSOSamlIdPPostProfileHandlerController extends AbstractSamlIdPProfileHandlerController {
    public SSOSamlIdPPostProfileHandlerController(final SamlProfileHandlerConfigurationContext samlProfileHandlerConfigurationContext) {
        super(samlProfileHandlerConfigurationContext);
    }

    /**
     * Handle SSO GET profile redirect request.
     *
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    @GetMapping(path = SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_REDIRECT)
    public void handleSaml2ProfileSsoRedirectRequest(final HttpServletResponse response,
                                                     final HttpServletRequest request) throws Exception {
        val decoder = getSamlProfileHandlerConfigurationContext().getSamlMessageDecoders().getInstance(HttpMethod.GET);
        handleSsoPostProfileRequest(response, request, decoder);
    }

    /**
     * Handle SSO HEAD profile redirect request (not allowed).
     *
     * @param response the response
     * @param request  the request
     */
    @RequestMapping(path = SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_REDIRECT, method = RequestMethod.HEAD)
    public void handleSaml2ProfileSsoRedirectHeadRequest(final HttpServletResponse response,
                                                         final HttpServletRequest request) {
        LOGGER.info("Endpoint [{}] called with HTTP HEAD returning 400 Bad Request", SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_REDIRECT);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    /**
     * Handle SSO POST profile request.
     *
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    @PostMapping(path = SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_POST)
    public void handleSaml2ProfileSsoPostRequest(final HttpServletResponse response,
                                                 final HttpServletRequest request) throws Exception {
        val decoder = getSamlProfileHandlerConfigurationContext().getSamlMessageDecoders().getInstance(HttpMethod.POST);
        handleSsoPostProfileRequest(response, request, decoder);
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
        val authnRequest = getSamlProfileHandlerConfigurationContext().getSamlHttpRequestExtractor()
            .extract(request, decoder, AuthnRequest.class);
        initiateAuthenticationRequest(authnRequest, response, request);
    }

}
