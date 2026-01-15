package org.apereo.cas.authentication.mfa.trigger;

import module java.base;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ScriptedRegisteredServiceMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("GroovyAuthentication")
class ScriptedRegisteredServiceMultifactorAuthenticationTriggerTests {

    @Nested
    @Import(BaseMultifactorAuthenticationTriggerTests.TestMultifactorTestConfiguration.class)
    class DefaultTests extends BaseMultifactorAuthenticationTriggerTests {
        @Test
        void verifyOperationByProviderEmbeddedScript() throws Throwable {
            val policy = mock(RegisteredServiceMultifactorPolicy.class);
            when(policy.getScript()).thenReturn("groovy { return '" + TestMultifactorAuthenticationProvider.ID + "' }");
            val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
            when(registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);

            val result = scriptedRegisteredServiceMultifactorAuthenticationTrigger.isActivated(
                CoreAuthenticationTestUtils.getAuthentication(), registeredService,
                new MockHttpServletRequest(), new MockHttpServletResponse(), CoreAuthenticationTestUtils.getService());
            assertTrue(result.isPresent());
        }

        @Test
        void verifyUnknownProvider() {
            val policy = mock(RegisteredServiceMultifactorPolicy.class);
            when(policy.getScript()).thenReturn("groovy { return 'unknown' }");
            val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
            when(registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);
            assertThrows(AuthenticationException.class,
                () -> scriptedRegisteredServiceMultifactorAuthenticationTrigger.isActivated(
                    CoreAuthenticationTestUtils.getAuthentication(), registeredService,
                    new MockHttpServletRequest(), new MockHttpServletResponse(),
                    CoreAuthenticationTestUtils.getService()));
        }

        @Test
        void verifyNoResult() throws Throwable {
            val policy = mock(RegisteredServiceMultifactorPolicy.class);
            when(policy.getScript()).thenReturn("groovy { return null }");
            val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
            when(registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);
            val result = scriptedRegisteredServiceMultifactorAuthenticationTrigger.isActivated(
                CoreAuthenticationTestUtils.getAuthentication(), registeredService,
                new MockHttpServletRequest(), new MockHttpServletResponse(), CoreAuthenticationTestUtils.getService());
            assertTrue(result.isEmpty());
        }


        @Test
        void verifyOperationByProviderScript() throws Throwable {
            val policy = mock(RegisteredServiceMultifactorPolicy.class);
            when(policy.getScript()).thenReturn("classpath:ScriptedRegisteredServiceMultifactorAuthenticationTrigger.groovy");
            val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
            when(registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);
            val result = scriptedRegisteredServiceMultifactorAuthenticationTrigger.isActivated(
                CoreAuthenticationTestUtils.getAuthentication(), registeredService,
                new MockHttpServletRequest(), new MockHttpServletResponse(), CoreAuthenticationTestUtils.getService());
            assertTrue(result.isPresent());
        }

        @Test
        void verifyOperationByProviderScriptUnknown() {
            val policy = mock(RegisteredServiceMultifactorPolicy.class);
            when(policy.getScript()).thenReturn("classpath:Unknown.groovy");
            val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
            when(registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);
            assertThrows(FileNotFoundException.class,
                () -> scriptedRegisteredServiceMultifactorAuthenticationTrigger.isActivated(
                    CoreAuthenticationTestUtils.getAuthentication(), registeredService,
                    new MockHttpServletRequest(), new MockHttpServletResponse(),
                    CoreAuthenticationTestUtils.getService()));
        }

    }

    @Nested
    class NoProvidersTests extends BaseMultifactorAuthenticationTriggerTests {
        @Test
        void verifyEmptyProviders() {
            val policy = mock(RegisteredServiceMultifactorPolicy.class);
            when(policy.getScript()).thenReturn("groovy { return '" + TestMultifactorAuthenticationProvider.ID + "' }");
            val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
            when(registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);
            assertThrows(AuthenticationException.class,
                () -> scriptedRegisteredServiceMultifactorAuthenticationTrigger.isActivated(
                    CoreAuthenticationTestUtils.getAuthentication(), registeredService,
                    new MockHttpServletRequest(), new MockHttpServletResponse(),
                    CoreAuthenticationTestUtils.getService()));
        }
    }
}
