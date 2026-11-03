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
    private final OidcRequestObjectDecrypter requestObjectDecrypter;

    public OidcRequestParameterResolver(final JwtBuilder jwtBuilder, final OidcRequestObjectDecrypter requestObjectDecrypter) {
        super(jwtBuilder);
        this.requestObjectDecrypter = requestObjectDecrypter;
    }

    /**
     * A request object encrypted to CAS is decrypted first; the nested JWT is then verified like any request object.
     *
     * @param requestObject the request object
     * @return the request object, decrypted
     */
    @Override
    protected String decryptRequestObject(final String requestObject) {
        return OidcRequestObjectDecrypter.isEncrypted(requestObject)
            ? requestObjectDecrypter.decrypt(requestObject)
            : requestObject;
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
