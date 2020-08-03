package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ScriptedRegisteredServiceMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 * @deprecated Since 6.2
 */
@Tag("Groovy")
@Deprecated(since = "6.2.0")
public class ScriptedRegisteredServiceMultifactorAuthenticationTriggerTests extends BaseMultifactorAuthenticationTriggerTests {
    @Test
    public void verifyOperationByProviderEmbeddedScript() {
        val policy = mock(RegisteredServiceMultifactorPolicy.class);
        when(policy.getScript()).thenReturn("groovy { return '" + multifactorAuthenticationProvider.getId() + "' }");
        when(this.registeredService.getMultifactorPolicy()).thenReturn(policy);

        val props = new CasConfigurationProperties();
        val trigger = new ScriptedRegisteredServiceMultifactorAuthenticationTrigger(props, applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertTrue(result.isPresent());
    }

    @Test
    public void verifyOperationByProviderScript() {
        val policy = mock(RegisteredServiceMultifactorPolicy.class);
        when(policy.getScript()).thenReturn("classpath:ScriptedRegisteredServiceMultifactorAuthenticationTrigger.groovy");
        when(this.registeredService.getMultifactorPolicy()).thenReturn(policy);

        val props = new CasConfigurationProperties();
        val trigger = new ScriptedRegisteredServiceMultifactorAuthenticationTrigger(props, applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertTrue(result.isPresent());
    }
}
