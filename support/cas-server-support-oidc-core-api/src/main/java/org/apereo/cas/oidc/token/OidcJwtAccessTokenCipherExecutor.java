package org.apereo.cas.oidc.token;

import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyCacheKey;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.jose4j.jwk.JsonWebKeySet;
import org.jspecify.annotations.NonNull;

/**
 * This is {@link OidcJwtAccessTokenCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public class OidcJwtAccessTokenCipherExecutor extends BaseOidcJwtCipherExecutor {

    public OidcJwtAccessTokenCipherExecutor(final LoadingCache<@NonNull OidcJsonWebKeyCacheKey, JsonWebKeySet> defaultJsonWebKeystoreCache,
                                            final OidcIssuerService oidcIssuerService) {
        super(defaultJsonWebKeystoreCache, oidcIssuerService);
    }

    @Override
    public String getName() {
        return "OpenID Connect JWT Access Tokens";
    }

}
