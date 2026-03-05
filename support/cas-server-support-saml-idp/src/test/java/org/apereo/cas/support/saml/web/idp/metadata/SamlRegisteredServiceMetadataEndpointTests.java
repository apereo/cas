package org.apereo.cas.support.saml.web.idp.metadata;

import module java.base;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.InMemorySamlRegisteredServiceMetadataManager;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataManager;
import org.apereo.cas.util.RandomUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link SamlRegisteredServiceMetadataEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("SAMLMetadata")
@TestPropertySource(properties = {
    "management.endpoints.web.exposure.include=*",
    "management.endpoint.samlIdPRegisteredServiceMetadata.access=UNRESTRICTED"
})
@Import(SamlRegisteredServiceMetadataEndpointTests.SamlRegisteredServiceMetadataTestConfiguration.class)
@Execution(ExecutionMode.SAME_THREAD)
class SamlRegisteredServiceMetadataEndpointTests extends BaseSamlIdPConfigurationTests {
    @Test
    void verifyManagers() throws Throwable {
        mockMvc.perform(
                get("/actuator/samlIdPRegisteredServiceMetadata/managers")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

    @Test
    void verifyUnknownManager() throws Throwable {
        mockMvc.perform(
                post("/actuator/samlIdPRegisteredServiceMetadata/managers/unknown")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(SamlMetadataDocument.builder()
                        .name(UUID.randomUUID().toString())
                        .signature("some-signature")
                        .value("metadata")
                        .build()
                        .toJson())
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
    
    @Test
    void verifyUploadMetadata() throws Throwable {
        val manager = applicationContext.getBeansOfType(SamlRegisteredServiceMetadataManager.class).values().iterator().next();
        val entry = SamlMetadataDocument.builder()
            .id(RandomUtils.nextLong())
            .name(UUID.randomUUID().toString())
            .signature("some-signature")
            .value("metadata")
            .build();
        mockMvc.perform(
                post("/actuator/samlIdPRegisteredServiceMetadata/managers/" + manager.getName())
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(entry.toJson())
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        mockMvc.perform(
                get("/actuator/samlIdPRegisteredServiceMetadata/managers/" + manager.getName())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        mockMvc.perform(
                get("/actuator/samlIdPRegisteredServiceMetadata/managers/" + manager.getName() + '/' + entry.getId())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        mockMvc.perform(
                delete("/actuator/samlIdPRegisteredServiceMetadata/managers/" + manager.getName() + '/' + entry.getId())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        mockMvc.perform(
                get("/actuator/samlIdPRegisteredServiceMetadata/managers/" + manager.getName() + '/' + entry.getId())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

    }

    @TestConfiguration(value = "SamlRegisteredServiceMetadataTestConfiguration", proxyBeanMethods = false)
    static class SamlRegisteredServiceMetadataTestConfiguration {
        @Bean
        public SamlRegisteredServiceMetadataManager dummySamlRegisteredServiceMetadataManager() {
            return new InMemorySamlRegisteredServiceMetadataManager();
        }
    }
}
