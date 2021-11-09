package org.apereo.cas.oidc.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;

import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OIDC")
public class OidcMultifactorAuthenticationTriggerTests {

    @TestConfiguration("OidcAuthenticationContextTestConfiguration")
    public static class OidcAuthenticationContextTestConfiguration {
        @Bean
        public MultifactorAuthenticationProvider dummyProvider() {
            return new TestMultifactorAuthenticationProvider();
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    public class NoMultifactorProvidersTests extends AbstractOidcTests {
        @Test
        public void verifyAcrMissingMfa() {
            val service = RegisteredServiceTestUtils.getService();
            val request = new MockHttpServletRequest();
            request.addParameter(OAuth20Constants.ACR_VALUES, "unknown");
            val authn = RegisteredServiceTestUtils.getAuthentication();
            val registeredService = RegisteredServiceTestUtils.getRegisteredService();
            assertThrows(AuthenticationException.class,
                () -> oidcMultifactorAuthenticationTrigger.isActivated(authn, registeredService, request, service));
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @Import(OidcMultifactorAuthenticationTriggerTests.OidcAuthenticationContextTestConfiguration.class)
    @TestPropertySource(properties = "cas.authn.oidc.core.authentication-context-reference-mappings=1->mfa-dummy")
    public class WithMappedMultifactorProvidersTests extends AbstractOidcTests {
        @Test
        public void verifyAcrMfa() {
            TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
            val service = RegisteredServiceTestUtils.getService();
            val request = new MockHttpServletRequest();
            request.addParameter(CasProtocolConstants.PARAMETER_SERVICE,
                String.format("https://app.org?%s=1 2", OAuth20Constants.ACR_VALUES));
            val authn = RegisteredServiceTestUtils.getAuthentication();
            val registeredService = RegisteredServiceTestUtils.getRegisteredService();
            assertTrue(oidcMultifactorAuthenticationTrigger.isActivated(authn,
                registeredService, request, service).isPresent());
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @Import(OidcMultifactorAuthenticationTriggerTests.OidcAuthenticationContextTestConfiguration.class)
    public class WithMultifactorProvidersTests extends AbstractOidcTests {
        @Test
        public void verifyNoAcr() {
            val service = RegisteredServiceTestUtils.getService();
            val request = new MockHttpServletRequest();
            val authn = RegisteredServiceTestUtils.getAuthentication();
            val registeredService = RegisteredServiceTestUtils.getRegisteredService();
            assertTrue(oidcMultifactorAuthenticationTrigger.isActivated(authn, registeredService, request, service).isEmpty());
        }

        @Test
        public void verifyAcrMfa() {
            TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);

            val service = RegisteredServiceTestUtils.getService();
            val request = new MockHttpServletRequest();
            request.addParameter(CasProtocolConstants.PARAMETER_SERVICE,
                String.format("https://app.org?%s=mfa-dummy", OAuth20Constants.ACR_VALUES));
            val authn = RegisteredServiceTestUtils.getAuthentication();
            val registeredService = RegisteredServiceTestUtils.getRegisteredService();
            assertFalse(oidcMultifactorAuthenticationTrigger.isActivated(authn, registeredService, request, service).isEmpty());
        }

        @Test
        public void verifyUrlEncoding() {
            val url = "https://link.test.edu/web/cas?profile=Example Primo&targetURL=abc";
            val request = new MockHttpServletRequest();
            request.setRequestURI("/cas/login");
            request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, url);
            val authn = RegisteredServiceTestUtils.getAuthentication();
            val registeredService = RegisteredServiceTestUtils.getRegisteredService();
            val service = RegisteredServiceTestUtils.getService(url);
            assertTrue(oidcMultifactorAuthenticationTrigger.isActivated(authn, registeredService, request, service).isEmpty());
        }
    }
}
