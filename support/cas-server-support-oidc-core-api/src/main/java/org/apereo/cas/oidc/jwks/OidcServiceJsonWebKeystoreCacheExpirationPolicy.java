package org.apereo.cas.oidc.jwks;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.OidcRegisteredService;

import com.github.benmanes.caffeine.cache.Expiry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.JsonWebKeySet;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link OidcServiceJsonWebKeystoreCacheExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class OidcServiceJsonWebKeystoreCacheExpirationPolicy
    implements Expiry<OidcJsonWebKeyCacheKey, Optional<JsonWebKeySet>> {
    private final CasConfigurationProperties casProperties;

    @Override
    public long expireAfterCreate(final OidcJsonWebKeyCacheKey service,
                                  final Optional<JsonWebKeySet> jsonWebKey,
                                  final long currentTime) {
        return getExpiration(service);
    }

    @Override
    public long expireAfterUpdate(final OidcJsonWebKeyCacheKey service,
                                  final Optional<JsonWebKeySet> jsonWebKey,
                                  final long currentTime, final long currentDuration) {
        return getExpiration(service);
    }

    @Override
    public long expireAfterRead(final OidcJsonWebKeyCacheKey service,
                                final Optional<JsonWebKeySet> jsonWebKey,
                                final long currentTime,
                                final long currentDuration) {
        return getExpiration(service);
    }

    private long getExpiration(final OidcJsonWebKeyCacheKey givenService) {
        LOGGER.trace("Attempting to determine JWKS cache expiration value for [{}]", givenService);
        if (givenService.getRegisteredService() instanceof OidcRegisteredService) {
            val service = (OidcRegisteredService) givenService.getRegisteredService();
            if (service.getJwksCacheDuration() > 0 && StringUtils.isNotBlank(service.getJwksCacheTimeUnit())) {
                val timeUnit = TimeUnit.valueOf(service.getJwksCacheTimeUnit().trim().toUpperCase());
                val expiration = timeUnit.toNanos(service.getJwksCacheDuration());
                LOGGER.trace("JWKS cache expiration value for service [{}] is set to [{}]", service, expiration);
                return expiration;
            }
            val jwks = casProperties.getAuthn().getOidc().getJwks();
            val expiration = Beans.newDuration(jwks.getCore().getJwksCacheExpiration()).toNanos();
            LOGGER.trace("CAS JWKS cache expiration value is set to [{}]", expiration);
            return expiration;
        }
        return -1;
    }
}
