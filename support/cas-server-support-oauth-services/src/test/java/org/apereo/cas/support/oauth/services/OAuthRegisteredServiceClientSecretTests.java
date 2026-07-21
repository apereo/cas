package org.apereo.cas.support.oauth.services;

import module java.base;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuthRegisteredServiceClientSecretTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("OAuth")
class OAuthRegisteredServiceClientSecretTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifyOperation() {
        val registeredService = getRegisteredService();
        registeredService.setClientSecrets(CollectionUtils.wrapList(
            OAuthRegisteredServiceClientSecret.withoutExpiration(UUID.randomUUID().toString()),
            new OAuthRegisteredServiceClientSecret(UUID.randomUUID().toString(), "1234567890"),
            new OAuthRegisteredServiceClientSecret(UUID.randomUUID().toString(), "2035-01-02T03:04:05"),
            new OAuthRegisteredServiceClientSecret(UUID.randomUUID().toString(), "01/02/2035 03:04 AM")));
        val serialized = MAPPER.writeValueAsString(registeredService);
        val deserialized = MAPPER.readValue(serialized, OAuthRegisteredService.class);
        assertEquals(4, deserialized.getClientSecrets().size());
    }

    @Test
    void verifyCompatibility() {
        val registeredService = getRegisteredService();
        val root = (ObjectNode) MAPPER.readTree(MAPPER.writeValueAsString(registeredService));
        root.put("clientSecret", "clientSecret");
        val serialized = MAPPER.writeValueAsString(root);
        val deserialized = MAPPER.readValue(serialized, OAuthRegisteredService.class);
        assertEquals(1, deserialized.getClientSecrets().size());
        val clientSecret = deserialized.getClientSecrets().getFirst();
        assertEquals("clientSecret", clientSecret.getValue());
        assertNull(clientSecret.getExpiration());
        assertFalse(clientSecret.hasClientSecretExpired(deserialized));
    }

    @Test
    void verifySetClientSecret() {
        val registeredService = getRegisteredService();
        registeredService.setClientSecret("clientSecret");
        registeredService.setClientSecret("anotherSecret");

        assertEquals(2, registeredService.getClientSecrets().size());
        assertEquals("clientSecret", registeredService.getClientSecrets().getFirst().getValue());
        assertNull(registeredService.getClientSecrets().getFirst().getExpiration());
        assertEquals("anotherSecret", registeredService.getClientSecrets().get(1).getValue());
        assertNull(registeredService.getClientSecrets().get(1).getExpiration());
        assertEquals("clientSecret", registeredService.getClientSecret());
    }

    @Test
    void verifyGetClientSecret() {
        val registeredService = getRegisteredService();
        assertEquals(StringUtils.EMPTY, registeredService.getClientSecret());
        assertTrue(registeredService.getClientSecrets().isEmpty());

        val currentSecret = "currentSecret";
        registeredService.setClientSecrets(CollectionUtils.wrapList(
            new OAuthRegisteredServiceClientSecret("expiredSecret", ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(5)),
            new OAuthRegisteredServiceClientSecret(currentSecret, ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(5)),
            OAuthRegisteredServiceClientSecret.withoutExpiration("fallbackSecret")));

        assertEquals(currentSecret, registeredService.getClientSecret());
    }

    @Test
    void verifyGetClientSecretWithExpiredSecrets() {
        val registeredService = getRegisteredService();
        registeredService.setClientSecrets(CollectionUtils.wrapList(
            new OAuthRegisteredServiceClientSecret("expiredSecret", ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(5))));
        assertEquals(StringUtils.EMPTY, registeredService.getClientSecret());
    }

    @ParameterizedTest
    @MethodSource("expirationFormats")
    void verifyExpirationFormats(final String expiration,
                                 final ZonedDateTime expected) {
        val secret = new OAuthRegisteredServiceClientSecret("clientSecret", expiration);
        assertEquals(expected.truncatedTo(ChronoUnit.SECONDS), secret.toEffectiveExpiration());
        assertFalse(secret.hasClientSecretExpired(getRegisteredService()));
    }

    @Test
    void verifyExpirationValues() {
        val registeredService = getRegisteredService();
        val future = ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(5).truncatedTo(ChronoUnit.SECONDS);
        val activeSecret = new OAuthRegisteredServiceClientSecret("clientSecret", future);
        assertEquals(String.valueOf(future.toEpochSecond()), activeSecret.getExpiration());
        assertEquals(future, activeSecret.toEffectiveExpiration());
        assertFalse(activeSecret.hasClientSecretExpired(registeredService));

        val expiredSecret = new OAuthRegisteredServiceClientSecret("clientSecret", ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(5));
        assertTrue(expiredSecret.hasClientSecretExpired(registeredService));

        expiredSecret.expireAt(future);
        assertEquals(future, expiredSecret.toEffectiveExpiration());
        assertFalse(expiredSecret.hasClientSecretExpired(registeredService));

        val withoutExpiration = OAuthRegisteredServiceClientSecret.withoutExpiration("clientSecret");
        assertNull(withoutExpiration.getExpiration());
        assertFalse(withoutExpiration.hasClientSecretExpired(registeredService));
    }

    private static Stream<Arguments> expirationFormats() {
        val expected = ZonedDateTime.of(2035, 1, 2, 3, 4, 5, 0, ZoneOffset.UTC);
        return Stream.of(
            Arguments.of(String.valueOf(expected.toEpochSecond()), expected),
            Arguments.of("2035-01-02T03:04:05", expected),
            Arguments.of("2035-01-02T03:04:05Z", expected),
            Arguments.of("01/02/2035 03:04 AM", expected.truncatedTo(ChronoUnit.MINUTES)),
            Arguments.of("01/02/2035 3:04 AM", expected.truncatedTo(ChronoUnit.MINUTES)),
            Arguments.of("01/02/2035 03:04", expected.truncatedTo(ChronoUnit.MINUTES)),
            Arguments.of("01/02/2035", expected.toLocalDate().atStartOfDay(ZoneOffset.UTC))
        );
    }

    private static OAuthRegisteredService getRegisteredService() {
        val registeredService = new OAuthRegisteredService();
        registeredService.setName(UUID.randomUUID().toString());
        registeredService.setServiceId("testId");
        registeredService.setClientId(UUID.randomUUID().toString());
        return registeredService;
    }
}
