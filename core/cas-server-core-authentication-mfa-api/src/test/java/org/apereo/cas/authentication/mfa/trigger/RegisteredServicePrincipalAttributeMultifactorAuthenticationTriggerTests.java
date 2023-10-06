package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.DefaultChainingMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationProviderResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.Ordered;

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
class RegisteredServicePrincipalAttributeMultifactorAuthenticationTriggerTests extends BaseMultifactorAuthenticationTriggerTests {
    @Test
    void verifyOperationByCompositeProvider() throws Throwable {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();

        val provider1 = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(appCtx);
        val provider2 = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(appCtx,
            new TestMultifactorAuthenticationProvider("mfa-example"));

        val policy = mock(RegisteredServiceMultifactorPolicy.class);
        when(policy.getPrincipalAttributeNameTrigger()).thenReturn("email");
        when(policy.getPrincipalAttributeValueToMatch()).thenReturn(".+@example.*");
        when(policy.getMultifactorAuthenticationProviders()).thenReturn(Set.of(provider1.getId(), provider2.getId()));
        when(registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);

        val chain = new DefaultChainingMultifactorAuthenticationProvider(mock(MultifactorAuthenticationFailureModeEvaluator.class));
        chain.addMultifactorAuthenticationProviders(provider1, provider1);
        val selector = mock(MultifactorAuthenticationProviderSelector.class);
        when(selector.resolve(any(Collection.class), any(RegisteredService.class), any(Principal.class))).thenReturn(chain);

        val props = new CasConfigurationProperties();
        val trigger = new RegisteredServicePrincipalAttributeMultifactorAuthenticationTrigger(props,
            new DefaultMultifactorAuthenticationProviderResolver(MultifactorAuthenticationPrincipalResolver.identical()),
            appCtx, selector);
        val result = trigger.isActivated(authentication, registeredService, httpRequest, httpResponse, mock(Service.class));
        assertTrue(result.isPresent());
        assertEquals(result.get(), chain);
        assertNotNull(trigger.getApplicationContext());
        assertNotNull(trigger.getCasProperties());
        assertNotNull(trigger.getMultifactorAuthenticationProviderResolver());
        assertNotNull(trigger.getMultifactorAuthenticationProviderSelector());
        assertEquals(Ordered.LOWEST_PRECEDENCE, trigger.getOrder());
    }

    @Test
    void verifyOperationByProvider() throws Throwable {
        val policy = mock(RegisteredServiceMultifactorPolicy.class);
        when(policy.getPrincipalAttributeNameTrigger()).thenReturn("email");
        when(policy.getPrincipalAttributeValueToMatch()).thenReturn(".+@example.*");
        when(policy.getMultifactorAuthenticationProviders()).thenReturn(Set.of(TestMultifactorAuthenticationProvider.ID));

        val props = new CasConfigurationProperties();
        val trigger = new RegisteredServicePrincipalAttributeMultifactorAuthenticationTrigger(props,
            new DefaultMultifactorAuthenticationProviderResolver(MultifactorAuthenticationPrincipalResolver.identical()), applicationContext,
            mock(MultifactorAuthenticationProviderSelector.class));

        assertTrue(trigger.isActivated(authentication, null,
            httpRequest, httpResponse, mock(Service.class)).isEmpty());

        when(registeredService.getMultifactorAuthenticationPolicy()).thenReturn(null);
        assertTrue(trigger.isActivated(authentication, registeredService,
            httpRequest, httpResponse, mock(Service.class)).isEmpty());


        when(this.registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);
        val result = trigger.isActivated(authentication, registeredService,
            this.httpRequest, this.httpResponse, mock(Service.class));
        assertTrue(result.isPresent());
    }

