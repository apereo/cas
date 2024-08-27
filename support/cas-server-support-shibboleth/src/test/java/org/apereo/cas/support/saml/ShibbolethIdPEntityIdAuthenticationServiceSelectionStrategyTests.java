package org.apereo.cas.support.saml;

import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasShibbolethIdPAutoConfiguration;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ShibbolethIdPEntityIdAuthenticationServiceSelectionStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreServicesAutoConfiguration.class,
    ShibbolethIdPEntityIdAuthenticationServiceSelectionStrategyTests.ShibbolethServicesTestConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreCookieAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasShibbolethIdPAutoConfiguration.class
}, properties = "cas.authn.shib-idp.server-url=https://sso.shibboleth.org/idp/Authn/External")
@Tag("SAML")
@ExtendWith(CasTestExtension.class)
class ShibbolethIdPEntityIdAuthenticationServiceSelectionStrategyTests {
    @Autowired
    @Qualifier("shibbolethIdPEntityIdAuthenticationServiceSelectionStrategy")
    private AuthenticationServiceSelectionStrategy shibbolethIdPEntityIdAuthenticationServiceSelectionStrategy;

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
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
        assertEquals(svc.getOriginalUrl(), result.getFirstAttribute(Service.class.getName(), String.class));
    }

    @Test
    void verifyQueryStrings() throws Throwable {
        val svc = RegisteredServiceTestUtils.getService("https://www.example.org?name=value");
        val context = MockRequestContext.create(applicationContext);
        context.setQueryString("entityId=https://idp.example.org");

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
