package org.apereo.cas.support.oauth.web.endpoints;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.OAuth20Constants;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This class tests the {@link OAuth20RevocationEndpointController} class.
 *
 * @author Julien Huon
 * @since 6.2.0
 */
@Tag("OAuthWeb")
class OAuth20RevocationEndpointControllerTests extends AbstractOAuth20Tests {

    private static final String PUBLIC_CLIENT_ID = "clientWithoutSecret";

    private static final String REVOCATION_ENDPOINT = CONTEXT + OAuth20Constants.REVOCATION_URL;

    @Test
    void verifyNoGivenToken() throws Throwable {
        val registeredService = getRegisteredService(REDIRECT_URI, CLIENT_SECRET, Set.of());
        servicesManager.save(registeredService);

        mockMvc.perform(post(REVOCATION_ENDPOINT)
                .param(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
                .param(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_REQUEST));
    }

    @Test
    void verifyGivenInvalidClientId() throws Throwable {
        val registeredService = getRegisteredService(REDIRECT_URI, CLIENT_SECRET, Set.of());
        servicesManager.save(registeredService);

        mockMvc.perform(post(REVOCATION_ENDPOINT)
                .param(OAuth20Constants.CLIENT_ID, "InvalidClientId")
                .param(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET)
                .param(OAuth20Constants.TOKEN, "AT-1234"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void verifyGivenInvalidClientSecret() throws Throwable {
        val registeredService = getRegisteredService(REDIRECT_URI, CLIENT_SECRET, Set.of());
        servicesManager.save(registeredService);

        mockMvc.perform(post(REVOCATION_ENDPOINT)
                .param(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
                .param(OAuth20Constants.CLIENT_SECRET, WRONG_CLIENT_SECRET)
                .param(OAuth20Constants.TOKEN, "AT-1234"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void verifyGivenTokenNotInRegistry() throws Throwable {
        val registeredService = getRegisteredService(REDIRECT_URI, CLIENT_SECRET, Set.of());
        servicesManager.save(registeredService);
        servicesManager.save(getRegisteredService(REDIRECT_URI, PUBLIC_CLIENT_ID, StringUtils.EMPTY, Set.of()));

        mockMvc.perform(post(REVOCATION_ENDPOINT)
                .param(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
                .param(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET)
                .param(OAuth20Constants.TOKEN, "AT-1234"))
            .andExpect(status().isNotFound());

        mockMvc.perform(post(REVOCATION_ENDPOINT)
                .param(OAuth20Constants.CLIENT_ID, PUBLIC_CLIENT_ID)
                .param(OAuth20Constants.TOKEN, "AT-1234"))
            .andExpect(status().isNotFound());
    }

    @Test
    void verifyGivenUnsupportedToken() throws Throwable {
        val principal = createPrincipal();
        val service = getRegisteredService(REDIRECT_URI, CLIENT_SECRET, Set.of());
        servicesManager.save(service);

        val code = addCode(principal, service);

        mockMvc.perform(post(REVOCATION_ENDPOINT)
                .param(OAuth20Constants.CLIENT_ID, service.getClientId())
                .param(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET)
                .param(OAuth20Constants.TOKEN, code.getId()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_REQUEST));
    }

    @Test
    void verifyGivenAccessTokenInRegistry() throws Throwable {
        val principal = createPrincipal();
        val service = getRegisteredService(REDIRECT_URI, CLIENT_SECRET, Set.of());
        val publicService = getRegisteredService(REDIRECT_URI, PUBLIC_CLIENT_ID, StringUtils.EMPTY, Set.of());
        servicesManager.save(service, publicService);

        val accessToken = addAccessToken(principal, service);
        assertNotNull(ticketRegistry.getTicket(accessToken.getId()));

        mockMvc.perform(post(REVOCATION_ENDPOINT)
                .param(OAuth20Constants.CLIENT_ID, service.getClientId())
                .param(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET)
                .param(OAuth20Constants.TOKEN, accessToken.getId()))
            .andExpect(status().isOk());
        assertNull(ticketRegistry.getTicket(accessToken.getId()));

        val accessToken2 = addAccessToken(principal, publicService);
        assertNotNull(ticketRegistry.getTicket(accessToken2.getId()));

        mockMvc.perform(post(REVOCATION_ENDPOINT)
                .param(OAuth20Constants.CLIENT_ID, PUBLIC_CLIENT_ID)
                .param(OAuth20Constants.TOKEN, accessToken2.getId()))
            .andExpect(status().isOk());
        assertNull(ticketRegistry.getTicket(accessToken2.getId()));

        val accessToken3 = addAccessToken(principal, service);
        assertNotNull(ticketRegistry.getTicket(accessToken3.getId()));

        mockMvc.perform(post(REVOCATION_ENDPOINT)
                .param(OAuth20Constants.CLIENT_ID, PUBLIC_CLIENT_ID)
                .param(OAuth20Constants.TOKEN, accessToken3.getId()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_REQUEST));
    }

    @Test
    void verifyGivenRefreshTokenInRegistry() throws Throwable {
        val principal = createPrincipal();
        val service = getRegisteredService(REDIRECT_URI, CLIENT_SECRET, Set.of());
        val publicService = getRegisteredService(REDIRECT_URI, PUBLIC_CLIENT_ID, StringUtils.EMPTY, Set.of());
        servicesManager.save(service, publicService);

        val accessToken = addAccessToken(principal, service);
        val refreshToken = addRefreshToken(principal, service, accessToken);
        assertNotNull(ticketRegistry.getTicket(accessToken.getId()));
        assertNotNull(ticketRegistry.getTicket(refreshToken.getId()));

        mockMvc.perform(post(REVOCATION_ENDPOINT)
                .param(OAuth20Constants.CLIENT_ID, service.getClientId())
                .param(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET)
                .param(OAuth20Constants.TOKEN, refreshToken.getId()))
            .andExpect(status().isOk());
        assertNull(ticketRegistry.getTicket(refreshToken.getId()));
        assertNull(ticketRegistry.getTicket(accessToken.getId()));

        val accessToken2 = addAccessToken(principal, publicService);
        val refreshToken2 = addRefreshToken(principal, publicService, accessToken2);
        assertNotNull(ticketRegistry.getTicket(accessToken2.getId()));

        mockMvc.perform(post(REVOCATION_ENDPOINT)
                .param(OAuth20Constants.CLIENT_ID, PUBLIC_CLIENT_ID)
                .param(OAuth20Constants.TOKEN, refreshToken2.getId()))
            .andExpect(status().isOk());
        assertNull(ticketRegistry.getTicket(refreshToken2.getId()));
        assertNull(ticketRegistry.getTicket(accessToken2.getId()));

        val refreshToken3 = addRefreshToken(principal, service);
        assertNotNull(ticketRegistry.getTicket(refreshToken3.getId()));

        mockMvc.perform(post(REVOCATION_ENDPOINT)
                .param(OAuth20Constants.CLIENT_ID, PUBLIC_CLIENT_ID)
                .param(OAuth20Constants.TOKEN, refreshToken3.getId()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_REQUEST));
    }
}
