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
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
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
class SamlRegisteredServiceMetadataEndpointTests extends BaseSamlIdPConfigurationTests {
    private static final String FAILING_MANAGER = "FailingMetadataManager";

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
    void verifyManagersHaveNamesForPeople() throws Throwable {
        mockMvc.perform(
                get("/actuator/samlIdPRegisteredServiceMetadata/managers")
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].name", hasItem("In Memory")))
            .andExpect(jsonPath("$[*].name", not(hasItem(InMemorySamlRegisteredServiceMetadataManager.class.getSimpleName()))));
        mockMvc.perform(
                get("/actuator/samlIdPRegisteredServiceMetadata/managers/{name}", "in memory")
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        mockMvc.perform(
                get("/actuator/samlIdPRegisteredServiceMetadata/managers/{name}", InMemorySamlRegisteredServiceMetadataManager.class.getSimpleName())
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void verifyProxiedManagerIsFoundByItsClassName() throws Throwable {
        mockMvc.perform(
                get("/actuator/samlIdPRegisteredServiceMetadata/managers/{name}", "Named Store")
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        mockMvc.perform(
                get("/actuator/samlIdPRegisteredServiceMetadata/managers/{name}", NamedMetadataManager.class.getSimpleName())
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
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
    void verifyUploadMetadataRefused() throws Throwable {
        mockMvc.perform(
                post("/actuator/samlIdPRegisteredServiceMetadata/managers/" + FAILING_MANAGER)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(SamlMetadataDocument.builder()
                        .name(UUID.randomUUID().toString())
                        .value("metadata")
                        .build()
                        .toJson())
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.message").value("The metadata service refused the document"));
    }

    @Test
    void verifyUploadMetadata() throws Throwable {
        val manager = applicationContext.getBeansOfType(SamlRegisteredServiceMetadataManager.class).values()
            .stream()
            .filter(candidate -> !FAILING_MANAGER.equals(candidate.getName()))
            .findFirst()
            .orElseThrow();
        val entry = SamlMetadataDocument.builder()
            .id(RandomUtils.nextLong())
            .name(UUID.randomUUID().toString())
            .signature("some-signature")
            .value("metadata")
            .build();
        mockMvc.perform(
                post("/actuator/samlIdPRegisteredServiceMetadata/managers/{name}", manager.getName())
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(entry.toJson())
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        mockMvc.perform(
                get("/actuator/samlIdPRegisteredServiceMetadata/managers/{name}", manager.getName())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        mockMvc.perform(
                get("/actuator/samlIdPRegisteredServiceMetadata/managers/{name}/{id}", manager.getName(), entry.getId())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        mockMvc.perform(
                delete("/actuator/samlIdPRegisteredServiceMetadata/managers/{name}/{id}", manager.getName(), entry.getId())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        mockMvc.perform(
                get("/actuator/samlIdPRegisteredServiceMetadata/managers/{name}/{id}", manager.getName(), entry.getId())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

    }

    static class NamedMetadataManager extends InMemorySamlRegisteredServiceMetadataManager {
        @Override
        public String getName() {
            return "Named Store";
        }
    }

    @TestConfiguration(value = "SamlRegisteredServiceMetadataTestConfiguration", proxyBeanMethods = false)
    static class SamlRegisteredServiceMetadataTestConfiguration {
        @Bean
        public SamlRegisteredServiceMetadataManager namedSamlRegisteredServiceMetadataManager() {
            val factory = new ProxyFactory(new NamedMetadataManager());
            factory.setProxyTargetClass(true);
            return (SamlRegisteredServiceMetadataManager) factory.getProxy();
        }

        @Bean
        public SamlRegisteredServiceMetadataManager dummySamlRegisteredServiceMetadataManager() {
            return new InMemorySamlRegisteredServiceMetadataManager();
        }

        @Bean
        public SamlRegisteredServiceMetadataManager failingSamlRegisteredServiceMetadataManager() {
            return new InMemorySamlRegisteredServiceMetadataManager() {
                @Override
                public SamlMetadataDocument store(final SamlMetadataDocument document) {
                    throw new IllegalStateException("The metadata service refused the document");
                }

                @Override
                public String getName() {
                    return FAILING_MANAGER;
                }
            };
        }
    }
}
