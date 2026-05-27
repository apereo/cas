package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.config.CasThymeleafAutoConfiguration;
import org.apereo.cas.config.CasValidationAutoConfiguration;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link CasProtocolValidationEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@TestPropertySource(properties = "management.endpoint.casValidate.access=UNRESTRICTED")
@Tag("ActuatorEndpoint")
@Import(CasProtocolValidationEndpointTests.AuthenticationTestConfiguration.class)
@ImportAutoConfiguration({
    CasThymeleafAutoConfiguration.class,
    CasValidationAutoConfiguration.class
})
class CasProtocolValidationEndpointTests extends AbstractCasEndpointTests {
    private RegisteredService registeredService;

    @BeforeEach
    void setup() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("185.86.151.11");
        request.setLocalAddr("185.88.151.11");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));

        registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);
    }

    @ParameterizedTest
    @ValueSource(strings = "Mellon")
    @NullAndEmptySource
    void verifyEndpoints(final String password) throws Throwable {
        val passwordParam = StringUtils.defaultString(password);
        val service = registeredService.getServiceId();

        mockMvc.perform(post("/actuator/casValidate/validate")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param(CasProtocolConstants.PARAMETER_USERNAME, "casuser")
                .param(CasProtocolConstants.PARAMETER_PASSWORD, passwordParam)
                .param(CasProtocolConstants.PARAMETER_SERVICE, service)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.response").exists());

        mockMvc.perform(post("/actuator/casValidate/serviceValidate")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param(CasProtocolConstants.PARAMETER_USERNAME, "casuser")
                .param(CasProtocolConstants.PARAMETER_PASSWORD, passwordParam)
                .param(CasProtocolConstants.PARAMETER_SERVICE, service)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.response").exists());

        mockMvc.perform(post("/actuator/casValidate/p3/serviceValidate")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param(CasProtocolConstants.PARAMETER_USERNAME, "casuser")
                .param(CasProtocolConstants.PARAMETER_PASSWORD, passwordParam)
                .param(CasProtocolConstants.PARAMETER_SERVICE, service)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.response").exists());
    }

    @TestConfiguration(value = "AuthenticationTestConfiguration", proxyBeanMethods = false)
    static class AuthenticationTestConfiguration {
        @Bean
        public AuthenticationEventExecutionPlanConfigurer surrogateAuthenticationEventExecutionPlanConfigurer() {
            return plan -> plan.registerAuthenticationHandler(new AcceptUsersAuthenticationHandler(CollectionUtils.wrap("casuser", "Mellon")));
        }
    }
}



