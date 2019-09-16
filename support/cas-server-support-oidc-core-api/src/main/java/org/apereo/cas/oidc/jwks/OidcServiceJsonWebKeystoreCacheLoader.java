package org.apereo.cas.oidc.jwks;

import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;

import com.github.benmanes.caffeine.cache.CacheLoader;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jose4j.jwk.RsaJsonWebKey;

import java.util.Optional;

/**
 * This is {@link OidcServiceJsonWebKeystoreCacheLoader}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class OidcServiceJsonWebKeystoreCacheLoader implements CacheLoader<OAuthRegisteredService, Optional<RsaJsonWebKey>> {

    @Override
    public Optional<RsaJsonWebKey> load(final @NonNull OAuthRegisteredService service) {
        if (service instanceof OidcRegisteredService) {
            val svc = (OidcRegisteredService) service;
            val jwks = OidcJsonWebKeySetUtils.getJsonWebKeySet(svc);
            if (jwks.isEmpty() || jwks.get().getJsonWebKeys().isEmpty()) {
                return Optional.empty();
            }
            val key = OidcJsonWebKeySetUtils.getJsonWebKeyFromJsonWebKeySet(jwks.get());
            if (key == null) {
                return Optional.empty();
            }
            return Optional.of(key);
        }
        return Optional.empty();
    }
}
