package org.apereo.cas.support.oauth.validator;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredServiceClientSecret;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultOAuth20ClientSecretValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("OAuth")
@TestPropertySource(properties = "app.custom.secret=T0ps3cr3t#")
class DefaultOAuth20ClientSecretValidatorTests extends AbstractOAuth20Tests {

    @BeforeEach
    void setup() {
        SpringExpressionLanguageValueResolver.getInstance().withApplicationContext(applicationContext);
    }

    @Test
    void verifyClientSecretCheck() {
        val secret = RandomUtils.randomAlphanumeric(12);
        val encodedSecret = oauth20ClientSecretValidator.getCipherExecutor().encode(secret);
        val registeredService = new OAuthRegisteredService();
        registeredService.setClientId("clientid");
        val clientSecret = OAuthRegisteredServiceClientSecret.withoutExpiration(encodedSecret);
        registeredService.setClientSecrets(List.of(clientSecret));
        val result = oauth20ClientSecretValidator.validate(registeredService, secret);
        assertTrue(result);
        assertFalse(oauth20ClientSecretValidator.isClientSecretExpired(clientSecret, registeredService));
    }

    @Test
    void verifyClientSecretIsWrong() {
        val secret = RandomUtils.randomAlphanumeric(12);
        val encodedSecret = oauth20ClientSecretValidator.getCipherExecutor().encode(secret);
        val registeredService = new OAuthRegisteredService();
        registeredService.setClientId("clientid");
        val clientSecret = OAuthRegisteredServiceClientSecret.withoutExpiration(encodedSecret);
        registeredService.setClientSecrets(List.of(clientSecret));
        val result = oauth20ClientSecretValidator.validate(registeredService, "badSecret");
        assertFalse(result);
    }

    @Test
    void verifyClientSecretCheckWithoutCipher() {
        val secret = RandomUtils.randomAlphanumeric(12);
        val clientSecret = OAuthRegisteredServiceClientSecret.withoutExpiration(secret);
        val registeredService = new OAuthRegisteredService();
        registeredService.setClientId("clientid");
        registeredService.setClientSecrets(List.of(clientSecret));
        val result = oauth20ClientSecretValidator.validate(registeredService, secret);
        assertTrue(result);
    }

    @Test
    void verifyClientSecretUndefined() {
        val secret = RandomUtils.randomAlphanumeric(12);
        val registeredService = new OAuthRegisteredService();
        registeredService.setClientId("clientid");
        val result = oauth20ClientSecretValidator.validate(registeredService, secret);
        assertTrue(result);
    }

    @Test
    void verifyClientSecretUrlEncoded() {
        val secret = "!@#$%^&^&*()";
        val clientSecret = OAuthRegisteredServiceClientSecret.withoutExpiration(secret);
        val registeredService = new OAuthRegisteredService();
        registeredService.setClientId(UUID.randomUUID().toString());
        registeredService.setClientSecrets(List.of(clientSecret));
        val result = oauth20ClientSecretValidator.validate(registeredService, secret);
        assertTrue(result);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void verifyMultipleSecrets(final int index) {
        val registeredService = new OAuthRegisteredService();
        registeredService.setClientId(UUID.randomUUID().toString());
        registeredService.setClientSecrets(List.of(
            OAuthRegisteredServiceClientSecret.withoutExpiration(RandomUtils.randomAlphanumeric(12)),
            OAuthRegisteredServiceClientSecret.withoutExpiration(RandomUtils.randomAlphanumeric(12)),
            OAuthRegisteredServiceClientSecret.withoutExpiration(RandomUtils.randomAlphanumeric(12))
        ));
        val secret = registeredService.getClientSecrets().get(index).getValue();
        val result = oauth20ClientSecretValidator.validate(registeredService, secret);
        assertTrue(result);
    }

    @Test
    void verifyExpiredClientSecretsAreRemoved() {
        val registeredService = getRegisteredService();
        val expiredSecret = new OAuthRegisteredServiceClientSecret(
            RandomUtils.randomAlphanumeric(12), ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(5));
        val activeSecret = OAuthRegisteredServiceClientSecret.withoutExpiration(RandomUtils.randomAlphanumeric(12));
        val expiringSecret = new OAuthRegisteredServiceClientSecret(
            RandomUtils.randomAlphanumeric(12), ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(5));
        registeredService.setClientSecrets(new ArrayList<>(List.of(expiredSecret, activeSecret, expiringSecret)));

        val result = oauth20ClientSecretValidator.validate(registeredService, activeSecret.getValue());

        assertTrue(result);
        assertEquals(List.of(activeSecret, expiringSecret), registeredService.getClientSecrets());
        assertFalse(registeredService.getClientSecrets().contains(expiredSecret));
    }

    @Test
    void verifyAllExpiredClientSecretsAreRemoved() {
        val registeredService = getRegisteredService();
        val expiredSecret = new OAuthRegisteredServiceClientSecret(
            RandomUtils.randomAlphanumeric(12), ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(5));
        val expiredSecretWithMatchingValue = new OAuthRegisteredServiceClientSecret(
            RandomUtils.randomAlphanumeric(12), ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(10));
        registeredService.setClientSecrets(new ArrayList<>(List.of(expiredSecret, expiredSecretWithMatchingValue)));

        val result = oauth20ClientSecretValidator.validate(registeredService, expiredSecretWithMatchingValue.getValue());

        assertFalse(result);
        assertTrue(registeredService.getClientSecrets().isEmpty());
    }

    private static OAuthRegisteredService getRegisteredService() {
        val registeredService = new OAuthRegisteredService();
        registeredService.setName(UUID.randomUUID().toString());
        registeredService.setServiceId("https://example.org/%s".formatted(UUID.randomUUID()));
        registeredService.setClientId(UUID.randomUUID().toString());
        return registeredService;
    }
}
