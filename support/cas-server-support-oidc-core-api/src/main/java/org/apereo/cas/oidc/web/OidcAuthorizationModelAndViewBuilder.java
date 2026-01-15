package org.apereo.cas.oidc.web;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.response.callback.DefaultOAuth20AuthorizationModelAndViewBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20ResponseModeFactory;
import lombok.val;
import org.apache.hc.core5.net.URIBuilder;

/**
 * This is {@link OidcAuthorizationModelAndViewBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */

public class OidcAuthorizationModelAndViewBuilder extends DefaultOAuth20AuthorizationModelAndViewBuilder {
    private final OidcIssuerService issuerService;

    private final CasConfigurationProperties casProperties;

    public OidcAuthorizationModelAndViewBuilder(final OAuth20ResponseModeFactory responseModeFactory,
                                                final OidcIssuerService issuerService,
                                                final CasConfigurationProperties casProperties) {
        super(responseModeFactory);
        this.issuerService = issuerService;
        this.casProperties = casProperties;
    }


    @Override
    protected String prepareRedirectUrl(final OAuthRegisteredService registeredService,
                                        final String redirectUrl, final Map<String, String> parameters) throws Exception {
        val discovery = casProperties.getAuthn().getOidc().getDiscovery();
        if (registeredService instanceof final OidcRegisteredService oidcService && discovery.isAuthorizationResponseIssuerParameterSupported()) {
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
