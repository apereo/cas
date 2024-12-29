package org.apereo.cas.web.report;

import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link CasResolveAttributesReportEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@TestPropertySource(properties = {
    "cas.authn.attribute-repository.stub.attributes.cn=CAS",
    "cas.authn.attribute-repository.stub.attributes.givenName=casuser",
    "management.endpoint.resolveAttributes.access=UNRESTRICTED"
})
@Tag("ActuatorEndpoint")
class CasResolveAttributesReportEndpointTests extends AbstractCasEndpointTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .build().toObjectMapper();
    
    @Test
    void verifyOperation() throws Throwable {
        val response = MAPPER.readValue(mockMvc.perform(get("/actuator/resolveAttributes/casuser")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), Map.class);
        assertFalse(response.isEmpty());
        assertTrue(response.containsKey("username"));
        assertTrue(response.containsKey("attributes"));
    }
}

