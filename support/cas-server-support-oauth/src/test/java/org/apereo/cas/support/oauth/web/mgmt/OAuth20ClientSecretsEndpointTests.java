package org.apereo.cas.support.oauth.web.mgmt;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredServiceClientSecret;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.util.RandomUtils;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OAuth20ClientSecretsEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@TestPropertySource(properties = {
    "management.endpoint.oauthClientSecrets.access=UNRESTRICTED",
    "management.endpoints.web.exposure.include=*"
})
@Tag("OAuthWeb")
class OAuth20ClientSecretsEndpointTests extends AbstractOAuth20Tests {
    @Test
    void verifyRotateClientSecrets() throws Throwable {
        val originalSecret = RandomUtils.randomAlphanumeric(12);
        val registeredService = getRegisteredService(
            "https://oauth-%s.example.org".formatted(RandomUtils.randomAlphabetic(6)),
            UUID.randomUUID().toString(), originalSecret);
        servicesManager.save(registeredService);

        mockMvc.perform(post("/actuator/oauthClientSecrets/{clientId}", registeredService.getClientId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is2xxSuccessful());

        val result = findRegisteredService(registeredService);
        assertEquals(1, result.getClientSecrets().size());
        val rotatedSecret = result.getClientSecrets().getFirst();
        assertNotEquals(originalSecret, rotatedSecret.getValue());
        assertNotNull(rotatedSecret.toEffectiveExpiration());
    }

    @Test
    void verifyRotateExpiredClientSecrets() throws Throwable {
        val registeredService = getRegisteredService(
            "https://oauth-%s.example.org".formatted(RandomUtils.randomAlphabetic(6)),
            UUID.randomUUID().toString(), RandomUtils.randomAlphanumeric(12));
        val activeSecret = OAuthRegisteredServiceClientSecret.withoutExpiration(RandomUtils.randomAlphanumeric(12));
        val expiredSecret = new OAuthRegisteredServiceClientSecret(
            RandomUtils.randomAlphanumeric(12), ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(5));
        registeredService.setClientSecrets(List.of(expiredSecret, activeSecret));
        servicesManager.save(registeredService);

        mockMvc.perform(post("/actuator/oauthClientSecrets/{clientId}", registeredService.getClientId())
                .with(csrf())
                .param("expiredOnly", "true")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is2xxSuccessful());

        val result = findRegisteredService(registeredService);
        assertEquals(2, result.getClientSecrets().size());
        assertNotEquals(expiredSecret.getValue(), result.getClientSecrets().getFirst().getValue());
        assertEquals(activeSecret, result.getClientSecrets().get(1));
    }

    @Test
    void verifyRotateClientSecretsFailsForUnknownClient() throws Throwable {
        mockMvc.perform(post("/actuator/oauthClientSecrets/{clientId}", UUID.randomUUID().toString())
                .with(csrf())
                .param("expiredOnly", "false")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError());
    }

    private @Nullable OAuthRegisteredService findRegisteredService(final OAuthRegisteredService registeredService) {
        return OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, registeredService.getClientId());
    }
}
