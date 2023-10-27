package org.apereo.cas.support.saml;

import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.CasCookieConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreLogoutConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreTicketsSerializationConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.config.ExternalShibbolethIdPAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ShibbolethIdPEntityIdAuthenticationServiceSelectionStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    ShibbolethIdPEntityIdAuthenticationServiceSelectionStrategyTests.ShibbolethServicesTestConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketsSerializationConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCookieConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreConfiguration.class,
    ExternalShibbolethIdPAuthenticationServiceSelectionStrategyConfiguration.class
}, properties = "cas.authn.shib-idp.server-url=https://sso.shibboleth.org/idp/Authn/External")
@Tag("SAML")
class ShibbolethIdPEntityIdAuthenticationServiceSelectionStrategyTests {
    @Autowired
    @Qualifier("shibbolethIdPEntityIdAuthenticationServiceSelectionStrategy")
    private AuthenticationServiceSelectionStrategy shibbolethIdPEntityIdAuthenticationServiceSelectionStrategy;

    @Test
    void verifyServiceAttribute() throws Throwable {
        val svc = RegisteredServiceTestUtils.getService("https://sso.shibboleth.org/idp/Authn/External");
        val entityId = "https://service.example.com";
        svc.getAttributes().put(SamlProtocolConstants.PARAMETER_ENTITY_ID, List.of(entityId));
        assertTrue(shibbolethIdPEntityIdAuthenticationServiceSelectionStrategy.supports(svc));
        val result = shibbolethIdPEntityIdAuthenticationServiceSelectionStrategy.resolveServiceFrom(svc);
        assertEquals(entityId, result.getId());
    }

    @Test
    void verifyServiceNotFound() throws Throwable {
        val svc = RegisteredServiceTestUtils.getService("https://www.example.org?param1=value1");
        val result = shibbolethIdPEntityIdAuthenticationServiceSelectionStrategy.resolveServiceFrom(svc);
        assertEquals(svc.getId(), result.getId());
        assertFalse(shibbolethIdPEntityIdAuthenticationServiceSelectionStrategy.supports(svc));
    }

    @Test
    void verifyServiceFound() throws Throwable {
        val svc = RegisteredServiceTestUtils.getService("https://www.example.org?entityId=https://idp.example.org");
        val result = shibbolethIdPEntityIdAuthenticationServiceSelectionStrategy.resolveServiceFrom(svc);
        assertEquals("https://idp.example.org", result.getId());
        assertEquals(Ordered.HIGHEST_PRECEDENCE, shibbolethIdPEntityIdAuthenticationServiceSelectionStrategy.getOrder());
    }

    @Test
    void verifyServiceFoundEncoded() throws Throwable {
        val serviceUrl = "https%3A%2F%2Fidp.example.com%2Fidp%2FAuthn%2FExtCas%3Fconversation%3De1s1&entityId=https%3A%2F%2Fservice.example.com";
        val svc = RegisteredServiceTestUtils.getService(
            "https://cas.example.com/login?service=" + serviceUrl);
        val result = shibbolethIdPEntityIdAuthenticationServiceSelectionStrategy.resolveServiceFrom(svc);
        assertEquals("https://service.example.com", result.getId());
        assertEquals(svc.getOriginalUrl(), result.getAttributes().get(Service.class.getName()).get(0).toString());
    }

    @Test
    void verifyQueryStrings() throws Throwable {
        val svc = RegisteredServiceTestUtils.getService("https://www.example.org?name=value");
        val context = new MockRequestContext();

        val request = new MockHttpServletRequest();
        request.setQueryString("entityId=https://idp.example.org");

        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        var result = shibbolethIdPEntityIdAuthenticationServiceSelectionStrategy.resolveServiceFrom(svc);
        assertEquals("https://idp.example.org", result.getId());

        val svc2 = RegisteredServiceTestUtils.getService("_ _");
        result = shibbolethIdPEntityIdAuthenticationServiceSelectionStrategy.resolveServiceFrom(svc2);
        assertEquals("_ _", result.getId());
    }

    @TestConfiguration(value = "ShibbolethServicesTestConfiguration", proxyBeanMethods = false)
    static class ShibbolethServicesTestConfiguration {
        @Bean
        public List inMemoryRegisteredServices() {
            val services = new ArrayList<RegisteredService>();
            services.add(RegisteredServiceTestUtils.getRegisteredService("https://service.example.com"));
            services.add(RegisteredServiceTestUtils.getRegisteredService("https://idp.example.org"));
            return services;
        }
    }
}
