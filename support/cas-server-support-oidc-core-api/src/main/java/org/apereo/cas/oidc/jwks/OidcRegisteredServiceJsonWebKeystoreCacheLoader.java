package org.apereo.cas.oidc.jwks;

import org.apereo.cas.services.OidcRegisteredService;

import com.github.benmanes.caffeine.cache.CacheLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jose4j.jwk.JsonWebKeySet;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

/**
 * This is {@link OidcRegisteredServiceJsonWebKeystoreCacheLoader}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class OidcRegisteredServiceJsonWebKeystoreCacheLoader implements
    CacheLoader<OidcJsonWebKeyCacheKey, Optional<JsonWebKeySet>> {
    private final ApplicationContext applicationContext;

    @Override
    public Optional<JsonWebKeySet> load(final OidcJsonWebKeyCacheKey cacheKey) {
        val service = cacheKey.getRegisteredService();
        if (service instanceof OidcRegisteredService) {
            val oidcService = (OidcRegisteredService) service;
            val jwks = OidcJsonWebKeyStoreUtils.getJsonWebKeySet(oidcService,
                applicationContext, Optional.of(cacheKey.getUsage()));
            if (jwks.isEmpty() || jwks.get().getJsonWebKeys().isEmpty()) {
                return Optional.empty();
            }
            val requestedKid = Optional.ofNullable(oidcService.getJwksKeyId());
            LOGGER.debug("Locating requested key [{}] for service [{}]", requestedKid, oidcService);
            val key = OidcJsonWebKeyStoreUtils.getJsonWebKeyFromJsonWebKeySet(jwks.get(),
                requestedKid, Optional.of(cacheKey.getUsage()));
            key.ifPresent(k -> LOGGER.debug("Located requested key [{}] for service [{}]", k, oidcService));
            return key;
        }
        return Optional.empty();
    }
}
