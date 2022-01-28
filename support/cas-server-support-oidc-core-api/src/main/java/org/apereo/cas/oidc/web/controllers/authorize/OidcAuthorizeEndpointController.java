package org.apereo.cas.oidc.web.controllers.authorize;

import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20AuthorizeEndpointController;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.JEEContext;
import org.springframework.http.HttpStatus;
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
public class OidcAuthorizeEndpointController extends OAuth20AuthorizeEndpointController<OidcConfigurationContext> {
    public OidcAuthorizeEndpointController(final OidcConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @GetMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.AUTHORIZE_URL,
        "/**/" + OidcConstants.AUTHORIZE_URL
    })
    @Override
    public ModelAndView handleRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        val webContext = new JEEContext(request, response);
        if (!getConfigurationContext().getOidcRequestSupport().isValidIssuerForEndpoint(webContext, OidcConstants.AUTHORIZE_URL)) {
            return OAuth20Utils.produceUnauthorizedErrorView(HttpStatus.NOT_FOUND);
        }

        if (getConfigurationContext().getDiscoverySettings().isRequirePushedAuthorizationRequests()
            && webContext.getRequestURL().endsWith(OidcConstants.AUTHORIZE_URL)
            && StringUtils.isBlank(request.getParameter(OidcConstants.REQUEST_URI))) {
            LOGGER.warn("CAS is configured to only accept pushed authorization requests");
            return OAuth20Utils.produceUnauthorizedErrorView(HttpStatus.FORBIDDEN);
        }

        val scopes = OAuth20Utils.getRequestedScopes(webContext);
        if (scopes.isEmpty() || !scopes.contains(OidcConstants.StandardScopes.OPENID.getScope())) {
            LOGGER.warn("Provided scopes [{}] are undefined by OpenID Connect, which requires that scope [{}] MUST be specified, "
                        + "or the behavior is unspecified. CAS MAY allow this request to be processed for now.",
                scopes, OidcConstants.StandardScopes.OPENID.getScope());
        }
        return super.handleRequest(request, response);
    }

    @PostMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.AUTHORIZE_URL,
        "/**/" + OidcConstants.AUTHORIZE_URL
    })
    @Override
    public ModelAndView handleRequestPost(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return handleRequest(request, response);
    }
}
