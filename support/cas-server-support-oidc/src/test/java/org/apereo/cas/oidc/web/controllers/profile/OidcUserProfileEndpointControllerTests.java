package org.apereo.cas.oidc.web.controllers.profile;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OidcUserProfileEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OIDCWeb")
class OidcUserProfileEndpointControllerTests extends AbstractOidcTests {

    @Test
    void verifyBadEndpointRequest() throws Throwable {
        mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.PROFILE_URL)
                .with(withHttpRequestProcessor())
                .with(request -> {
                    request.setServerName("unknown.issuer.org");
                    return request;
                }))
            .andExpect(status().isBadRequest());
    }

    @Test
    void verifyGetRequest() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);

        val accessToken = getAccessToken(registeredService.getClientId());
        ticketRegistry.addTicket(accessToken.getTicketGrantingTicket());
        ticketRegistry.addTicket(accessToken);

        mockMvc.perform(get("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.PROFILE_URL)
                .with(withHttpRequestProcessor())
                .param(OAuth20Constants.ACCESS_TOKEN, accessToken.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sub").exists());
    }

    @Test
    void verifyPostRequest() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);

        val accessToken = getAccessToken(registeredService.getClientId());
        ticketRegistry.addTicket(accessToken.getTicketGrantingTicket());
        ticketRegistry.addTicket(accessToken);

        mockMvc.perform(post("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.PROFILE_URL)
                .with(withHttpRequestProcessor())
                .param(OAuth20Constants.ACCESS_TOKEN, accessToken.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sub").exists());
    }

    @Test
    void verifyWebFingerEndpoint() throws Throwable {
        mockMvc.perform(get("/cas/" + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.WELL_KNOWN_URL + "/webfinger")
                .queryParam("resource", "acct:casuser@sso.example.org")
                .queryParam("rel", OidcConstants.WEBFINGER_REL)
                .with(withHttpRequestProcessor()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.subject").value("acct:casuser@sso.example.org"))
            .andExpect(jsonPath("$.links").isArray());
    }
}
