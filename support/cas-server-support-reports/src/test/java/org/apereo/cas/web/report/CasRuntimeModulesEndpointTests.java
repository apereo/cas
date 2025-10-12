package org.apereo.cas.web.report;

import org.apereo.cas.util.feature.CasRuntimeModule;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link CasRuntimeModulesEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@TestPropertySource(properties = "management.endpoint.casModules.access=UNRESTRICTED")
@Tag("ActuatorEndpoint")
class CasRuntimeModulesEndpointTests extends AbstractCasEndpointTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder().build().toObjectMapper();

    @Test
    void verifyOperation() throws Throwable {
        val modules = MAPPER.readValue(mockMvc.perform(get("/actuator/casModules")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), new TypeReference<List<CasRuntimeModule>>() {
        });
        assertFalse(modules.isEmpty());
        val module = modules.getFirst();
        assertNotNull(module.getName());
        assertNotNull(module.getDescription());
        assertNotNull(module.getVersion());
    }
}
