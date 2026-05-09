package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link CasReleaseAttributesReportEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@TestPropertySource(properties = "management.endpoint.releaseAttributes.access=UNRESTRICTED")
@Tag("ActuatorEndpoint")
class CasReleaseAttributesReportEndpointTests extends AbstractCasEndpointTests {
    private RegisteredService registeredService;

    @BeforeEach
    void setup() {
        registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);
    }

    @ParameterizedTest
    @ValueSource(strings = "casuser")
    @NullAndEmptySource
    void verifyOperation(final String password) throws Throwable {
        val bodyBuilder = new StringBuilder("{");
        bodyBuilder.append("\"username\":\"casuser\",");
        if (password != null) {
            bodyBuilder.append("\"password\":\"").append(password).append("\",");
        }
        bodyBuilder.append("\"service\":\"").append(registeredService.getServiceId()).append("\"");
        bodyBuilder.append('}');

        mockMvc.perform(post("/actuator/releaseAttributes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bodyBuilder.toString())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("casuser"))
            .andExpect(jsonPath("$.attributes").exists());
    }
}

