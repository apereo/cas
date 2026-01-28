package org.apereo.cas.oidc.web.controllers.authorize;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OidcPushedAuthorizeEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("OIDCWeb")
@TestPropertySource(properties = "cas.authn.oidc.discovery.require-pushed-authorization-requests=true")
class OidcPushedAuthorizeEndpointControllerTests extends AbstractOidcTests {

    @Test
    void verifyGetOperationFails() throws Exception {
        val id = UUID.randomUUID().toString();
        val service = getOidcRegisteredService(id);
        service.setBypassApprovalPrompt(true);
        servicesManager.save(service);
        mockMvc.perform(get("/cas/oidc/" + OidcConstants.PUSHED_AUTHORIZE_URL)
                .param(OAuth20Constants.CLIENT_ID, id)
                .param(OAuth20Constants.CLIENT_SECRET, service.getClientSecret())
                .with(withHttpRequestProcessor())
            )
            .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void verifyPostWithoutRequiredParams() throws Exception {
        mockMvc.perform(post("/cas/oidc/" + OidcConstants.PUSHED_AUTHORIZE_URL)
                .with(withHttpRequestProcessor()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void verifyPostOperation() throws Exception {
        val id = UUID.randomUUID().toString();
        val service = getOidcRegisteredService(id);
        service.setBypassApprovalPrompt(true);
        servicesManager.save(service);

        mockMvc.perform(post("/cas/oidc/" + OidcConstants.PUSHED_AUTHORIZE_URL)
                .param(OAuth20Constants.CLIENT_ID, id)
                .param(OAuth20Constants.CLIENT_SECRET, service.getClientSecret())
                .param(OAuth20Constants.REDIRECT_URI, "https://oauth.example.org/")
                .param(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.name().toLowerCase(Locale.ENGLISH))
                .with(withHttpRequestProcessor()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.request_uri").exists())
            .andExpect(jsonPath("$.expires_in").exists());
    }

    @Test
    void verifyPostWithInvalidIssuer() throws Exception {
        val id = UUID.randomUUID().toString();
        val service = getOidcRegisteredService(id);
        service.setBypassApprovalPrompt(true);
        servicesManager.save(service);

        mockMvc.perform(post("/cas/oidc/" + OidcConstants.PUSHED_AUTHORIZE_URL)
                .param(OAuth20Constants.CLIENT_ID, id)
                .param(OAuth20Constants.CLIENT_SECRET, service.getClientSecret())
                .param(OAuth20Constants.REDIRECT_URI, "https://oauth.example.org/")
                .param(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.name().toLowerCase(Locale.ENGLISH))
                .with(withHttpRequestProcessor())
                .with(request -> {
                    request.setServerName("invalid.example.org");
                    return request;
                }))
            .andExpect(status().isBadRequest());
    }
}
