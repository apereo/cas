package org.apereo.cas.trusted.web;

import module java.base;
import org.apereo.cas.config.CasMultifactorAuthnTrustAutoConfiguration;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.report.AbstractCasEndpointTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.ObjectMapper;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link MultifactorAuthenticationTrustedDevicesReportEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@TestPropertySource(properties = "management.endpoint.multifactorTrustedDevices.access=UNRESTRICTED")
@Tag("MFATrustedDevices")
@ImportAutoConfiguration(CasMultifactorAuthnTrustAutoConfiguration.class)
class MultifactorAuthenticationTrustedDevicesReportEndpointTests extends AbstractCasEndpointTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Autowired
    @Qualifier(MultifactorAuthenticationTrustStorage.BEAN_NAME)
    private MultifactorAuthenticationTrustStorage mfaTrustEngine;

    @Test
    void verifyRemovals() throws Throwable {
        var record = MultifactorAuthenticationTrustRecord.newInstance(UUID.randomUUID().toString(), "geography", "fingerprint");
        mfaTrustEngine.save(record);

        mockMvc.perform(delete("/actuator/multifactorTrustedDevices/clean")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        val date = LocalDateTime.now(Clock.systemUTC()).plusDays(1);
        mockMvc.perform(delete("/actuator/multifactorTrustedDevices/expire")
                .with(csrf())
                .queryParam("expiration", date.toString())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        mockMvc.perform(get("/actuator/multifactorTrustedDevices")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()", greaterThan(0)));
    }

    @Test
    void verifyOperation() throws Throwable {
        var record = MultifactorAuthenticationTrustRecord.newInstance(UUID.randomUUID().toString(), "geography", "fingerprint");
        record = mfaTrustEngine.save(record);
        mockMvc.perform(get("/actuator/multifactorTrustedDevices")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()", greaterThan(0)));
        mockMvc.perform(get("/actuator/multifactorTrustedDevices/{username}", record.getPrincipal())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()", greaterThan(0)));

        mockMvc.perform(delete("/actuator/multifactorTrustedDevices/{key}", record.getRecordKey())
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        mockMvc.perform(get("/actuator/multifactorTrustedDevices")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(get("/actuator/multifactorTrustedDevices/{username}", record.getPrincipal())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void verifyImportExport() throws Throwable {
        var record = MultifactorAuthenticationTrustRecord.newInstance(
            UUID.randomUUID().toString(), "london", "fingerprint");
        val content = MAPPER.writeValueAsString(record);
        mockMvc.perform(post("/actuator/multifactorTrustedDevices/import")
                .with(csrf())
                .content(content)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
        mockMvc.perform(get("/actuator/multifactorTrustedDevices/export")
                .accept(MediaType.APPLICATION_OCTET_STREAM))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, startsWith("attachment")));
        mockMvc.perform(get("/actuator/multifactorTrustedDevices/export/{username}", record.getPrincipal())
                .accept(MediaType.APPLICATION_OCTET_STREAM))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, startsWith("attachment")));
    }
}
