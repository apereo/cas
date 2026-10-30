package org.apereo.cas.web;

import module java.base;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.config.CasTokenAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasTokenAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.token.authentication.TokenAuthenticationSecurity;
import org.apereo.cas.token.authentication.TokenCredential;
import org.apereo.cas.util.JsonUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.web.report.AbstractCasEndpointTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link TokenAuthenticationEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@TestPropertySource(properties = "management.endpoint.tokenAuth.access=UNRESTRICTED")
@Tag("ActuatorEndpoint")
@ImportAutoConfiguration({
    CasTokenAuthenticationAutoConfiguration.class,
    CasTokenAuthenticationWebflowAutoConfiguration.class
})
class TokenAuthenticationEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("tokenAuthenticationHandler")
    private AuthenticationHandler tokenAuthenticationHandler;

    @Test
    void verifyOperation() throws Throwable {
        val serviceId = "https://token.example.org/" + UUID.randomUUID();
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(serviceId);
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy());
        val signingSecret = new DefaultRegisteredServiceProperty();
        signingSecret.addValue(RandomUtils.randomAlphanumeric(256));
        registeredService.getProperties().put(RegisteredServiceProperties.TOKEN_SECRET_SIGNING.getPropertyName(), signingSecret);
        servicesManager.save(registeredService);
        val token = TokenAuthenticationSecurity.forRegisteredService(registeredService)
            .generateTokenFor(RegisteredServiceTestUtils.getAuthentication("casuser"))
            .orElseThrow();
        val service = RegisteredServiceTestUtils.getService(serviceId);
        val authnResults = tokenAuthenticationHandler.authenticate(new TokenCredential(token, service), service);
        assertEquals("casuser", authnResults.getPrincipal().getId());
        mockMvc.perform(get("/actuator/tokenAuth/{token}", token)
                .param("service", service.getId())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.principal.id").value("casuser"))
            .andExpect(jsonPath("$.registeredService.id").value(registeredService.getId()))
            .andExpect(jsonPath("$.registeredService.serviceId").value(serviceId))
            .andExpect(jsonPath("$.registeredService.properties").doesNotExist());
        mockMvc.perform(post("/actuator/tokenAuth/{username}", "casuser")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.render(Map.of("service", serviceId)))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void verifyServiceWithoutSigningSecret() throws Throwable {
        val serviceId = "https://token.example.org/" + UUID.randomUUID();
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(serviceId);
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy());
        servicesManager.save(registeredService);
        mockMvc.perform(get("/actuator/tokenAuth/{token}", "invalid-token")
                .param("service", serviceId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists());
    }
}
