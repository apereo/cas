package org.apereo.cas.support.saml.web.idp.metadata;

import module java.base;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link SamlRegisteredServiceCachedMetadataEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLMetadata")
@TestPropertySource(properties = {
    "management.endpoints.web.exposure.include=*",
    "management.endpoint.samlIdPRegisteredServiceMetadataCache.access=UNRESTRICTED"
})
@Execution(ExecutionMode.SAME_THREAD)
class SamlRegisteredServiceCachedMetadataEndpointTests extends BaseSamlIdPConfigurationTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .build().toObjectMapper();
    
    private SamlRegisteredService samlRegisteredService;

    @BeforeEach
    void beforeEach() {
        samlRegisteredService = SamlIdPTestUtils.getSamlRegisteredService();
        servicesManager.save(samlRegisteredService);
    }

    @Test
    void verifyInvalidate() throws Throwable {
        mockMvc.perform(delete("/actuator/samlIdPRegisteredServiceMetadataCache")
            .queryParam("serviceId", samlRegisteredService.getServiceId())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent());
        
        mockMvc.perform(delete("/actuator/samlIdPRegisteredServiceMetadataCache")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent());
    }

    @Test
    void verifyInvalidateByEntityId() throws Throwable {
        mockMvc.perform(get("/actuator/samlIdPRegisteredServiceMetadataCache")
                .queryParam("serviceId", samlRegisteredService.getServiceId())
                .queryParam("entityId", samlRegisteredService.getServiceId())
                .queryParam("force", "true")
                .queryParam("includeMetadata", "true")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());

        mockMvc.perform(delete("/actuator/samlIdPRegisteredServiceMetadataCache")
            .queryParam("serviceId", samlRegisteredService.getServiceId())
            .queryParam("entityId", samlRegisteredService.getServiceId())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent());
        
        val body = MAPPER.readValue(mockMvc.perform(get("/actuator/samlIdPRegisteredServiceMetadataCache")
            .queryParam("serviceId", samlRegisteredService.getServiceId())
            .queryParam("entityId", samlRegisteredService.getServiceId())
            .queryParam("force", "false")
            .queryParam("includeMetadata", "true")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString(), Map.class);
        assertFalse(body.containsKey(samlRegisteredService.getServiceId()));
    }

    @Test
    void verifyCachedMetadataObject() throws Throwable {
        val body = MAPPER.readValue(mockMvc.perform(get("/actuator/samlIdPRegisteredServiceMetadataCache")
            .queryParam("serviceId", samlRegisteredService.getServiceId())
            .queryParam("force", "true")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), Map.class);
        assertTrue(body.containsKey(samlRegisteredService.getServiceId()));
    }

    @Test
    void verifyCachedServiceWithoutResolution() throws Throwable {
        val body = MAPPER.readValue(mockMvc.perform(get("/actuator/samlIdPRegisteredServiceMetadataCache")
            .queryParam("serviceId", samlRegisteredService.getName())
            .queryParam("entityId", UUID.randomUUID().toString())
            .queryParam("force", "false")
            .queryParam("includeMetadata", "true")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString(), Map.class);
        assertFalse(body.containsKey(samlRegisteredService.getServiceId()));
    }

    @Test
    void verifyCachedService() throws Throwable {
        var body = MAPPER.readValue(mockMvc.perform(get("/actuator/samlIdPRegisteredServiceMetadataCache")
            .queryParam("serviceId", String.valueOf(samlRegisteredService.getId()))
            .queryParam("force", "true")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), Map.class);
        assertTrue(body.containsKey(samlRegisteredService.getServiceId()));

        body = MAPPER.readValue(mockMvc.perform(get("/actuator/samlIdPRegisteredServiceMetadataCache")
            .queryParam("serviceId", String.valueOf(samlRegisteredService.getId()))
            .queryParam("force", "false")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), Map.class);
        assertTrue(body.containsKey(samlRegisteredService.getServiceId()));
    }

    @Test
    void verifyBadService() throws Throwable {
        val body = MAPPER.readValue(mockMvc.perform(get("/actuator/samlIdPRegisteredServiceMetadataCache")
            .queryParam("serviceId", UUID.randomUUID().toString())
            .queryParam("force", "false")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString(), Map.class);
        assertTrue(body.containsKey("error"));
    }
}
