package org.apereo.cas.oidc.web.controllers.authorize;

import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;

import lombok.val;
import org.pac4j.core.context.JEEContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link OidcPushedAuthorizeEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OidcPushedAuthorizeEndpointController extends OidcAuthorizeEndpointController {
    public OidcPushedAuthorizeEndpointController(final OidcConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @Override
    @GetMapping(value = "/**/" + OidcConstants.PUSHED_AUTHORIZE_URL)
    public ModelAndView handleRequest(final HttpServletRequest request,
                                      final HttpServletResponse response) {
        return OAuth20Utils.produceUnauthorizedErrorView(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Override
    @PostMapping(value = "/**/" + OidcConstants.PUSHED_AUTHORIZE_URL)
    public ModelAndView handleRequestPost(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        val webContext = new JEEContext(request, response);
        if (!getConfigurationContext().getOidcRequestSupport().isValidIssuerForEndpoint(webContext, OidcConstants.PUSHED_AUTHORIZE_URL)) {
            return OAuth20Utils.produceUnauthorizedErrorView(HttpStatus.NOT_FOUND);
        }
        return super.handleRequest(request, response);
    }
}
