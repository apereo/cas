package org.apereo.cas.oidc.web;

import module java.base;
import org.apereo.cas.support.oauth.web.DefaultOAuth20RequestParameterResolver;
import org.apereo.cas.token.JwtBuilder;
import org.pac4j.core.context.WebContext;

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
    protected List<String> getSupportedScopes(final WebContext context) {
        return jwtBuilder.getCasProperties().getAuthn().getOidc().getDiscovery().getScopes();
    }

    @Override
    protected List<String> getResponseTypesSupported(final WebContext context) {
        return jwtBuilder.getCasProperties().getAuthn().getOidc().getDiscovery().getResponseTypesSupported();
    }

    @Override
    protected List<String> getGrantTypesSupported(final WebContext context) {
        return jwtBuilder.getCasProperties().getAuthn().getOidc().getDiscovery().getGrantTypesSupported();
    }

    @Override
    protected List<String> getResponseModesSupported(final WebContext context) {
        return jwtBuilder.getCasProperties().getAuthn().getOidc().getDiscovery().getResponseModesSupported();
    }

    @Override
    protected boolean isRequestParameterSupported(final WebContext context) {
        return jwtBuilder.getCasProperties().getAuthn().getOidc().getDiscovery().isRequestParameterSupported();
    }

    @Override
    protected boolean isClaimsParameterSupported(final WebContext context) {
        return jwtBuilder.getCasProperties().getAuthn().getOidc().getDiscovery().isClaimsParameterSupported();
    }

    @Override
    protected List<String> getPromptValuesSupported() {
        return jwtBuilder.getCasProperties().getAuthn().getOidc().getDiscovery().getPromptValuesSupported();
    }
}
