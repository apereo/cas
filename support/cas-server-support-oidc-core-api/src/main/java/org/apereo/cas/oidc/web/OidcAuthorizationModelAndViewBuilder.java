package org.apereo.cas.oidc.web;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.response.callback.DefaultOAuth20AuthorizationModelAndViewBuilder;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.http.client.utils.URIBuilder;

import java.util.Map;
import java.util.Optional;

/**
 * This is {@link OidcAuthorizationModelAndViewBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiredArgsConstructor
public class OidcAuthorizationModelAndViewBuilder extends DefaultOAuth20AuthorizationModelAndViewBuilder {
    private final OidcIssuerService issuerService;

    private final CasConfigurationProperties casProperties;

    @Override
    protected String prepareRedirectUrl(final OAuthRegisteredService registeredService,
                                        final String redirectUrl, final Map<String, String> parameters) throws Exception {
        val discovery = casProperties.getAuthn().getOidc().getDiscovery();
        if (registeredService instanceof OidcRegisteredService && discovery.isAuthorizationResponseIssuerParameterSupported()) {
            val oidcService = (OidcRegisteredService) registeredService;
            val issuer = issuerService.determineIssuer(Optional.of(oidcService));
            parameters.put(OidcConstants.ISS, issuer);
            return new URIBuilder(redirectUrl)
                .addParameter(OidcConstants.ISS, issuer)
                .build()
                .toString();
        }
        return super.prepareRedirectUrl(registeredService, redirectUrl, parameters);
    }
}
