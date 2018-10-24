package org.apereo.cas.support.saml;

import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.ExternalShibbolethIdPAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;

import lombok.val;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * This is {@link ShibbolethIdPEntityIdAuthenticationServiceSelectionStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    ShibbolethIdPEntityIdAuthenticationServiceSelectionStrategyTests.ShibbolethServicesTestConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreUtilConfiguration.class,
    ExternalShibbolethIdPAuthenticationServiceSelectionStrategyConfiguration.class})
public class ShibbolethIdPEntityIdAuthenticationServiceSelectionStrategyTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    @Qualifier("shibbolethIdPEntityIdAuthenticationServiceSelectionStrategy")
    private AuthenticationServiceSelectionStrategy shibbolethIdPEntityIdAuthenticationServiceSelectionStrategy;

    @Test
    public void verifyServiceNotFound() {
        val svc = RegisteredServiceTestUtils.getService("https://www.example.org?param1=value1");
        val result = shibbolethIdPEntityIdAuthenticationServiceSelectionStrategy.resolveServiceFrom(svc);
        assertEquals(svc.getId(), result.getId());
        assertFalse(shibbolethIdPEntityIdAuthenticationServiceSelectionStrategy.supports(svc));
    }

    @Test
    public void verifyServiceFound() {
        val svc = RegisteredServiceTestUtils.getService("https://www.example.org?entityId=https://idp.example.org");
        val result = shibbolethIdPEntityIdAuthenticationServiceSelectionStrategy.resolveServiceFrom(svc);
        assertEquals("https://idp.example.org", result.getId());
    }

    @Test
    public void verifyServiceFoundEncoded() {
        val serviceUrl = "https%3A%2F%2Fidp.example.com%2Fidp%2FAuthn%2FExtCas%3Fconversation%3De1s1&entityId=https%3A%2F%2Fservice.example.com";
        val svc = RegisteredServiceTestUtils.getService(
            "https://cas.example.com/login?service=" + serviceUrl);
        val result = shibbolethIdPEntityIdAuthenticationServiceSelectionStrategy.resolveServiceFrom(svc);
        assertEquals("https://service.example.com", result.getId());
    }

    @TestConfiguration
    public static class ShibbolethServicesTestConfiguration {
        @Bean
        public List inMemoryRegisteredServices() {
            val l = new ArrayList();
            l.add(RegisteredServiceTestUtils.getRegisteredService("https://service.example.com"));
            l.add(RegisteredServiceTestUtils.getRegisteredService("https://idp.example.org"));
            return l;
        }
    }
}
