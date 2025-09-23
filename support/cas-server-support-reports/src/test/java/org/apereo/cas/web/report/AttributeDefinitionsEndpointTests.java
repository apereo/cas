package org.apereo.cas.web.report;

import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.attribute.DefaultAttributeDefinition;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link AttributeDefinitionsEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@ExtendWith(CasTestExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = AbstractCasEndpointTests.SharedTestConfiguration.class,
    properties = {
        "cas.monitor.endpoints.endpoint.defaults.access=ANONYMOUS",
        "management.endpoint.attributeDefinitions.access=UNRESTRICTED",
        "management.endpoints.web.exposure.include=*"
    },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("ActuatorEndpoint")
class AttributeDefinitionsEndpointTests extends AbstractCasEndpointTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Autowired
    @Qualifier(AttributeDefinitionStore.BEAN_NAME)
    private AttributeDefinitionStore attributeDefinitionStore;
    
    @Test
    void verifyAllFetchDefinitions() throws Throwable {
        attributeDefinitionStore.registerAttributeDefinition(
            DefaultAttributeDefinition.builder()
                .name("commonName")
                .key("cn")
                .scoped(true)
                .encrypted(false)
                .singleValue(true)
                .build()
        );
        val result = MAPPER.readValue(mockMvc.perform(get("/actuator/attributeDefinitions")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(), List.class);
        assertFalse(result.isEmpty());
    }
}
