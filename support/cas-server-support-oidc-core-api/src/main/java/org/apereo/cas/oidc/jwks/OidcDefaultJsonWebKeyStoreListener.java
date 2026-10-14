package org.apereo.cas.oidc.jwks;

import module java.base;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreModifiedEvent;
import org.apereo.cas.oidc.jwks.rotation.OidcJsonWebKeystoreRotatedEvent;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jose4j.jwk.JsonWebKeySet;

/**
 * This is {@link OidcDefaultJsonWebKeyStoreListener}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
@Slf4j
public class OidcDefaultJsonWebKeyStoreListener implements OidcJsonWebKeyStoreListener {
    private final LoadingCache<OidcJsonWebKeyCacheKey, JsonWebKeySet> oidcJsonWebKeystoreCache;

    @Override
    public void handleOidcJsonWebKeystoreModifiedEvent(final OidcJsonWebKeystoreModifiedEvent event) {
        LOGGER.debug("Detected change in [{}]. Will invalidate OIDC JWKS cache...", event.getFile());
        oidcJsonWebKeystoreCache.invalidateAll();
    }

    /*
     * Signing keys are read from this cache, so without this a rotated key stays in use, and a revoked one stays usable,
     * until the cache expires.
     */
    @Override
    public void handleOidcJsonWebKeystoreRotatedEvent(final OidcJsonWebKeystoreRotatedEvent event) {
        LOGGER.debug("Keys in the JSON web keystore are rotated or revoked. Will invalidate OIDC JWKS cache...");
        oidcJsonWebKeystoreCache.invalidateAll();
    }
}
