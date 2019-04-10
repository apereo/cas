package org.apereo.cas.oidc.web.controllers.authorize;

import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20AuthorizeEndpointController;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link OidcAuthorizeEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class OidcAuthorizeEndpointController extends OAuth20AuthorizeEndpointController {
    public OidcAuthorizeEndpointController(final OAuth20ConfigurationContext oAuthConfigurationContext) {
        super(oAuthConfigurationContext);
    }

    @GetMapping(value = '/' + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.AUTHORIZE_URL)
    @Override
    public ModelAndView handleRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        val scopes = OAuth20Utils.getRequestedScopes(request);
        if (scopes.isEmpty() || !scopes.contains(OidcConstants.StandardScopes.OPENID.getScope())) {
            LOGGER.warn("Provided scopes [{}] are undefined by OpenID Connect, which requires that scope [{}] MUST be specified, "
                    + "or the behavior is unspecified. CAS MAY allow this request to be processed for now.",
                scopes, OidcConstants.StandardScopes.OPENID.getScope());
        }

        return super.handleRequest(request, response);
    }

    @PostMapping(value = '/' + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.AUTHORIZE_URL)
    @Override
    public ModelAndView handleRequestPost(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return handleRequest(request, response);
    }
}
