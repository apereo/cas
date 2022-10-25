package org.apereo.cas.oidc.token;

import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyCacheKey;

import com.github.benmanes.caffeine.cache.LoadingCache;
import org.jose4j.jwk.JsonWebKeySet;

import java.util.Optional;

/**
 * This is {@link OidcRegisteredServiceJwtCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public interface OidcRegisteredServiceJwtCipherExecutor {

    LoadingCache<OidcJsonWebKeyCacheKey, Optional<JsonWebKeySet>> getDefaultJsonWebKeystoreCache();

    LoadingCache<OidcJsonWebKeyCacheKey, Optional<JsonWebKeySet>> getRegisteredServiceJsonWebKeystoreCache();

    OidcIssuerService getOidcIssuerService();
}
