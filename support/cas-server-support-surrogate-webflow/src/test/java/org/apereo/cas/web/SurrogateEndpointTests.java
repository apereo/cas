package org.apereo.cas.web;

import module java.base;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.flow.action.BaseSurrogateAuthenticationTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link SurrogateEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@ExtendWith(CasTestExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = BaseSurrogateAuthenticationTests.SharedTestConfiguration.class,
    properties = {
        "cas.monitor.endpoints.endpoint.defaults.access=ANONYMOUS",
        "management.endpoint.impersonation.access=UNRESTRICTED",
        "management.endpoints.web.exposure.include=*",
        "cas.authn.surrogate.simple.surrogates.casuser=cassurrogate"
    },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("ActuatorEndpoint")
class SurrogateEndpointTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Autowired
    @Qualifier("mockMvc")
    private MockMvc mockMvc;

    @Test
    void verifyGetSurrogateAccountsForKnownUser() throws Throwable {
        val content = mockMvc.perform(get("/actuator/impersonation/casuser")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        val accounts = MAPPER.readValue(content, List.class);
        assertFalse(accounts.isEmpty());
        assertTrue(accounts.contains("cassurrogate"));
    }

    @Test
    void verifyGetSurrogateAccountsForUnknownUser() throws Throwable {
        val content = mockMvc.perform(get("/actuator/impersonation/unknown-user")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        val accounts = MAPPER.readValue(content, List.class);
        assertTrue(accounts.isEmpty());
    }
}
