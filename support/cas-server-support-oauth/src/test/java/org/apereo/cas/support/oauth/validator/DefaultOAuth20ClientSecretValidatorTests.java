package org.apereo.cas.support.oauth.validator;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.events.service.CasRegisteredServicesLoadedEvent;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredServiceClientSecret;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
    void verifyExpiredClientSecretsAreIgnored() {
        val registeredService = getRegisteredService();
        val expiredSecret = new OAuthRegisteredServiceClientSecret(
            RandomUtils.randomAlphanumeric(12), ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(5));
        val activeSecret = OAuthRegisteredServiceClientSecret.withoutExpiration(RandomUtils.randomAlphanumeric(12));
        val expiringSecret = new OAuthRegisteredServiceClientSecret(
            RandomUtils.randomAlphanumeric(12), ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(5));
        val secrets = List.of(expiredSecret, activeSecret, expiringSecret);
        registeredService.setClientSecrets(new ArrayList<>(secrets));

        assertTrue(oauth20ClientSecretValidator.validate(registeredService, activeSecret.getValue()));
        assertTrue(oauth20ClientSecretValidator.validate(registeredService, expiringSecret.getValue()));
        assertFalse(oauth20ClientSecretValidator.validate(registeredService, expiredSecret.getValue()));
        assertEquals(secrets, registeredService.getClientSecrets());
    }

    @Test
    void verifyAllExpiredClientSecretsAreRefused() {
        val registeredService = getRegisteredService();
        val expiredSecret = new OAuthRegisteredServiceClientSecret(
            RandomUtils.randomAlphanumeric(12), ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(5));
        val expiredSecretWithMatchingValue = new OAuthRegisteredServiceClientSecret(
            RandomUtils.randomAlphanumeric(12), ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(10));
        val secrets = List.of(expiredSecret, expiredSecretWithMatchingValue);
        registeredService.setClientSecrets(new ArrayList<>(secrets));

        assertFalse(oauth20ClientSecretValidator.validate(registeredService, expiredSecretWithMatchingValue.getValue()));
        assertFalse(oauth20ClientSecretValidator.validate(registeredService, expiredSecretWithMatchingValue.getValue()));
        assertFalse(oauth20ClientSecretValidator.validate(registeredService, RandomUtils.randomAlphanumeric(12)));
        assertEquals(secrets, registeredService.getClientSecrets());
    }

    @Test
    void verifyBlankClientSecretIsRefused() {
        val registeredService = getRegisteredService();
        registeredService.setClientSecrets(List.of(OAuthRegisteredServiceClientSecret.withoutExpiration(RandomUtils.randomAlphanumeric(12))));
        assertFalse(oauth20ClientSecretValidator.validate(registeredService, StringUtils.EMPTY));
    }

    @Test
    void verifyExpiredClientSecretsRemoved() {
        val registeredService = getRegisteredService();
        val expiredSecret = new OAuthRegisteredServiceClientSecret(
            RandomUtils.randomAlphanumeric(12), ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(5));
        val activeSecret = OAuthRegisteredServiceClientSecret.withoutExpiration(RandomUtils.randomAlphanumeric(12));
        val expiringSecret = new OAuthRegisteredServiceClientSecret(
            RandomUtils.randomAlphanumeric(12), ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(5));
        val secrets = new ArrayList<>(List.of(expiredSecret, activeSecret, expiringSecret));
        registeredService.setClientSecrets(secrets);

        assertTrue(oauth20ClientSecretValidator.removeExpiredClientSecrets(registeredService));
        assertEquals(List.of(activeSecret, expiringSecret), registeredService.getClientSecrets());
        assertEquals(3, secrets.size());
        assertFalse(oauth20ClientSecretValidator.removeExpiredClientSecrets(registeredService));
    }

    @Test
    void verifyClientSecretExpiredLastIsKept() {
        val registeredService = getRegisteredService();
        val expiredSecret = new OAuthRegisteredServiceClientSecret(
            RandomUtils.randomAlphanumeric(12), ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(10));
        val lastExpiredSecret = new OAuthRegisteredServiceClientSecret(
            RandomUtils.randomAlphanumeric(12), ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(5));
        registeredService.setClientSecrets(new ArrayList<>(List.of(lastExpiredSecret, expiredSecret)));

        assertTrue(oauth20ClientSecretValidator.removeExpiredClientSecrets(registeredService));
        assertEquals(List.of(lastExpiredSecret), registeredService.getClientSecrets());
        assertFalse(oauth20ClientSecretValidator.removeExpiredClientSecrets(registeredService));
        assertFalse(oauth20ClientSecretValidator.validate(registeredService, lastExpiredSecret.getValue()));
        assertFalse(oauth20ClientSecretValidator.validate(registeredService, RandomUtils.randomAlphanumeric(12)));
    }

    @Test
    void verifyExpiredClientSecretsRemovedOnceServicesAreLoaded() {
        val registeredService = getRegisteredService();
        val expiredSecret = new OAuthRegisteredServiceClientSecret(
            RandomUtils.randomAlphanumeric(12), ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(5));
        val activeSecret = OAuthRegisteredServiceClientSecret.withoutExpiration(RandomUtils.randomAlphanumeric(12));
        registeredService.setClientSecrets(new ArrayList<>(List.of(expiredSecret, activeSecret)));
        val savedService = (OAuthRegisteredService) servicesManager.save(registeredService);

        val listener = new DefaultOAuth20ClientSecretExpirationListener(servicesManager, oauth20ClientSecretValidator);
        listener.handleRegisteredServicesLoadedEvent(new CasRegisteredServicesLoadedEvent(this,
            List.of(savedService), ClientInfoHolder.getClientInfo()));
        val loadedService = servicesManager.findServiceBy(savedService.getId(), OAuthRegisteredService.class);
        assertNotNull(loadedService);
        assertEquals(List.of(activeSecret), loadedService.getClientSecrets());
    }

    private static OAuthRegisteredService getRegisteredService() {
        val registeredService = new OAuthRegisteredService();
        registeredService.setName(UUID.randomUUID().toString());
        registeredService.setServiceId("https://example.org/%s".formatted(UUID.randomUUID()));
        registeredService.setClientId(UUID.randomUUID().toString());
        return registeredService;
    }
}
