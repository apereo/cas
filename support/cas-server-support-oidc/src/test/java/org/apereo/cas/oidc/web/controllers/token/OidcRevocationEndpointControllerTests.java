package org.apereo.cas.oidc.web.controllers.token;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OidcRevocationEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OIDCWeb")
class OidcRevocationEndpointControllerTests extends AbstractOidcTests {
    @Test
    void verifyBadEndpointRequest() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);
        mockMvc.perform(post('/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.REVOCATION_URL)
                .secure(true)
                .param(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
                .param(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecret())
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(OAuth20Constants.INVALID_REQUEST));
    }

    @Test
    void verifyAccessTokenRevocation() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);

        val token = getAccessToken(registeredService.getClientId());
        ticketRegistry.addTicket(token.getTicketGrantingTicket());
        ticketRegistry.addTicket(token);

        mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.REVOCATION_URL)
                .secure(true)
                .param(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
                .param(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecret())
                .param(OAuth20Constants.TOKEN, token.getId())
                .with(withHttpRequestProcessor())
            )
            .andExpect(status().isOk());
        assertNull(ticketRegistry.getTicket(token.getId()));
    }

}
