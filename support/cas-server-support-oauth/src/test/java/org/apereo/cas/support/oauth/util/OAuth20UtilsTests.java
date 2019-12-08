package org.apereo.cas.support.oauth.util;

import org.apereo.cas.support.oauth.services.OAuth20RegisteredServiceCipherExecutor;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.util.RandomUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20UtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OAuth")
public class OAuth20UtilsTests {
    @Test
    public void verifyClientSecretCheck() {
        val cipher = new OAuth20RegisteredServiceCipherExecutor();
        val secret = RandomUtils.randomAlphanumeric(12);
        val encodedSecret = cipher.encode(secret);
        val registeredService = new OAuthRegisteredService();
        registeredService.setClientId("clientid");
        registeredService.setClientSecret(encodedSecret);
        val result = OAuth20Utils.checkClientSecret(registeredService, secret, cipher);
        assertTrue(result);
    }

    @Test
    public void verifyClientSecretCheckWithoutCipher() {
        val cipher = new OAuth20RegisteredServiceCipherExecutor();
        val secret = RandomUtils.randomAlphanumeric(12);
        val registeredService = new OAuthRegisteredService();
        registeredService.setClientId("clientid");
        registeredService.setClientSecret(secret);
        val result = OAuth20Utils.checkClientSecret(registeredService, secret, cipher);
        assertTrue(result);
    }
}
