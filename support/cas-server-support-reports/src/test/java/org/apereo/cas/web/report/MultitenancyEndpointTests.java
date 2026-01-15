package org.apereo.cas.web.report;

import module java.base;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link MultitenancyEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@TestPropertySource(properties = {
    "cas.multitenancy.core.enabled=true",
    "cas.multitenancy.json.location=classpath:/tenants.json",
    "management.endpoint.multitenancy.access=UNRESTRICTED"
})
@Tag("ActuatorEndpoint")
class MultitenancyEndpointTests extends AbstractCasEndpointTests {
    @Test
    void verifyOperation() throws Throwable {
        mockMvc.perform(get("/actuator/multitenancy/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void verifyOperationById() throws Throwable {
        mockMvc.perform(get("/actuator/multitenancy/tenants/shire")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }
}