    @Test
    void verifyOperationByMultipleProviders() throws Throwable {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();

        val provider1 = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(appCtx);
        val provider2 = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(appCtx,
            new TestMultifactorAuthenticationProvider("mfa-example"));

        val policy = mock(RegisteredServiceMultifactorPolicy.class);
        when(policy.getPrincipalAttributeNameTrigger()).thenReturn("email");
        when(policy.getPrincipalAttributeValueToMatch()).thenReturn(".+@example.*");
        when(policy.getMultifactorAuthenticationProviders()).thenReturn(Set.of(provider1.getId(), provider2.getId()));
        when(registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);

        val props = new CasConfigurationProperties();
        val selector = mock(MultifactorAuthenticationProviderSelector.class);
        when(selector.resolve(any(Collection.class), any(), any())).thenReturn(provider2);
        val trigger = new RegisteredServicePrincipalAttributeMultifactorAuthenticationTrigger(props,
            new DefaultMultifactorAuthenticationProviderResolver(MultifactorAuthenticationPrincipalResolver.identical()), appCtx, selector);
        val result = trigger.isActivated(authentication, registeredService,
            httpRequest, this.httpResponse, mock(Service.class));
        assertTrue(result.isPresent());
        assertEquals(provider2.getId(), result.get().getId());
    }

    @Test
    void verifyMismatchAttributesMustDeny() throws Throwable {
        val policy = mock(RegisteredServiceMultifactorPolicy.class);
        when(policy.getPrincipalAttributeNameTrigger()).thenReturn("bad-attribute");
        when(policy.getPrincipalAttributeValueToMatch()).thenReturn(".+@example.*");
        when(policy.getMultifactorAuthenticationProviders()).thenReturn(Set.of(TestMultifactorAuthenticationProvider.ID));
        when(this.registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);

        val props = new CasConfigurationProperties();
        props.getAuthn().getMfa().getTriggers().getPrincipal().setDenyIfUnmatched(true);
        val trigger = new RegisteredServicePrincipalAttributeMultifactorAuthenticationTrigger(props,
            new DefaultMultifactorAuthenticationProviderResolver(MultifactorAuthenticationPrincipalResolver.identical()), applicationContext,
            mock(MultifactorAuthenticationProviderSelector.class));
        assertThrows(AuthenticationException.class, () -> trigger.isActivated(authentication, registeredService,
            this.httpRequest, this.httpResponse, mock(Service.class)));
    }

    @Test
    void verifyMismatchAttributes() throws Throwable {
        val policy = mock(RegisteredServiceMultifactorPolicy.class);
        when(policy.getPrincipalAttributeNameTrigger()).thenReturn("bad-attribute");
        when(policy.getPrincipalAttributeValueToMatch()).thenReturn(".+@example.*");
        when(policy.getMultifactorAuthenticationProviders()).thenReturn(Set.of(TestMultifactorAuthenticationProvider.ID));
        when(this.registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);

        val props = new CasConfigurationProperties();
        val trigger = new RegisteredServicePrincipalAttributeMultifactorAuthenticationTrigger(props,
            new DefaultMultifactorAuthenticationProviderResolver(MultifactorAuthenticationPrincipalResolver.identical()), applicationContext,
            mock(MultifactorAuthenticationProviderSelector.class));
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class));
        assertFalse(result.isPresent());
    }

    @Test
    void verifyPolicyNoAttributes() throws Throwable {
        val policy = mock(RegisteredServiceMultifactorPolicy.class);
        when(policy.getPrincipalAttributeNameTrigger()).thenReturn("email");
        when(policy.getPrincipalAttributeValueToMatch()).thenReturn(StringUtils.EMPTY);
        when(policy.getMultifactorAuthenticationProviders()).thenReturn(Set.of(TestMultifactorAuthenticationProvider.ID));
        when(this.registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);

        val props = new CasConfigurationProperties();
        val trigger = new RegisteredServicePrincipalAttributeMultifactorAuthenticationTrigger(props,
            new DefaultMultifactorAuthenticationProviderResolver(MultifactorAuthenticationPrincipalResolver.identical()), applicationContext,
            mock(MultifactorAuthenticationProviderSelector.class));
        val result = trigger.isActivated(authentication, registeredService, this.httpRequest, this.httpResponse, mock(Service.class));
        assertTrue(result.isEmpty());
    }
}
