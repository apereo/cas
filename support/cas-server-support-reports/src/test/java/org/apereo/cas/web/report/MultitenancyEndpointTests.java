package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link MultitenancyEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@ExtendWith(CasTestExtension.class)
@Tag("ActuatorEndpoint")
class MultitenancyEndpointTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Nested
    @TestPropertySource(properties = {
        "cas.multitenancy.core.enabled=true",
        "cas.multitenancy.json.location=classpath:/tenants.json",
        "management.endpoint.multitenancy.access=UNRESTRICTED"
    })
    class DefaultTests extends AbstractCasEndpointTests {
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

    @Nested
    @TestPropertySource(properties = {
        "cas.multitenancy.core.enabled=true",
        "cas.multitenancy.json.location=file://${java.io.tmpdir}/ExternalTenants.json",
        "management.endpoint.multitenancy.access=UNRESTRICTED"
    })
    class ExternalTests extends AbstractCasEndpointTests {
        @Test
        void verifyOperation() throws Throwable {
            val tenant = TenantDefinition.builder()
                .id(UUID.randomUUID().toString())
                .description("External tenant for testing")
                .build();
            mockMvc.perform(post("/actuator/multitenancy/tenants")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(MAPPER.writeValueAsString(tenant))
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tenant.getId()));

            mockMvc.perform(delete("/actuator/multitenancy/tenants/{tenantId}", tenant.getId()))
                .andExpect(status().isNoContent());
            mockMvc.perform(get("/actuator/multitenancy/tenants/{tenantId}", tenant.getId())
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
            mockMvc.perform(delete("/actuator/multitenancy/tenants/{tenantId}", tenant.getId()))
                .andExpect(status().isNotFound());
        }
    }
}
