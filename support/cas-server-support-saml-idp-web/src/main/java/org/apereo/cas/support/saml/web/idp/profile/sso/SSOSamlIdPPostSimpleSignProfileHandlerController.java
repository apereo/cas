package org.apereo.cas.support.saml.web.idp.profile.sso;

import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.web.idp.profile.AbstractSamlIdPProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.SamlProfileHandlerConfigurationContext;

import lombok.val;
import org.opensaml.messaging.decoder.servlet.BaseHttpServletRequestXMLMessageDecoder;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The {@link SSOSamlIdPPostSimpleSignProfileHandlerController} is responsible for
 * handling profile requests for SAML2 Web SSO SimpleSign.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SSOSamlIdPPostSimpleSignProfileHandlerController extends AbstractSamlIdPProfileHandlerController {
    public SSOSamlIdPPostSimpleSignProfileHandlerController(final SamlProfileHandlerConfigurationContext samlProfileHandlerConfigurationContext) {
        super(samlProfileHandlerConfigurationContext);
    }

    /**
     * Handle SSO POST profile request.
     *
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    @GetMapping(path = SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_POST_SIMPLE_SIGN)
    protected void handleSaml2ProfileSsoRedirectRequest(final HttpServletResponse response,
                                                        final HttpServletRequest request) throws Exception {
        val decoder = getSamlProfileHandlerConfigurationContext().getSamlMessageDecoders().getInstance(HttpMethod.GET);
        handleSsoPostProfileRequest(response, request, decoder);
    }

    /**
     * Handle SSO POST profile request.
     *
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    @PostMapping(path = SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_POST_SIMPLE_SIGN)
    protected void handleSaml2ProfileSsoPostRequest(final HttpServletResponse response,
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
