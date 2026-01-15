package org.apereo.cas.oidc.authn;

import module java.base;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20ClientAuthenticationMethods;
import org.apereo.cas.support.oauth.authenticator.OAuth20X509Authenticator;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import lombok.val;
import org.pac4j.core.context.CallContext;

/**
 * This is {@link OidcX509Authenticator}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class OidcX509Authenticator extends OAuth20X509Authenticator {
    private final OidcServerDiscoverySettings oidcServerDiscoverySettings;

    public OidcX509Authenticator(final ServicesManager servicesManager,
                                 final OAuth20RequestParameterResolver requestParameterResolver,
                                 final OidcServerDiscoverySettings oidcServerDiscoverySettings) {
        super(servicesManager, requestParameterResolver);
        this.oidcServerDiscoverySettings = oidcServerDiscoverySettings;
    }

    @Override
    protected boolean isAuthenticationMethodSupported(final CallContext ctx, final OAuthRegisteredService registeredService) {
        val authMethodSupported = oidcServerDiscoverySettings.getTokenEndpointAuthMethodsSupported()
            .stream()
            .map(OAuth20ClientAuthenticationMethods::parse)
            .anyMatch(method -> method == OAuth20ClientAuthenticationMethods.TLS_CLIENT_AUTH);
        return super.isAuthenticationMethodSupported(ctx, registeredService) && authMethodSupported;
    }
}
