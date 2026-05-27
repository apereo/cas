package org.apereo.cas.consent;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasConsentCoreAutoConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.report.AbstractCasEndpointTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.ObjectMapper;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link AttributeConsentReportEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@TestPropertySource(properties = "management.endpoint.attributeConsent.access=UNRESTRICTED")
@Tag("ActuatorEndpoint")
@ImportAutoConfiguration(CasConsentCoreAutoConfiguration.class)
class AttributeConsentReportEndpointTests extends AbstractCasEndpointTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Autowired
    @Qualifier(ConsentRepository.BEAN_NAME)
    private ConsentRepository consentRepository;

    @Autowired
    @Qualifier(ConsentDecisionBuilder.BEAN_NAME)
    private ConsentDecisionBuilder consentDecisionBuilder;

    @Test
    void verifyOperation() throws Throwable {
        val uid = UUID.randomUUID().toString();
        val desc = consentDecisionBuilder.build(RegisteredServiceTestUtils.getService(),
            RegisteredServiceTestUtils.getRegisteredService(), uid,
            CoreAuthenticationTestUtils.getAttributes());
        consentRepository.storeConsentDecision(desc);

        mockMvc.perform(get("/actuator/attributeConsent/{principal}", uid)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0]").exists());

        mockMvc.perform(get("/actuator/attributeConsent")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0]").exists());

        mockMvc.perform(get("/actuator/attributeConsent/export")
                .accept(MediaType.APPLICATION_OCTET_STREAM))
            .andExpect(status().isOk());

        mockMvc.perform(delete("/actuator/attributeConsent/{principal}/{decisionId}", desc.getPrincipal(), desc.getId())
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        mockMvc.perform(get("/actuator/attributeConsent/{principal}", uid)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void verifyImportOperation() throws Throwable {
        val uid = UUID.randomUUID().toString();
        val toSave = consentDecisionBuilder.build(RegisteredServiceTestUtils.getService(),
            RegisteredServiceTestUtils.getRegisteredService(), uid,
            CoreAuthenticationTestUtils.getAttributes());
        val content = MAPPER.writeValueAsString(toSave);
        mockMvc.perform(post("/actuator/attributeConsent/import")
                .with(csrf())
                .content(content)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
    }
}
