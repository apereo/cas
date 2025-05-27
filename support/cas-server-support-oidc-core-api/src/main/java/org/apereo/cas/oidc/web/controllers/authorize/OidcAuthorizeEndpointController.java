package org.apereo.cas.oidc.web.controllers.authorize;

import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20AuthorizeEndpointController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * This is {@link OidcAuthorizeEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Tag(name = "OpenID Connect")
public class OidcAuthorizeEndpointController extends OAuth20AuthorizeEndpointController<OidcConfigurationContext> {
    public OidcAuthorizeEndpointController(final OidcConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @GetMapping({
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.AUTHORIZE_URL,
        "/**/" + OidcConstants.AUTHORIZE_URL
    })
    @Operation(summary = "Handle OIDC authorization request")
    @Override
    public ModelAndView handleRequest(final HttpServletRequest request, final HttpServletResponse response) throws Throwable {
        val webContext = new JEEContext(request, response);
        if (!getConfigurationContext().getIssuerService().validateIssuer(webContext, List.of(OidcConstants.AUTHORIZE_URL, OAuth20Constants.AUTHORIZE_URL))) {
            LOGGER.warn("CAS cannot accept the authorization request given the issuer is invalid.");
            return OAuth20Utils.writeError(response, OAuth20Constants.INVALID_REQUEST, "Invalid issuer");
        }

        if (getConfigurationContext().getDiscoverySettings().isRequirePushedAuthorizationRequests()
            && webContext.getRequestURL().endsWith(OidcConstants.AUTHORIZE_URL)
            && StringUtils.isBlank(request.getParameter(OidcConstants.REQUEST_URI))) {
            LOGGER.warn("CAS is configured to only accept pushed authorization requests");
            return OAuth20Utils.produceUnauthorizedErrorView(HttpStatus.FORBIDDEN);
        }

        val scopes = getConfigurationContext().getRequestParameterResolver().resolveRequestedScopes(webContext);
        if (scopes.isEmpty() || !scopes.contains(OidcConstants.StandardScopes.OPENID.getScope())) {
            LOGGER.warn("Provided scopes [{}] are undefined by OpenID Connect, which requires that scope [{}] MUST be specified, "
                        + "or the behavior is unspecified. CAS MAY allow this request to be processed for now.",
                scopes, OidcConstants.StandardScopes.OPENID.getScope());
        }
        return super.handleRequest(request, response);
    }

    @PostMapping({
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.AUTHORIZE_URL,
        "/**/" + OidcConstants.AUTHORIZE_URL
    })
    @Override
    @Operation(summary = "Handle OIDC authorization request")
    public ModelAndView handleRequestPost(final HttpServletRequest request,
                                          final HttpServletResponse response) throws Throwable {
        return handleRequest(request, response);
    }
}
