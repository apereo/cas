package org.apereo.cas.oidc.web;

import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.validator.DefaultOAuth20ClientSecretValidator;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * This is {@link OidcClientSecretValidator}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Slf4j
public class OidcClientSecretValidator extends DefaultOAuth20ClientSecretValidator {
    public OidcClientSecretValidator(final CipherExecutor<Serializable, String> cipherExecutor) {
        super(cipherExecutor);
    }

    @Override
    public boolean validate(final OAuthRegisteredService registeredService, final String clientSecret) {
        return super.validate(registeredService, clientSecret) && !isClientSecretExpired(registeredService);
    }

    @Override
    public boolean isClientSecretExpired(final OAuthRegisteredService registeredService) {
        if (registeredService instanceof final OidcRegisteredService oidcService && oidcService.getClientSecretExpiration() > 0) {
            val expirationTime = DateTimeUtils.zonedDateTimeOf(Instant.ofEpochSecond(oidcService.getClientSecretExpiration()));
            val currentTime = ZonedDateTime.now(ZoneOffset.UTC);
            LOGGER.debug("Client secret is set to expire at [{}], while now is [{}]", expirationTime, currentTime);
            if (currentTime.isAfter(expirationTime)) {
                LOGGER.warn("Client secret for service [{}] has expired at [{}] and must be renewed",
                    oidcService.getName(), expirationTime);
                return true;
            }
        }
        return false;
    }
}
