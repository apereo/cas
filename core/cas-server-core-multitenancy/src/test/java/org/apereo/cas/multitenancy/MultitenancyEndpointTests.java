package org.apereo.cas.multitenancy;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
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

    @SpringBootTestAutoConfigurations
    @SpringBootTest(
        classes = BaseMultitenancyTests.SharedTestConfiguration.class,
        properties = {
            "cas.multitenancy.core.enabled=true",
            "management.endpoints.web.exposure.include=*",
            "management.endpoint.multitenancy.access=UNRESTRICTED"
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @EnableAspectJAutoProxy(proxyTargetClass = false)
    @EnableScheduling
    @AutoConfigureMockMvc
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    abstract static class BaseTests {
        @Autowired
        @Qualifier("mockMvc")
        protected MockMvc mockMvc;
    }

    @Nested
    @TestPropertySource(properties = "cas.multitenancy.json.location=classpath:/tenants.json")
    class DefaultTests extends BaseTests {
        @Test
        void verifyOperation() throws Throwable {
            mockMvc.perform(get("/actuator/multitenancy/tenants")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        }

        @Test
        void verifyOperationById() throws Throwable {
            mockMvc.perform(get("/actuator/multitenancy/tenants/b9584c42")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.multitenancy.json.location=file://${java.io.tmpdir}/ExternalTenants.json")
    class ExternalTests extends BaseTests {
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
