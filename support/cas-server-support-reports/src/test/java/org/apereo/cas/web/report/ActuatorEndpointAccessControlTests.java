package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.JsonUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link ActuatorEndpointAccessControlTests}, which asserts that
 * {@code management.endpoint.<id>.access=READ_ONLY} means the same thing for both kinds of
 * actuator endpoint CAS ships, once the endpoint is exposed.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("ActuatorEndpoint")
@ExtendWith(CasTestExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = AbstractCasEndpointTests.SharedTestConfiguration.class,
    properties = {
        "management.endpoints.web.exposure.include=registeredServices,releaseAttributes,loggingConfig",

        "management.endpoint.registeredServices.access=READ_ONLY",
        "management.endpoint.releaseAttributes.access=READ_ONLY",
        "management.endpoint.loggingConfig.access=NONE",

        "cas.monitor.endpoints.endpoint.loggingConfig.access=PERMIT"
    },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ActuatorEndpointAccessControlTests extends AbstractCasEndpointTests {

    @Test
    void verifyReadOnlyAccessRefusesWritesForOperationEndpoints() throws Throwable {
        mockMvc.perform(post("/actuator/releaseAttributes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.render(Map.of(
                    "username", "casuser",
                    "service", "https://github.com/apereo/cas"))))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void verifyReadOnlyAccessAllowsOnlyGetMappings() throws Throwable {
        val service = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(service);

        mockMvc.perform(get("/actuator/registeredServices")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        mockMvc.perform(get("/actuator/registeredServices/{id}", service.getServiceId())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        val content = new RegisteredServiceJsonSerializer(applicationContext)
            .toString(RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString()));
        mockMvc.perform(post("/actuator/registeredServices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
            .andExpect(status().is4xxClientError());
        mockMvc.perform(put("/actuator/registeredServices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
            .andExpect(status().is4xxClientError());
        mockMvc.perform(delete("/actuator/registeredServices/{id}", service.getServiceId())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError());
        mockMvc.perform(delete("/actuator/registeredServices/cache")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void verifyAccessOfNoneRegistersNoMappings() throws Throwable {
        mockMvc.perform(get("/actuator/loggingConfig")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError());
    }
}
