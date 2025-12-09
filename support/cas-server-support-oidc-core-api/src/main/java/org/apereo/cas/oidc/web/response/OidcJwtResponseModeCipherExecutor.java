package org.apereo.cas.oidc.web.response;

import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyCacheKey;
import org.apereo.cas.oidc.token.BaseOidcJwtCipherExecutor;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.jose4j.jwk.JsonWebKeySet;
import org.jspecify.annotations.NonNull;

/**
 * This is {@link OidcJwtResponseModeCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public class OidcJwtResponseModeCipherExecutor extends BaseOidcJwtCipherExecutor {
    public OidcJwtResponseModeCipherExecutor(final LoadingCache<@NonNull OidcJsonWebKeyCacheKey, JsonWebKeySet> defaultJsonWebKeystoreCache,
                                             final OidcIssuerService oidcIssuerService) {
        super(defaultJsonWebKeystoreCache, oidcIssuerService);
    }

    @Override
    public String getName() {
        return "OpenID Connect Response Mode JWT";
    }
}

