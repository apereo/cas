package org.apereo.cas.authentication;

import module java.base;
import org.apereo.cas.impl.BasePasswordlessUserAccountStoreTests;
import org.apereo.cas.web.report.AbstractCasEndpointTests;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link PasswordlessAuthenticationEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@TestPropertySource(properties = {
    "cas.authn.passwordless.accounts.groovy.location=classpath:PasswordlessAccountEndpoint.groovy",
    "management.endpoint.passwordless.access=UNRESTRICTED"
})
@Tag("ActuatorEndpoint")
@Import(BasePasswordlessUserAccountStoreTests.SharedTestConfiguration.class)
class PasswordlessAuthenticationEndpointTests extends AbstractCasEndpointTests {

    @Test
    void verifyAccountFound() throws Throwable {
        mockMvc.perform(get("/actuator/passwordless")
                .param("username", "casuser")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("casuser"))
            .andExpect(jsonPath("$.email").value("casuser@example.org"))
            .andExpect(jsonPath("$.phoneNumber").value("1234567890"))
            .andExpect(jsonPath("$.name").value("casuser"))
            .andExpect(jsonPath("$.attributes.lastName[0]").value("Smith"));
    }

    @Test
    void verifyAccountNotFound() throws Throwable {
        mockMvc.perform(get("/actuator/passwordless")
                .param("username", "unknown-user")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void verifyMissingUsernameParameter() throws Throwable {
        mockMvc.perform(get("/actuator/passwordless")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
}
