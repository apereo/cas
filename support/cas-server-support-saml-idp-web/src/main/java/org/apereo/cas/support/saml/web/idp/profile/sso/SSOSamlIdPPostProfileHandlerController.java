package org.apereo.cas.support.saml.web.idp.profile.sso;

import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.web.idp.profile.AbstractSamlIdPProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.SamlProfileHandlerConfigurationContext;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

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
    public SSOSamlIdPPostProfileHandlerController(final SamlProfileHandlerConfigurationContext ctx) {
        super(ctx);
    }

    /**
     * Handle SSO GET profile redirect request.
     *
     * @param response the response
     * @param request  the request
     * @return the model and view
     * @throws Exception the exception
     */
    @GetMapping(path = SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_REDIRECT)
    public ModelAndView handleSaml2ProfileSsoRedirectRequest(final HttpServletResponse response,
                                                             final HttpServletRequest request) throws Exception {
        val decoder = getConfigurationContext().getSamlMessageDecoders().getInstance(HttpMethod.GET);
        return handleSsoPostProfileRequest(response, request, decoder);
    }

    /**
     * Handle SSO HEAD profile redirect request (not allowed).
     *
     * @param response the response
     * @param request  the request
     * @return the model and view
     */
    @RequestMapping(path = SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_REDIRECT, method = RequestMethod.HEAD)
    public ModelAndView handleSaml2ProfileSsoRedirectHeadRequest(final HttpServletResponse response,
                                                                 final HttpServletRequest request) {
        LOGGER.info("Endpoint [{}] called with HTTP HEAD returning Bad Request", SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_REDIRECT);
        return WebUtils.produceErrorView(new IllegalArgumentException("Unable to handle request type"));
    }

    /**
     * Handle SSO POST profile request.
     *
     * @param response the response
     * @param request  the request
     * @return the model and view
     * @throws Exception the exception
     */
    @PostMapping(path = SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_POST)
    public ModelAndView handleSaml2ProfileSsoPostRequest(final HttpServletResponse response,
                                                         final HttpServletRequest request) throws Exception {
        val decoder = getConfigurationContext().getSamlMessageDecoders().getInstance(HttpMethod.POST);
        return handleSsoPostProfileRequest(response, request, decoder);
    }
}
