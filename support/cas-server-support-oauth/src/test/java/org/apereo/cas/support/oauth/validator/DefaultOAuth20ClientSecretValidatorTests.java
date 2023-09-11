package org.apereo.cas.support.oauth.validator;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.RandomUtils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultOAuth20ClientSecretValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("OAuth")
class DefaultOAuth20ClientSecretValidatorTests extends AbstractOAuth20Tests {
    @Test
    void verifyClientSecretCheck() throws Throwable {
        val secret = RandomUtils.randomAlphanumeric(12);
        val encodedSecret = oauth20ClientSecretValidator.getCipherExecutor().encode(secret);
        val registeredService = new OAuthRegisteredService();
        registeredService.setClientId("clientid");
        registeredService.setClientSecret(encodedSecret);
        val result = oauth20ClientSecretValidator.validate(registeredService, secret);
        assertTrue(result);
        assertFalse(oauth20ClientSecretValidator.isClientSecretExpired(registeredService));
    }

    @Test
    void verifyClientSecretIsWrong() throws Throwable {
        val secret = RandomUtils.randomAlphanumeric(12);
        val encodedSecret = oauth20ClientSecretValidator.getCipherExecutor().encode(secret);
        val registeredService = new OAuthRegisteredService();
        registeredService.setClientId("clientid");
        registeredService.setClientSecret(encodedSecret);
        val result = oauth20ClientSecretValidator.validate(registeredService, "badSecret");
        assertFalse(result);
    }

    @Test
    void verifyClientSecretCheckWithoutCipher() throws Throwable {
        val secret = RandomUtils.randomAlphanumeric(12);
        val registeredService = new OAuthRegisteredService();
        registeredService.setClientId("clientid");
        registeredService.setClientSecret(secret);
        val result = oauth20ClientSecretValidator.validate(registeredService, secret);
        assertTrue(result);
    }

    @Test
    void verifyClientSecretUndefined() throws Throwable {
        val secret = RandomUtils.randomAlphanumeric(12);
        val registeredService = new OAuthRegisteredService();
        registeredService.setClientId("clientid");
        val result = oauth20ClientSecretValidator.validate(registeredService, secret);
        assertTrue(result);
    }

    @Test
    void verifyClientSecretUrlEncoded() throws Throwable {
        val secret = "!@#$%^&^&*()";
        val encodedSecret = EncodingUtils.urlEncode(secret);
        val registeredService = new OAuthRegisteredService();
        registeredService.setClientId("clientid");
        registeredService.setClientSecret(secret);
        val result = oauth20ClientSecretValidator.validate(registeredService, encodedSecret);
        assertTrue(result);
    }

    @Test
    void verifyNullClientSecretUrlEncoded() throws Throwable {
        val secret = "!@#$%^&^&*()";
        val registeredService = new OAuthRegisteredService();
        registeredService.setClientId("clientid");
        registeredService.setClientSecret(secret);
        val result = oauth20ClientSecretValidator.validate(registeredService, null);
        assertFalse(result);
        val result2 = oauth20ClientSecretValidator.validate(registeredService, StringUtils.EMPTY);
        assertFalse(result2);
    }
}
