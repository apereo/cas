package org.apereo.cas.rest.authentication;

import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreTicketsSerializationConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.rest.config.CasCoreRestConfiguration;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.util.LinkedMultiValueMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultRestAuthenticationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("Authentication")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    CasCoreRestConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCookieConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketsSerializationConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasDefaultServiceTicketIdGeneratorsConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasMultifactorAuthenticationWebflowConfiguration.class,
    CasCoreMultifactorAuthenticationConfiguration.class,
    DefaultRestAuthenticationServiceTests.TestAuthenticationConfiguration.class,
    CasWebflowContextConfiguration.class,
    CasCoreWebflowConfiguration.class,
    CasCoreTicketsConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DefaultRestAuthenticationServiceTests {
    @Autowired
    @Qualifier(RestAuthenticationService.DEFAULT_BEAN_NAME)
    private RestAuthenticationService restAuthenticationService;

    @Test
    public void verifyAuthentication() {
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();

        val body = new LinkedMultiValueMap<String, String>();
        body.add(RestHttpRequestCredentialFactory.PARAMETER_USERNAME, "casuser");
        body.add(RestHttpRequestCredentialFactory.PARAMETER_PASSWORD, "Mellon");
        val result = restAuthenticationService.authenticate(body, request, response).get();
        assertEquals("casuser", result.getAuthentication().getPrincipal().getId());
    }

    @TestConfiguration(value = "TestAuthenticationConfiguration", proxyBeanMethods = false)
    public static class TestAuthenticationConfiguration {
        @Bean
        public AuthenticationEventExecutionPlanConfigurer surrogateAuthenticationEventExecutionPlanConfigurer() {
            return plan -> plan.registerAuthenticationHandler(new AcceptUsersAuthenticationHandler(CollectionUtils.wrap("casuser", "Mellon")));
        }
    }
}
