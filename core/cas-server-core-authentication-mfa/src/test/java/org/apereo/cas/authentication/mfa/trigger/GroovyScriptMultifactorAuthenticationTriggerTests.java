package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.mfa.MultifactorAuthenticationTestUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyScriptMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("GroovyAuthentication")
@SetSystemProperty(key = ExecutableCompiledScriptFactory.SYSTEM_PROPERTY_GROOVY_COMPILE_STATIC, value = "true")
class GroovyScriptMultifactorAuthenticationTriggerTests {

    @Nested
    @Import(BaseMultifactorAuthenticationTriggerTests.TestMultifactorTestConfiguration.class)
    class DefaultTests extends BaseMultifactorAuthenticationTriggerTests {

        @Test
        void verifyOperationByProvider() throws Throwable {
            var result = groovyScriptMultifactorAuthenticationTrigger.isActivated(
                CoreAuthenticationTestUtils.getAuthentication(), CoreAuthenticationTestUtils.getRegisteredService(),
                new MockHttpServletRequest(), new MockHttpServletResponse(), CoreAuthenticationTestUtils.getService());
            assertTrue(result.isPresent());

            val service = MultifactorAuthenticationTestUtils.getService("nomfa");
            result = groovyScriptMultifactorAuthenticationTrigger.isActivated(
                CoreAuthenticationTestUtils.getAuthentication(), CoreAuthenticationTestUtils.getRegisteredService(),
                new MockHttpServletRequest(), new MockHttpServletResponse(), service);
            assertFalse(result.isPresent());
        }

        @Test
        void verifyCompositeProvider() throws Throwable {
            val service = MultifactorAuthenticationTestUtils.getService("composite");
            val result = groovyScriptMultifactorAuthenticationTrigger.isActivated(
                CoreAuthenticationTestUtils.getAuthentication(), CoreAuthenticationTestUtils.getRegisteredService(),
                new MockHttpServletRequest(), new MockHttpServletResponse(), service);
            assertTrue(result.isPresent());
        }

        @Test
        void verifyBadInputParameters() throws Throwable {
            var result = groovyScriptMultifactorAuthenticationTrigger.isActivated(
                null, CoreAuthenticationTestUtils.getRegisteredService(),
                new MockHttpServletRequest(), new MockHttpServletResponse(), CoreAuthenticationTestUtils.getService());
            assertFalse(result.isPresent());

            result = groovyScriptMultifactorAuthenticationTrigger.isActivated(
                CoreAuthenticationTestUtils.getAuthentication(), null,
                new MockHttpServletRequest(), new MockHttpServletResponse(), CoreAuthenticationTestUtils.getService());
            assertTrue(result.isPresent());

            result = groovyScriptMultifactorAuthenticationTrigger.isActivated(
                CoreAuthenticationTestUtils.getAuthentication(), CoreAuthenticationTestUtils.getRegisteredService(),
                new MockHttpServletRequest(), new MockHttpServletResponse(), null);
            assertTrue(result.isPresent());
        }
    }

    @Nested
    class NoProvidersTests extends BaseMultifactorAuthenticationTriggerTests {
        @Test
        void verifyNoProvider() throws Throwable {
            assertThrows(AuthenticationException.class,
                () -> groovyScriptMultifactorAuthenticationTrigger.isActivated(
                    CoreAuthenticationTestUtils.getAuthentication(), CoreAuthenticationTestUtils.getRegisteredService(),
                    new MockHttpServletRequest(), new MockHttpServletResponse(), CoreAuthenticationTestUtils.getService()));
        }
    }
}
