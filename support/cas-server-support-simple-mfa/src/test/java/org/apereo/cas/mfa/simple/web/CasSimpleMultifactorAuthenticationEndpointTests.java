package org.apereo.cas.mfa.simple.web;

import module java.base;
import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.mfa.simple.BaseCasSimpleMultifactorAuthenticationTests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.web.report.AbstractCasEndpointTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link CasSimpleMultifactorAuthenticationEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("ActuatorEndpoint")
@TestPropertySource(properties = "management.endpoint.mfaSimple.access=UNRESTRICTED")
@Import({
    CasAuthenticationEventExecutionPlanTestConfiguration.class,
    BaseCasSimpleMultifactorAuthenticationTests.SharedTestConfiguration.class
})
class CasSimpleMultifactorAuthenticationEndpointTests extends AbstractCasEndpointTests {
    @Test
    void verifyGenerateToken() throws Throwable {
        val authorization = EncodingUtils.encodeBase64("casuser:casuser");
        mockMvc.perform(get("/actuator/mfaSimple")
                .param("service", RegisteredServiceTestUtils.CONST_TEST_URL)
                .header("Credential", authorization)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void verifyAuthFails() throws Throwable {
        val authorization = EncodingUtils.encodeBase64("casuser:unknown");
        mockMvc.perform(get("/actuator/mfaSimple")
                .param("service", RegisteredServiceTestUtils.CONST_TEST_URL)
                .header("Credential", authorization)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }
}
