package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationProviderResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.authentication.mfa.MultifactorAuthenticationTestUtils;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;

import lombok.val;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GroovyScriptMultifactorAuthenticationTriggerTests extends BaseMultifactorAuthenticationTriggerTests {
    @Test
    @Order(1)
    public void verifyOperationByProvider() {
        val trigger = buildGroovyTrigger();
        var result = trigger.isActivated(authentication, registeredService,
            this.httpRequest, this.httpResponse, mock(Service.class));
        assertTrue(result.isPresent());

        val service = MultifactorAuthenticationTestUtils.getService("nomfa");
        result = trigger.isActivated(authentication, registeredService,
            this.httpRequest, this.httpResponse, service);
        assertFalse(result.isPresent());
    }

    @Test
    @Order(2)
    public void verifyCompositeProvider() {
        val trigger = buildGroovyTrigger();
        val service = MultifactorAuthenticationTestUtils.getService("composite");
        val result = trigger.isActivated(authentication, registeredService,
            this.httpRequest, this.httpResponse, service);
        assertTrue(result.isPresent());
    }

    @Test
    @Order(3)
    public void verifyBadInputParameters() {
        val trigger = buildGroovyTrigger();
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
    @Order(0)
    @Tag("DisableProviderRegistration")
    public void verifyNoProvider() {
        val trigger = buildGroovyTrigger();
        assertThrows(AuthenticationException.class,
            () -> trigger.isActivated(authentication, registeredService,
                this.httpRequest, this.httpResponse, mock(Service.class)));
        assertDoesNotThrow(trigger::destroy);
        assertNotNull(trigger.getApplicationContext());
        assertNotNull(trigger.getMultifactorAuthenticationProviderResolver());
        assertNotNull(trigger.getMultifactorAuthenticationProviderSelector());
        assertNotNull(trigger.getWatchableScript());
    }

    private GroovyScriptMultifactorAuthenticationTrigger buildGroovyTrigger() {
        val selector = mock(MultifactorAuthenticationProviderSelector.class);
        when(selector.resolve(any(), any(), any())).thenReturn(new TestMultifactorAuthenticationProvider());
        return new GroovyScriptMultifactorAuthenticationTrigger(
            new WatchableGroovyScriptResource(new ClassPathResource("GroovyMfaTrigger.groovy")),
            applicationContext,
            new DefaultMultifactorAuthenticationProviderResolver(MultifactorAuthenticationPrincipalResolver.identical()),
            selector);
    }

}
