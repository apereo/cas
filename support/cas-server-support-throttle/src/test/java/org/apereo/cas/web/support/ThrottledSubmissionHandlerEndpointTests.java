package org.apereo.cas.web.support;

import module java.base;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.report.AbstractCasEndpointTests;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link ThrottledSubmissionHandlerEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@TestPropertySource(properties = {
    "cas.authn.throttle.failure.range-seconds=5",
    "management.endpoint.throttles.access=UNRESTRICTED"
})
@Import(BaseThrottledSubmissionHandlerInterceptorAdapterTests.SharedTestConfiguration.class)
@Tag("ActuatorEndpoint")
class ThrottledSubmissionHandlerEndpointTests extends AbstractCasEndpointTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Autowired
    @Qualifier(ThrottledSubmissionHandlerInterceptor.BEAN_NAME)
    private ThrottledSubmissionHandlerInterceptor throttle;

    @Test
    void verifyOperation() throws Throwable {
        var result = MAPPER.readValue(mockMvc.perform(get("/actuator/throttles")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(), List.class);
        assertTrue(result.isEmpty());

        val request = new MockHttpServletRequest();
        request.setRemoteAddr("1.2.3.4");
        request.setLocalAddr("4.5.6.7");
        request.setRemoteUser("cas");
        request.addHeader(HttpHeaders.USER_AGENT, "Firefox");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));

        throttle.recordSubmissionFailure(request);

        val records = MAPPER.readValue(mockMvc.perform(get("/actuator/throttles")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(), new TypeReference<List<ThrottledSubmission>>() {
        });
        assertFalse(records.isEmpty());

        mockMvc.perform(delete("/actuator/throttles")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("key", records.getFirst().getKey())
            )
            .andExpect(status().isOk());

        mockMvc.perform(delete("/actuator/throttles")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("clear", "true")
            )
            .andExpect(status().isOk());

        mockMvc.perform(delete("/actuator/throttles")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("clear", "false")
            )
            .andExpect(status().isOk());
    }
}
