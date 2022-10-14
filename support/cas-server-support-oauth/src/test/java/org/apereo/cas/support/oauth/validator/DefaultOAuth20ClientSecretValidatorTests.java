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
public class DefaultOAuth20ClientSecretValidatorTests extends AbstractOAuth20Tests {
    @Test
    public void verifyClientSecretCheck() {
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
    public void verifyClientSecretIsWrong() {
        val secret = RandomUtils.randomAlphanumeric(12);
        val encodedSecret = oauth20ClientSecretValidator.getCipherExecutor().encode(secret);
        val registeredService = new OAuthRegisteredService();
        registeredService.setClientId("clientid");
        registeredService.setClientSecret(encodedSecret);
        val result = oauth20ClientSecretValidator.validate(registeredService, "badSecret");
        assertFalse(result);
    }

    @Test
    public void verifyClientSecretCheckWithoutCipher() {
        val secret = RandomUtils.randomAlphanumeric(12);
        val registeredService = new OAuthRegisteredService();
        registeredService.setClientId("clientid");
        registeredService.setClientSecret(secret);
        val result = oauth20ClientSecretValidator.validate(registeredService, secret);
        assertTrue(result);
    }

    @Test
    public void verifyClientSecretUndefined() {
        val secret = RandomUtils.randomAlphanumeric(12);
        val registeredService = new OAuthRegisteredService();
        registeredService.setClientId("clientid");
        val result = oauth20ClientSecretValidator.validate(registeredService, secret);
        assertTrue(result);
    }

    @Test
    public void verifyClientSecretUrlEncoded() {
        val secret = "!@#$%^&^&*()";
        val encodedSecret = EncodingUtils.urlEncode(secret);
        val registeredService = new OAuthRegisteredService();
        registeredService.setClientId("clientid");
        registeredService.setClientSecret(secret);
        val result = oauth20ClientSecretValidator.validate(registeredService, encodedSecret);
        assertTrue(result);
    }

    @Test
    public void verifyNullClientSecretUrlEncoded() {
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
