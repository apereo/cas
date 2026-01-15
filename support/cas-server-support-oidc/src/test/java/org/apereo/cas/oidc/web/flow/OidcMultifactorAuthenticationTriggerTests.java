package org.apereo.cas.oidc.web.flow;

import module java.base;
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
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OIDC")
class OidcMultifactorAuthenticationTriggerTests {

    @TestConfiguration(value = "OidcAuthenticationContextTestConfiguration", proxyBeanMethods = false)
    static class OidcAuthenticationContextTestConfiguration {
        @Bean
        public MultifactorAuthenticationProvider dummyProvider() {
            return new TestMultifactorAuthenticationProvider();
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.oidc.discovery.acr-values-supported=unknown")
    class NoMultifactorProvidersTests extends AbstractOidcTests {
        @Test
        void verifyAcrMissingMfa() {
            val service = RegisteredServiceTestUtils.getService();
            val request = new MockHttpServletRequest();
            request.addParameter(OAuth20Constants.ACR_VALUES, "unknown");
            val authn = RegisteredServiceTestUtils.getAuthentication();
            val registeredService = RegisteredServiceTestUtils.getRegisteredService();
            assertThrows(AuthenticationException.class,
                () -> oidcMultifactorAuthenticationTrigger.isActivated(authn, registeredService, request,
                    new MockHttpServletResponse(), service));
        }
    }

    @Nested
    @Import(OidcMultifactorAuthenticationTriggerTests.OidcAuthenticationContextTestConfiguration.class)
    @TestPropertySource(properties = {
        "cas.authn.oidc.discovery.acr-values-supported=1,2",
        "cas.authn.oidc.core.authentication-context-reference-mappings=1->mfa-dummy"
    })
    class WithMappedMultifactorProvidersTests extends AbstractOidcTests {
        @Test
        void verifyAcrMfa() throws Throwable {
            TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
            val service = RegisteredServiceTestUtils.getService();
            val request = new MockHttpServletRequest();
            request.addParameter(CasProtocolConstants.PARAMETER_SERVICE,
                String.format("https://app.org?%s=1 2", OAuth20Constants.ACR_VALUES));
            val authn = RegisteredServiceTestUtils.getAuthentication();
            val registeredService = RegisteredServiceTestUtils.getRegisteredService();
            assertTrue(oidcMultifactorAuthenticationTrigger.isActivated(authn,
                registeredService, request, new MockHttpServletResponse(), service).isPresent());
        }

        @Test
        void verifyUnsupportedAcr() throws Throwable {
            TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
            val service = RegisteredServiceTestUtils.getService();
            val request = new MockHttpServletRequest();
            request.addParameter(CasProtocolConstants.PARAMETER_SERVICE,
                String.format("https://app.org?%s=mfa-dummy", OAuth20Constants.ACR_VALUES));
            val authn = RegisteredServiceTestUtils.getAuthentication();
            val registeredService = RegisteredServiceTestUtils.getRegisteredService();
            assertFalse(oidcMultifactorAuthenticationTrigger.isActivated(authn,
                registeredService, request, new MockHttpServletResponse(), service).isPresent());
        }
    }

    @Nested
    @Import(OidcMultifactorAuthenticationTriggerTests.OidcAuthenticationContextTestConfiguration.class)
    @TestPropertySource(properties = "cas.authn.oidc.discovery.acr-values-supported=mfa-dummy")
    class WithMultifactorProvidersTests extends AbstractOidcTests {
        @Test
        void verifyNoAcr() throws Throwable {
            val service = RegisteredServiceTestUtils.getService();
            val request = new MockHttpServletRequest();
            val authn = RegisteredServiceTestUtils.getAuthentication();
            val registeredService = RegisteredServiceTestUtils.getRegisteredService();
            assertTrue(oidcMultifactorAuthenticationTrigger.isActivated(authn, registeredService,
                request, new MockHttpServletResponse(), service).isEmpty());
        }

        @Test
        void verifyAcrMfa() throws Throwable {
            TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);

            val service = RegisteredServiceTestUtils.getService();
            val request = new MockHttpServletRequest();
            request.addParameter(CasProtocolConstants.PARAMETER_SERVICE,
                String.format("https://app.org?%s=mfa-dummy", OAuth20Constants.ACR_VALUES));
            val authn = RegisteredServiceTestUtils.getAuthentication();
            val registeredService = RegisteredServiceTestUtils.getRegisteredService();
            assertFalse(oidcMultifactorAuthenticationTrigger.isActivated(authn, registeredService,
                request, new MockHttpServletResponse(), service).isEmpty());
        }

        @Test
        void verifyUrlEncoding() throws Throwable {
            val url = "https://link.test.edu/web/cas?profile=Example Primo&targetURL=abc";
            val request = new MockHttpServletRequest();
            request.setRequestURI("/cas/login");
            request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, url);
            val authn = RegisteredServiceTestUtils.getAuthentication();
            val registeredService = RegisteredServiceTestUtils.getRegisteredService();
            val service = RegisteredServiceTestUtils.getService(url);
            assertTrue(oidcMultifactorAuthenticationTrigger.isActivated(authn, registeredService,
                request, new MockHttpServletResponse(), service).isEmpty());
        }
    }
}
