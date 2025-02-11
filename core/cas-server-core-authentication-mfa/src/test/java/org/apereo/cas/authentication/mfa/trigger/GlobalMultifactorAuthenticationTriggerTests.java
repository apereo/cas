package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GlobalMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("MFATrigger")
class GlobalMultifactorAuthenticationTriggerTests {

    @Nested
    @TestPropertySource(properties = "cas.authn.mfa.triggers.global.global-provider-id=mfa-dummy")
    class NoProviderTests extends BaseMultifactorAuthenticationTriggerTests {
        @Test
        void verifyNoProvider() {
            assertThrows(AuthenticationException.class,
                () -> globalMultifactorAuthenticationTrigger.isActivated(
                    CoreAuthenticationTestUtils.getAuthentication(), CoreAuthenticationTestUtils.getRegisteredService(),
                    new MockHttpServletRequest(), new MockHttpServletResponse(), CoreAuthenticationTestUtils.getService()));
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.mfa.triggers.global.global-provider-id=mfa-dummy")
    @Import(BaseMultifactorAuthenticationTriggerTests.TestMultifactorTestConfiguration.class)
    class ByProviderTests extends BaseMultifactorAuthenticationTriggerTests {
        @Test
        void verifyNoProvider() throws Throwable {
            val result = globalMultifactorAuthenticationTrigger.isActivated(
                CoreAuthenticationTestUtils.getAuthentication(), CoreAuthenticationTestUtils.getRegisteredService(),
                new MockHttpServletRequest(), new MockHttpServletResponse(), CoreAuthenticationTestUtils.getService());
            assertTrue(result.isPresent());
        }
    }

    @Nested
    @Import(BaseMultifactorAuthenticationTriggerTests.TestMultifactorTestConfiguration.class)
    @TestPropertySource(properties = {
        "cas.multitenancy.core.enabled=true",
        "cas.multitenancy.json.location=classpath:/tenants.json"
    })
    class MultitenancyTests extends BaseMultifactorAuthenticationTriggerTests {
        @Test
        void verifyProvider() throws Throwable {
            val request = new MockHttpServletRequest();
            request.setContextPath("/tenants/shire/login");
            val result = globalMultifactorAuthenticationTrigger.isActivated(
                CoreAuthenticationTestUtils.getAuthentication(), CoreAuthenticationTestUtils.getRegisteredService(),
                request, new MockHttpServletResponse(), CoreAuthenticationTestUtils.getService());
            assertTrue(result.isPresent());
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.mfa.triggers.global.global-provider-id=mfa-dummy,mfa-invalid")
    @Import(BaseMultifactorAuthenticationTriggerTests.TestMultifactorTestConfiguration.class)
    class ManyProvidersTests extends BaseMultifactorAuthenticationTriggerTests {
        @Test
        void verifyOperationByManyProviders() throws Throwable {
            assertThrows(AuthenticationException.class,
                () -> globalMultifactorAuthenticationTrigger.isActivated(
                    CoreAuthenticationTestUtils.getAuthentication(), CoreAuthenticationTestUtils.getRegisteredService(),
                    new MockHttpServletRequest(), new MockHttpServletResponse(), CoreAuthenticationTestUtils.getService()));
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.mfa.triggers.global.global-provider-id=mfa-dummy,mfa-simple")
    @Import({
        GlobalMultifactorAuthenticationTriggerTests.SecondMultifactorTestConfiguration.class,
        BaseMultifactorAuthenticationTriggerTests.TestMultifactorTestConfiguration.class
    })
    class ValidProvidersTests extends BaseMultifactorAuthenticationTriggerTests {
        @Autowired
        @Qualifier("simpleProvider")
        private MultifactorAuthenticationProvider simpleProvider;
        
        @Test
        void verifyOperationByValidProviders() throws Throwable {
            val result = globalMultifactorAuthenticationTrigger.isActivated(
                CoreAuthenticationTestUtils.getAuthentication(), CoreAuthenticationTestUtils.getRegisteredService(),
                new MockHttpServletRequest(), new MockHttpServletResponse(), CoreAuthenticationTestUtils.getService());
            assertTrue(result.isPresent());
            assertEquals(simpleProvider.getId(), result.get().getId());
        }
    }

    @Nested
    class UndefinedProviderTests extends BaseMultifactorAuthenticationTriggerTests {
        @Test
        void verifyNoProvider() throws Throwable {
            val result = globalMultifactorAuthenticationTrigger.isActivated(
                CoreAuthenticationTestUtils.getAuthentication(), CoreAuthenticationTestUtils.getRegisteredService(),
                new MockHttpServletRequest(), new MockHttpServletResponse(), CoreAuthenticationTestUtils.getService());
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.mfa.triggers.global.global-provider-id=mfa-unknown")
    class UnresolvedProviderTests extends BaseMultifactorAuthenticationTriggerTests {
        @Test
        void verifyProvider() throws Throwable {
            assertThrows(AuthenticationException.class,
                () -> globalMultifactorAuthenticationTrigger.isActivated(
                    CoreAuthenticationTestUtils.getAuthentication(), CoreAuthenticationTestUtils.getRegisteredService(),
                    new MockHttpServletRequest(), new MockHttpServletResponse(), CoreAuthenticationTestUtils.getService()));
        }
    }

    @TestConfiguration(value = "SecondMultifactorTestConfiguration", proxyBeanMethods = false)
    static class SecondMultifactorTestConfiguration {
        @Bean
        public MultifactorAuthenticationProvider simpleProvider() {
            return new TestMultifactorAuthenticationProvider("mfa-simple").setOrder(1000);
        }
    }
}
