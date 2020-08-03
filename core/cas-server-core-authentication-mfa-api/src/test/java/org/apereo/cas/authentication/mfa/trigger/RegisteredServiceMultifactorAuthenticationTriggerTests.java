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
@Tag("MFA")
public class RegisteredServiceMultifactorAuthenticationTriggerTests extends BaseMultifactorAuthenticationTriggerTests {
    @Test
    public void verifyOperationByNoPolicy() {
        val props = new CasConfigurationProperties();
        val trigger = new RegisteredServiceMultifactorAuthenticationTrigger(props,
            (providers, service, principal) -> providers.iterator().next());
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertFalse(result.isPresent());
    }

    @Test
    public void verifyBadInput() {
        val props = new CasConfigurationProperties();
        val trigger = new RegisteredServiceMultifactorAuthenticationTrigger(props,
            (providers, service, principal) -> providers.iterator().next());
        assertNotNull(trigger.getCasProperties());
        assertNotNull(trigger.getMultifactorAuthenticationProviderSelector());
        val result = trigger.isActivated(null, null, this.httpRequest, mock(Service.class));
        assertFalse(result.isPresent());
    }
    
    @Test
    public void verifyOperationByPolicyForPrincipal() {
        val policy = mock(RegisteredServiceMultifactorPolicy.class);
        when(policy.getMultifactorAuthenticationProviders()).thenReturn(Set.of("mfa-dummy"));
        when(policy.getPrincipalAttributeNameTrigger()).thenReturn("email");
        when(policy.getPrincipalAttributeValueToMatch()).thenReturn("@example.org");
        when(this.registeredService.getMultifactorPolicy()).thenReturn(policy);
        val props = new CasConfigurationProperties();
        val trigger = new RegisteredServiceMultifactorAuthenticationTrigger(props,
            (providers, service, principal) -> providers.iterator().next());
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertFalse(result.isPresent());
    }

    @Test
    public void verifyOperationByProvider() {
        val policy = mock(RegisteredServiceMultifactorPolicy.class);
        when(policy.getMultifactorAuthenticationProviders()).thenReturn(Set.of(TestMultifactorAuthenticationProvider.ID));
        when(this.registeredService.getMultifactorPolicy()).thenReturn(policy);

        val props = new CasConfigurationProperties();
        val trigger = new RegisteredServiceMultifactorAuthenticationTrigger(props,
            (providers, service, principal) -> providers.iterator().next());
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertTrue(result.isPresent());
    }
}
