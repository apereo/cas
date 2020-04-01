package org.apereo.cas.oidc.jwks;

import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;

import com.github.benmanes.caffeine.cache.CacheLoader;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jose4j.jwk.PublicJsonWebKey;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

/**
 * This is {@link OidcServiceJsonWebKeystoreCacheLoader}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
public class OidcServiceJsonWebKeystoreCacheLoader implements CacheLoader<OAuthRegisteredService, Optional<PublicJsonWebKey>> {
    private final ApplicationContext applicationContext;

    @Override
    public Optional<PublicJsonWebKey> load(final @NonNull OAuthRegisteredService service) {
        if (service instanceof OidcRegisteredService) {
            val svc = (OidcRegisteredService) service;
            val jwks = OidcJsonWebKeyStoreUtils.getJsonWebKeySet(svc, applicationContext);
            if (jwks.isEmpty() || jwks.get().getJsonWebKeys().isEmpty()) {
                return Optional.empty();
            }
            val key = OidcJsonWebKeyStoreUtils.getJsonWebKeyFromJsonWebKeySet(jwks.get());
            if (key == null) {
                return Optional.empty();
            }
            return Optional.of(key);
        }
        return Optional.empty();
    }
}
