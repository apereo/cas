package org.apereo.cas.web.report;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.config.CasThymeleafConfiguration;
import org.apereo.cas.config.CasValidationConfiguration;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasProtocolValidationEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@TestPropertySource(properties = "management.endpoint.casValidate.enabled=true")
@Tag("ActuatorEndpoint")
@Import({
    CasProtocolValidationEndpointTests.TestAuthenticationConfiguration.class,
    CasThymeleafConfiguration.class,
    CasValidationConfiguration.class
})
class CasProtocolValidationEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("casProtocolValidationEndpoint")
    private CasProtocolValidationEndpoint endpoint;

    private RegisteredService registeredService;

    @BeforeEach
    public void setup() {
        registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);
    }

    @ParameterizedTest
    @ValueSource(strings = "Mellon")
    @NullAndEmptySource
    void verifyEndpoints(final String password) throws Throwable {
        val request = prepareRequest(password);
        val response = new MockHttpServletResponse();

        endpoint.validate(request, response);
        assertNotNull(response.getContentAsString());

        endpoint.serviceValidate(request, response);
        assertNotNull(response.getContentAsString());

        endpoint.p3ServiceValidate(request, response);
        assertNotNull(response.getContentAsString());
    }

    private HttpServletRequest prepareRequest(final String password) {
        val request = new MockHttpServletRequest();
        request.setParameter(CasProtocolConstants.PARAMETER_USERNAME, "casuser");
        request.setParameter(CasProtocolConstants.PARAMETER_PASSWORD, password);
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, registeredService.getServiceId());
        return request;
    }

    @TestConfiguration(value = "TestAuthenticationConfiguration", proxyBeanMethods = false)
    static class TestAuthenticationConfiguration {
        @Bean
        public AuthenticationEventExecutionPlanConfigurer surrogateAuthenticationEventExecutionPlanConfigurer() {
            return plan -> plan.registerAuthenticationHandler(new AcceptUsersAuthenticationHandler(CollectionUtils.wrap("casuser", "Mellon")));
        }

    }
}

