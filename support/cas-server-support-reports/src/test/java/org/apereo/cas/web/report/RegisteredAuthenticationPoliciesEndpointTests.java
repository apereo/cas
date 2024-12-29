package org.apereo.cas.web.report;

import org.apereo.cas.authentication.policy.AtLeastOneCredentialValidatedAuthenticationPolicy;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link RegisteredAuthenticationPoliciesEndpointTests}.
 *
 * @author Francesco Chicchiriccò
 * @since 6.3.0
 */
@TestPropertySource(properties = "management.endpoint.authenticationPolicies.access=UNRESTRICTED")
@Tag("ActuatorEndpoint")
class RegisteredAuthenticationPoliciesEndpointTests extends AbstractCasEndpointTests {

    @Test
    void verifyOperation() throws Exception {
        mockMvc.perform(get("/actuator/authenticationPolicies")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
        mockMvc.perform(get("/actuator/authenticationPolicies/"
            + AtLeastOneCredentialValidatedAuthenticationPolicy.class.getSimpleName())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }
}
