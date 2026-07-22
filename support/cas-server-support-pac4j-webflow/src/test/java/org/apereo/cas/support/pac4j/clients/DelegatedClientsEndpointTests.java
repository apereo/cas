package org.apereo.cas.support.pac4j.clients;

import module java.base;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.apereo.cas.web.report.AbstractCasEndpointTests;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link DelegatedClientsEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@TestPropertySource(properties = {
    "management.endpoint.delegatedClients.access=UNRESTRICTED",
    
    "cas.authn.pac4j.cas[0].login-url=https://localhost:8444/cas/login",

    "cas.authn.pac4j.oauth2[0].id=123456",
    "cas.authn.pac4j.oauth2[0].secret=s3cr3t",

    "cas.authn.pac4j.oidc[0].google.id=123",
    "cas.authn.pac4j.oidc[0].google.secret=123",

    "cas.authn.pac4j.github.id=123",
    "cas.authn.pac4j.github.secret=123"
})
@Tag("ActuatorEndpoint")
@Import(BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
class DelegatedClientsEndpointTests extends AbstractCasEndpointTests {
    @Test
    void verifyOperation() throws Throwable {
        mockMvc.perform(get("/actuator/delegatedClients")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()", greaterThan(0)));

        mockMvc.perform(delete("/actuator/delegatedClients")
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()", greaterThan(0)));
    }
}
