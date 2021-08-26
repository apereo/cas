package org.apereo.cas.oidc.web.controllers.token;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20AccessTokenEndpointController;

import lombok.val;
import org.pac4j.core.context.JEEContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link OidcAccessTokenEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OidcAccessTokenEndpointController extends OAuth20AccessTokenEndpointController<OidcConfigurationContext> {

    public OidcAccessTokenEndpointController(final OidcConfigurationContext oauthConfigurationContext,
                                             final AuditableExecution accessTokenGrantAuditableRequestExtractor) {
        super(oauthConfigurationContext, accessTokenGrantAuditableRequestExtractor);
    }

    @PostMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.ACCESS_TOKEN_URL,
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.TOKEN_URL,
        "/**/" + OidcConstants.ACCESS_TOKEN_URL,
        "/**/" + OidcConstants.TOKEN_URL
    })
    @Override
    public ModelAndView handleRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        val webContext = new JEEContext(request, response);
        if (!getConfigurationContext().getOidcRequestSupport().isValidIssuerForEndpoint(webContext, OidcConstants.ACCESS_TOKEN_URL)
            && !getConfigurationContext().getOidcRequestSupport().isValidIssuerForEndpoint(webContext, OidcConstants.TOKEN_URL)) {
            return OAuth20Utils.produceUnauthorizedErrorView(HttpStatus.NOT_FOUND);
        }
        return super.handleRequest(request, response);
    }

    @GetMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.ACCESS_TOKEN_URL,
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.TOKEN_URL,
        "/**/" + OidcConstants.ACCESS_TOKEN_URL,
        "/**/" + OidcConstants.TOKEN_URL
    })
    @Override
    public ModelAndView handleGetRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return this.handleRequest(request, response);
    }
}
