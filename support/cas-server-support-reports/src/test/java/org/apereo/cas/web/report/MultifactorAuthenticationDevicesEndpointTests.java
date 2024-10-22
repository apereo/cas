package org.apereo.cas.web.report;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link MultifactorAuthenticationDevicesEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@TestPropertySource(properties = "management.endpoint.mfaDevices.access=UNRESTRICTED")
@Tag("ActuatorEndpoint")
@Import(MultifactorAuthenticationDevicesEndpointTests.MultifactorProviderTestConfiguration.class)
class MultifactorAuthenticationDevicesEndpointTests extends AbstractCasEndpointTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Test
    void verifyOperation() throws Throwable {
        val content = mockMvc.perform(get("/actuator/mfaDevices/casuser")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        val results = MAPPER.readValue(content, List.class);
        assertFalse(results.isEmpty());
    }

    @Test
    void verifyDelete() throws Throwable {
        mockMvc.perform(delete("/actuator/mfaDevices/casuser/%s/%s"
                .formatted(TestMultifactorAuthenticationProvider.ID, UUID.randomUUID().toString()))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @TestConfiguration(value = "MultifactorProviderTestConfiguration", proxyBeanMethods = false)
    static class MultifactorProviderTestConfiguration {
        @Bean
        public MultifactorAuthenticationProvider dummyProvider() {
            return new TestMultifactorAuthenticationProvider();
        }
    }
}
