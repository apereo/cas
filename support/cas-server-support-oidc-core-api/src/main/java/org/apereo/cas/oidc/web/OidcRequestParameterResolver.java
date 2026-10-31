package org.apereo.cas.oidc.web;

import module java.base;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettingsFactory;
import org.apereo.cas.support.oauth.web.DefaultOAuth20RequestParameterResolver;
import org.apereo.cas.token.JwtBuilder;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link OidcRequestParameterResolver}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public class OidcRequestParameterResolver extends DefaultOAuth20RequestParameterResolver {
    public OidcRequestParameterResolver(final JwtBuilder jwtBuilder) {
        super(jwtBuilder);
    }

    @Override
    protected List<String> getSupportedScopes(final HttpServletRequest request) {
        return OidcServerDiscoverySettingsFactory.resolveScopesSupported(jwtBuilder.getCasProperties());
    }

    @Override
    protected List<String> getResponseTypesSupported(final HttpServletRequest request) {
        return jwtBuilder.getCasProperties().getAuthn().getOidc().getDiscovery().getResponseTypesSupported();
    }

    @Override
    protected List<String> getGrantTypesSupported(final HttpServletRequest request) {
        return jwtBuilder.getCasProperties().getAuthn().getOidc().getDiscovery().getGrantTypesSupported();
    }

    @Override
    protected List<String> getResponseModesSupported(final HttpServletRequest request) {
        return jwtBuilder.getCasProperties().getAuthn().getOidc().getDiscovery().getResponseModesSupported();
    }

    @Override
    protected boolean isRequestParameterSupported(final HttpServletRequest request) {
        return jwtBuilder.getCasProperties().getAuthn().getOidc().getDiscovery().isRequestParameterSupported();
    }

    @Override
    protected boolean isClaimsParameterSupported(final HttpServletRequest request) {
        return jwtBuilder.getCasProperties().getAuthn().getOidc().getDiscovery().isClaimsParameterSupported();
    }

    @Override
    protected List<String> getPromptValuesSupported() {
        return jwtBuilder.getCasProperties().getAuthn().getOidc().getDiscovery().getPromptValuesSupported();
    }
}
