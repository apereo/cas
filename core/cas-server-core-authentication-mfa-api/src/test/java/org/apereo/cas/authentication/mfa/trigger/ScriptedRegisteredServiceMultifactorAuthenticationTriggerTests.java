package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ScriptedRegisteredServiceMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("GroovyAuthentication")
class ScriptedRegisteredServiceMultifactorAuthenticationTriggerTests extends BaseMultifactorAuthenticationTriggerTests {
    @Test
    void verifyOperationByProviderEmbeddedScript() throws Throwable {
        val policy = mock(RegisteredServiceMultifactorPolicy.class);
        when(policy.getScript()).thenReturn("groovy { return '" + multifactorAuthenticationProvider.getId() + "' }");
        when(this.registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);

        val props = new CasConfigurationProperties();
        val trigger = new ScriptedRegisteredServiceMultifactorAuthenticationTrigger(props, applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class));
        assertTrue(result.isPresent());
    }

    @Test
    void verifyUnknownProvider() throws Throwable {
        val policy = mock(RegisteredServiceMultifactorPolicy.class);
        when(policy.getScript()).thenReturn("groovy { return 'unknown' }");
        when(this.registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);

        val props = new CasConfigurationProperties();
        val trigger = new ScriptedRegisteredServiceMultifactorAuthenticationTrigger(props, applicationContext);
        assertThrows(AuthenticationException.class,
            () -> trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class)));
    }

    @Test
    void verifyNoResult() throws Throwable {
        val policy = mock(RegisteredServiceMultifactorPolicy.class);
        when(policy.getScript()).thenReturn("groovy { return null }");
        when(this.registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);

        val props = new CasConfigurationProperties();
        val trigger = new ScriptedRegisteredServiceMultifactorAuthenticationTrigger(props, applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class));
        assertFalse(result.isPresent());
    }

    @Test
    void verifyEmptyProviders() throws Throwable {
        val ctx = new StaticApplicationContext();
        ctx.refresh();

        val policy = mock(RegisteredServiceMultifactorPolicy.class);
        when(policy.getScript()).thenReturn("groovy { return '" + multifactorAuthenticationProvider.getId() + "' }");
        when(this.registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);

        val props = new CasConfigurationProperties();
        val trigger = new ScriptedRegisteredServiceMultifactorAuthenticationTrigger(props, ctx);
        assertThrows(AuthenticationException.class,
            () -> trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class)));
    }

    @Test
    void verifyOperationByProviderScript() throws Throwable {
        val policy = mock(RegisteredServiceMultifactorPolicy.class);
        when(policy.getScript()).thenReturn("classpath:ScriptedRegisteredServiceMultifactorAuthenticationTrigger.groovy");
        when(this.registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);

        val props = new CasConfigurationProperties();
        val trigger = new ScriptedRegisteredServiceMultifactorAuthenticationTrigger(props, applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class));
        assertTrue(result.isPresent());
    }

    @Test
    void verifyOperationByProviderScriptUnknown() throws Throwable {
        val policy = mock(RegisteredServiceMultifactorPolicy.class);
        when(policy.getScript()).thenReturn("classpath:Unknown.groovy");
        when(this.registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);

        val props = new CasConfigurationProperties();
        val trigger = new ScriptedRegisteredServiceMultifactorAuthenticationTrigger(props, applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class));
        assertFalse(result.isPresent());
    }
}
