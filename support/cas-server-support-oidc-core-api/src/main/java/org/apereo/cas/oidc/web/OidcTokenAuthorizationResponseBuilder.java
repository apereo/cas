package org.apereo.cas.oidc.web;

import module java.base;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationModelAndViewBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20TokenAuthorizationResponseBuilder;
import lombok.val;

/**
 * This is {@link OidcTokenAuthorizationResponseBuilder}.
 * Access tokens returned from the authorization endpoint name the OpenID Connect issuer,
 * as the ID token and the token endpoint do.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class OidcTokenAuthorizationResponseBuilder<T extends OidcConfigurationContext> extends OAuth20TokenAuthorizationResponseBuilder<T> {
    public OidcTokenAuthorizationResponseBuilder(
        final T configurationContext,
        final OAuth20AuthorizationModelAndViewBuilder authorizationModelAndViewBuilder) {
        super(configurationContext, authorizationModelAndViewBuilder);
    }

    @Override
    protected String determineAccessTokenIssuer(final AccessTokenRequestContext tokenRequestContext) {
        val registeredService = Optional.ofNullable(tokenRequestContext.getRegisteredService())
            .filter(OidcRegisteredService.class::isInstance)
            .map(OidcRegisteredService.class::cast);
        return configurationContext.getIssuerService().determineIssuer(registeredService);
    }
}
