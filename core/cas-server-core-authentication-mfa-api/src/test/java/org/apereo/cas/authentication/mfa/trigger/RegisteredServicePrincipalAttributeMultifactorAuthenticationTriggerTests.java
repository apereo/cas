package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationProviderResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;

import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RegisteredServicePrincipalAttributeMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("MFATrigger")
public class RegisteredServicePrincipalAttributeMultifactorAuthenticationTriggerTests extends BaseMultifactorAuthenticationTriggerTests {
    @Test
    public void verifyOperationByProvider() {
        val policy = mock(RegisteredServiceMultifactorPolicy.class);
        when(policy.getPrincipalAttributeNameTrigger()).thenReturn("email");
        when(policy.getPrincipalAttributeValueToMatch()).thenReturn(".+@example.*");
        when(policy.getMultifactorAuthenticationProviders()).thenReturn(Set.of(TestMultifactorAuthenticationProvider.ID));
        when(this.registeredService.getMultifactorPolicy()).thenReturn(policy);

        val props = new CasConfigurationProperties();
        val trigger = new RegisteredServicePrincipalAttributeMultifactorAuthenticationTrigger(props,
            new DefaultMultifactorAuthenticationProviderResolver(MultifactorAuthenticationPrincipalResolver.identical()), applicationContext,
            mock(MultifactorAuthenticationProviderSelector.class));
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertTrue(result.isPresent());
    }

    @Test
    public void verifyOperationByMultipleProviders() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();

        val provider1 = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(appCtx);
        val provider2 = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(appCtx,
            new TestMultifactorAuthenticationProvider("mfa-example"));

        val policy = mock(RegisteredServiceMultifactorPolicy.class);
        when(policy.getPrincipalAttributeNameTrigger()).thenReturn("email");
        when(policy.getPrincipalAttributeValueToMatch()).thenReturn(".+@example.*");
        when(policy.getMultifactorAuthenticationProviders()).thenReturn(Set.of(provider1.getId(), provider2.getId()));
        when(registeredService.getMultifactorPolicy()).thenReturn(policy);

        val props = new CasConfigurationProperties();
        val selector = mock(MultifactorAuthenticationProviderSelector.class);
        when(selector.resolve(any(Collection.class), any(), any())).thenReturn(provider2);
        val trigger = new RegisteredServicePrincipalAttributeMultifactorAuthenticationTrigger(props,
            new DefaultMultifactorAuthenticationProviderResolver(MultifactorAuthenticationPrincipalResolver.identical()), appCtx, selector);
        val result = trigger.isActivated(authentication, registeredService, httpRequest, mock(Service.class));
        assertTrue(result.isPresent());
        assertEquals(provider2.getId(), result.get().getId());
    }

    @Test
    public void verifyMismatchAttributesMustDeny() {
        val policy = mock(RegisteredServiceMultifactorPolicy.class);
        when(policy.getPrincipalAttributeNameTrigger()).thenReturn("bad-attribute");
        when(policy.getPrincipalAttributeValueToMatch()).thenReturn(".+@example.*");
        when(policy.getMultifactorAuthenticationProviders()).thenReturn(Set.of(TestMultifactorAuthenticationProvider.ID));
        when(this.registeredService.getMultifactorPolicy()).thenReturn(policy);

        val props = new CasConfigurationProperties();
        props.getAuthn().getMfa().getTriggers().getPrincipal().setDenyIfUnmatched(true);
        val trigger = new RegisteredServicePrincipalAttributeMultifactorAuthenticationTrigger(props,
            new DefaultMultifactorAuthenticationProviderResolver(MultifactorAuthenticationPrincipalResolver.identical()), applicationContext,
            mock(MultifactorAuthenticationProviderSelector.class));
        assertThrows(AuthenticationException.class, () -> trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class)));
    }

    @Test
    public void verifyMismatchAttributes() {
        val policy = mock(RegisteredServiceMultifactorPolicy.class);
        when(policy.getPrincipalAttributeNameTrigger()).thenReturn("bad-attribute");
        when(policy.getPrincipalAttributeValueToMatch()).thenReturn(".+@example.*");
        when(policy.getMultifactorAuthenticationProviders()).thenReturn(Set.of(TestMultifactorAuthenticationProvider.ID));
        when(this.registeredService.getMultifactorPolicy()).thenReturn(policy);

        val props = new CasConfigurationProperties();
        val trigger = new RegisteredServicePrincipalAttributeMultifactorAuthenticationTrigger(props,
            new DefaultMultifactorAuthenticationProviderResolver(MultifactorAuthenticationPrincipalResolver.identical()), applicationContext,
            mock(MultifactorAuthenticationProviderSelector.class));
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertFalse(result.isPresent());
    }

    @Test
    public void verifyPolicyNoAttributes() {
        val policy = mock(RegisteredServiceMultifactorPolicy.class);
        when(policy.getPrincipalAttributeNameTrigger()).thenReturn("email");
        when(policy.getPrincipalAttributeValueToMatch()).thenReturn(StringUtils.EMPTY);
        when(policy.getMultifactorAuthenticationProviders()).thenReturn(Set.of(TestMultifactorAuthenticationProvider.ID));
        when(this.registeredService.getMultifactorPolicy()).thenReturn(policy);

        val props = new CasConfigurationProperties();
        val trigger = new RegisteredServicePrincipalAttributeMultifactorAuthenticationTrigger(props,
            new DefaultMultifactorAuthenticationProviderResolver(MultifactorAuthenticationPrincipalResolver.identical()), applicationContext,
            mock(MultifactorAuthenticationProviderSelector.class));
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, mock(Service.class));
        assertTrue(result.isEmpty());
    }
}
