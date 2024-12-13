package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationProviderResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.authentication.mfa.MultifactorAuthenticationTestUtils;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.ClassPathResource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GroovyScriptMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("GroovyAuthentication")
@SetSystemProperty(key = ExecutableCompiledScriptFactory.SYSTEM_PROPERTY_GROOVY_COMPILE_STATIC, value = "true")
class GroovyScriptMultifactorAuthenticationTriggerTests extends BaseMultifactorAuthenticationTriggerTests {
    @Test
    void verifyOperationByProvider() throws Throwable {
        val trigger = buildGroovyTrigger(applicationContext);
        var result = trigger.isActivated(authentication, registeredService,
            this.httpRequest, this.httpResponse, mock(Service.class));
        assertTrue(result.isPresent());

        val service = MultifactorAuthenticationTestUtils.getService("nomfa");
        result = trigger.isActivated(authentication, registeredService,
            this.httpRequest, this.httpResponse, service);
        assertFalse(result.isPresent());
    }

    @Test
    void verifyCompositeProvider() throws Throwable {
        val trigger = buildGroovyTrigger(applicationContext);
        val service = MultifactorAuthenticationTestUtils.getService("composite");
        val result = trigger.isActivated(authentication, registeredService,
            this.httpRequest, this.httpResponse, service);
        assertTrue(result.isPresent());
    }

    @Test
    void verifyBadInputParameters() throws Throwable {
        val trigger = buildGroovyTrigger(applicationContext);
        var result = trigger.isActivated(null, registeredService,
            this.httpRequest, this.httpResponse, mock(Service.class));
        assertFalse(result.isPresent());

        result = trigger.isActivated(authentication, null,
            this.httpRequest, this.httpResponse, mock(Service.class));
        assertTrue(result.isPresent());

        result = trigger.isActivated(authentication, registeredService,
            this.httpRequest, this.httpResponse, null);
        assertTrue(result.isPresent());
        trigger.destroy();
    }

    @Test
    void verifyNoProvider() throws Throwable {
        val appContext = new StaticApplicationContext();
        appContext.refresh();

        val trigger = buildGroovyTrigger(appContext);
        assertThrows(AuthenticationException.class,
            () -> trigger.isActivated(authentication, registeredService,
                this.httpRequest, this.httpResponse, mock(Service.class)));
        assertDoesNotThrow(trigger::destroy);
        assertNotNull(trigger.getApplicationContext());
        assertNotNull(trigger.getMultifactorAuthenticationProviderResolver());
        assertNotNull(trigger.getMultifactorAuthenticationProviderSelector());
        assertNotNull(trigger.getWatchableScript());
    }

    private static GroovyScriptMultifactorAuthenticationTrigger buildGroovyTrigger(final ApplicationContext appContext) throws Throwable {
        val selector = mock(MultifactorAuthenticationProviderSelector.class);
        when(selector.resolve(any(), any(), any())).thenReturn(new TestMultifactorAuthenticationProvider());
        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        val resource = new ClassPathResource("GroovyMfaTrigger.groovy");
        return new GroovyScriptMultifactorAuthenticationTrigger(
            scriptFactory.fromResource(resource),
            appContext,
            new DefaultMultifactorAuthenticationProviderResolver(MultifactorAuthenticationPrincipalResolver.identical()),
            selector);
    }

}
