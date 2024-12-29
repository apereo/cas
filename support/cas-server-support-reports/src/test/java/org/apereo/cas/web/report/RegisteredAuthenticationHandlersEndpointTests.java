package org.apereo.cas.web.report;

import org.apereo.cas.authentication.handler.support.ProxyAuthenticationHandler;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link RegisteredAuthenticationHandlersEndpointTests}.
 *
 * @author Francesco Chicchiriccò
 * @since 6.3.0
 */
@TestPropertySource(properties = "management.endpoint.authenticationHandlers.access=UNRESTRICTED")
@Tag("ActuatorEndpoint")
class RegisteredAuthenticationHandlersEndpointTests extends AbstractCasEndpointTests {

    @Test
    void verifyOperation() throws Exception {
        mockMvc.perform(get("/actuator/authenticationHandlers")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
        mockMvc.perform(get("/actuator/authenticationHandlers/" + ProxyAuthenticationHandler.class.getSimpleName())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }
}
