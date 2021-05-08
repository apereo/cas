package org.apereo.cas.support.saml.web.idp.profile.sso;

import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.web.idp.profile.AbstractSamlIdPProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.SamlProfileHandlerConfigurationContext;

import lombok.val;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

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
    public SSOSamlIdPPostSimpleSignProfileHandlerController(final SamlProfileHandlerConfigurationContext ctx) {
        super(ctx);
    }

    /**
     * Handle SSO POST profile request.
     *
     * @param response the response
     * @param request  the request
     * @return the model and view
     * @throws Exception the exception
     */
    @GetMapping(path = SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_POST_SIMPLE_SIGN)
    protected ModelAndView handleSaml2ProfileSsoRedirectRequest(final HttpServletResponse response,
                                                                final HttpServletRequest request) throws Exception {
        val decoder = getConfigurationContext().getSamlMessageDecoders().getInstance(HttpMethod.GET);
        return handleSsoPostProfileRequest(response, request, decoder);
    }

    /**
     * Handle SSO POST profile request.
     *
     * @param response the response
     * @param request  the request
     * @return the model and view
     * @throws Exception the exception
     */
    @PostMapping(path = SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_POST_SIMPLE_SIGN)
    protected ModelAndView handleSaml2ProfileSsoPostRequest(final HttpServletResponse response,
                                                            final HttpServletRequest request) throws Exception {
        val decoder = getConfigurationContext().getSamlMessageDecoders().getInstance(HttpMethod.POST);
        return handleSsoPostProfileRequest(response, request, decoder);
    }
}
