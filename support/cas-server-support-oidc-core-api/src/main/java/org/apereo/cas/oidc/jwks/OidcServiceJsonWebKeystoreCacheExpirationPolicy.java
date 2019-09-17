package org.apereo.cas.oidc.jwks;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;

import com.github.benmanes.caffeine.cache.Expiry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.RsaJsonWebKey;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link OidcServiceJsonWebKeystoreCacheExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class OidcServiceJsonWebKeystoreCacheExpirationPolicy implements Expiry<OAuthRegisteredService, Optional<RsaJsonWebKey>> {
    private final CasConfigurationProperties casProperties;

    @Override
    public long expireAfterCreate(final OAuthRegisteredService oidcRegisteredService,
                                  final Optional<RsaJsonWebKey> rsaJsonWebKey,
                                  final long currentTime) {
        return getExpiration(oidcRegisteredService, currentTime);
    }

    @Override
    public long expireAfterUpdate(final OAuthRegisteredService oidcRegisteredService,
                                  final Optional<RsaJsonWebKey> rsaJsonWebKey,
                                  final long currentTime, final long currentDuration) {
        return getExpiration(oidcRegisteredService, currentDuration);
    }

    @Override
    public long expireAfterRead(final OAuthRegisteredService oidcRegisteredService,
                                final Optional<RsaJsonWebKey> rsaJsonWebKey,
                                final long currentTime,
                                final long currentDuration) {
        return getExpiration(oidcRegisteredService, currentDuration);
    }

    private long getExpiration(final OAuthRegisteredService givenService, final long currentTime) {
        if (givenService instanceof OidcRegisteredService) {
            val service = (OidcRegisteredService) givenService;
            if (service.getJwksCacheDuration() > 0 && StringUtils.isNotBlank(service.getJwksCacheTimeUnit())) {
                val timeUnit = TimeUnit.valueOf(service.getJwksCacheTimeUnit().trim().toUpperCase());
                return timeUnit.toNanos(service.getJwksCacheDuration());
            }
            return TimeUnit.MINUTES.toNanos(casProperties.getAuthn().getOidc().getJwksCacheInMinutes());
        }
        return -1;
    }
}
