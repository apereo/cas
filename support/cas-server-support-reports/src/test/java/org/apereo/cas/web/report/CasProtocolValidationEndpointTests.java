package org.apereo.cas.web.report;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CasViewConstants;
import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.config.CasThymeleafAutoConfiguration;
import org.apereo.cas.config.CasValidationAutoConfiguration;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

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
    @Autowired
    @Qualifier("casProtocolValidationEndpoint")
    private CasProtocolValidationEndpoint endpoint;

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
        val request = prepareRequest(password);
        val response = new MockHttpServletResponse();

        var mv = endpoint.validate(request, response);
        assertModelAndView(mv);
        mv = endpoint.serviceValidate(request, response);
        assertModelAndView(mv);
        mv = endpoint.p3ServiceValidate(request, response);
        assertModelAndView(mv);
    }

    private static void assertModelAndView(final ModelAndView mv) {
        assertNotNull(mv);
        assertTrue(mv.getStatus().is2xxSuccessful());
        assertTrue(mv.getModel().containsKey(CasViewConstants.MODEL_ATTRIBUTE_REGISTERED_SERVICE));
        assertTrue(mv.getModel().containsKey(CasViewConstants.MODEL_ATTRIBUTE_REGISTERED_SERVICE));
        assertTrue(mv.getModel().containsKey(CasViewConstants.MODEL_ATTRIBUTE_NAME_ASSERTION));
        assertTrue(mv.getModel().containsKey("response"));
    }

    private HttpServletRequest prepareRequest(final String password) {
        val request = new MockHttpServletRequest();
        request.setParameter(CasProtocolConstants.PARAMETER_USERNAME, "casuser");
        request.setParameter(CasProtocolConstants.PARAMETER_PASSWORD, password);
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, registeredService.getServiceId());
        return request;
    }

    @TestConfiguration(value = "AuthenticationTestConfiguration", proxyBeanMethods = false)
    static class AuthenticationTestConfiguration {
        @Bean
        public AuthenticationEventExecutionPlanConfigurer surrogateAuthenticationEventExecutionPlanConfigurer() {
            return plan -> plan.registerAuthenticationHandler(new AcceptUsersAuthenticationHandler(CollectionUtils.wrap("casuser", "Mellon")));
        }

    }
}

