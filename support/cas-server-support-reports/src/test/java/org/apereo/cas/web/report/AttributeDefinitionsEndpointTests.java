package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.authentication.attribute.AttributeDefinition;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.attribute.DefaultAttributeDefinition;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
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

    @Test
    void verifyRegisterDefinition() throws Throwable {
        val defn = DefaultAttributeDefinition.builder()
            .name("my-common-name")
            .key("my-cn")
            .scoped(true)
            .encrypted(false)
            .singleValue(true)
            .build();
        val json = MAPPER.writerFor(new TypeReference<List<AttributeDefinition>>() {})
            .writeValueAsString(List.of(defn));
        mockMvc.perform(post("/actuator/attributeDefinitions")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json)
            )
            .andExpect(status().isOk());
        val registered = attributeDefinitionStore.locateAttributeDefinitionByName("my-common-name");
        assertTrue(registered.isPresent());
    }
}
