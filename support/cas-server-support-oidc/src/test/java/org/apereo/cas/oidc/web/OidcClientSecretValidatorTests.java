package org.apereo.cas.oidc.web;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.support.oauth.validator.OAuth20ClientSecretValidator;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcClientSecretValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("OIDCAuthentication")
class OidcClientSecretValidatorTests extends AbstractOidcTests {
    @Autowired
    @Qualifier(OAuth20ClientSecretValidator.BEAN_NAME)
    private OAuth20ClientSecretValidator oauth20ClientSecretValidator;

    @Test
    void verifyNotExpired() {
        val secret = UUID.randomUUID().toString();
        val service = getOidcRegisteredService();
        service.setClientSecret(secret);
        service.setClientSecretExpiration(ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(1).toEpochSecond());
        val results = oauth20ClientSecretValidator.validate(service, secret);
        assertTrue(results);
    }

    @Test
    void verifyExpired() {
        val secret = UUID.randomUUID().toString();
        val service = getOidcRegisteredService();
        service.setClientSecret(secret);
        service.setClientSecretExpiration(ZonedDateTime.now(ZoneOffset.UTC).minusHours(1).toEpochSecond());
        val results = oauth20ClientSecretValidator.validate(service, secret);
        assertFalse(results);
    }
}
