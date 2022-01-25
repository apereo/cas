package org.apereo.cas.oidc.jwks;

import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreModifiedEvent;

import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jose4j.jwk.JsonWebKeySet;

import java.util.Optional;

/**
 * This is {@link OidcDefaultJsonWebKeyStoreListener}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
@Slf4j
public class OidcDefaultJsonWebKeyStoreListener implements OidcJsonWebKeyStoreListener {
    private final LoadingCache<OidcJsonWebKeyCacheKey, Optional<JsonWebKeySet>> oidcJsonWebKeystoreCache;

    @Override
    public void handleOidcJsonWebKeystoreModifiedEvent(final OidcJsonWebKeystoreModifiedEvent event) {
        LOGGER.debug("Detected change in [{}]. Will invalidate OIDC JWKS cache...", event.getFile());
        oidcJsonWebKeystoreCache.invalidateAll();
    }
}
