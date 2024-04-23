package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RegisteredServiceMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("MFATrigger")
class RegisteredServiceMultifactorAuthenticationTriggerTests extends BaseMultifactorAuthenticationTriggerTests {
    @Test
    void verifyOperationByNoPolicy() throws Throwable {
        val props = new CasConfigurationProperties();
        val trigger = new RegisteredServiceMultifactorAuthenticationTrigger(props,
            (providers, service, principal) -> providers.iterator().next(), applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class));
        assertFalse(result.isPresent());
    }

    @Test
    void verifyBadInput() throws Throwable {
        val props = new CasConfigurationProperties();
        val trigger = new RegisteredServiceMultifactorAuthenticationTrigger(props,
            (providers, service, principal) -> providers.iterator().next(), applicationContext);
        assertNotNull(trigger.getCasProperties());
        assertNotNull(trigger.getMultifactorAuthenticationProviderSelector());
        val result = trigger.isActivated(null, null,
            this.httpRequest, this.httpResponse, mock(Service.class));
        assertFalse(result.isPresent());
    }

    @Test
    void verifyOperationByPolicyForPrincipal() throws Throwable {
        val policy = mock(RegisteredServiceMultifactorPolicy.class);
        when(policy.getMultifactorAuthenticationProviders()).thenReturn(Set.of("mfa-dummy"));
        when(policy.getPrincipalAttributeNameTrigger()).thenReturn("email");
        when(policy.getPrincipalAttributeValueToMatch()).thenReturn("@example.org");
        when(this.registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);
        val props = new CasConfigurationProperties();
        val trigger = new RegisteredServiceMultifactorAuthenticationTrigger(props,
            (providers, service, principal) -> providers.iterator().next(), applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class));
        assertFalse(result.isPresent());
    }

    @Test
    void verifyOperationByProvider() throws Throwable {
        val policy = mock(RegisteredServiceMultifactorPolicy.class);
        when(policy.getMultifactorAuthenticationProviders()).thenReturn(Set.of(TestMultifactorAuthenticationProvider.ID));
        when(this.registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);

        val props = new CasConfigurationProperties();
        val trigger = new RegisteredServiceMultifactorAuthenticationTrigger(props,
            (providers, service, principal) -> providers.iterator().next(), applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class));
        assertTrue(result.isPresent());
    }

    @Test
    void verifyOperationByNoKnownProvider() throws Throwable {
        val policy = mock(RegisteredServiceMultifactorPolicy.class);
        when(policy.getMultifactorAuthenticationProviders()).thenReturn(Set.of("unknown"));
        when(this.registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);

        val props = new CasConfigurationProperties();
        val trigger = new RegisteredServiceMultifactorAuthenticationTrigger(props,
            (providers, service, principal) -> providers.iterator().next(), applicationContext);
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class));
        assertTrue(result.isEmpty());
    }
}
