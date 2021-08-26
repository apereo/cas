package org.apereo.cas.authentication.bypass;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */

@Tag("MFATrigger")
public class RegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluatorTests {

    @Test
    public void verifyNoMatchOperation() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        ApplicationContextProvider.holdApplicationContext(applicationContext);
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext,
            MultifactorAuthenticationPrincipalResolver.identical(), UUID.randomUUID().toString());

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);

        val eval = new DefaultChainingMultifactorAuthenticationBypassProvider();
        eval.addMultifactorAuthenticationProviderBypassEvaluator(
            new RegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(TestMultifactorAuthenticationProvider.ID));

        val principal = CoreAuthenticationTestUtils.getPrincipal(Map.of("cn", List.of("nothing")));
        val authentication = CoreAuthenticationTestUtils.getAuthentication(principal);
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        val policy = new DefaultRegisteredServiceMultifactorPolicy();
        policy.setBypassEnabled(true);
        policy.setBypassPrincipalAttributeName("cn");
        policy.setBypassPrincipalAttributeValue("^e[x]am.*");
        when(registeredService.getMultifactorPolicy()).thenReturn(policy);
        assertTrue(eval.shouldMultifactorAuthenticationProviderExecute(authentication, registeredService,
            provider, new MockHttpServletRequest()));
    }

    @Test
    public void verifyOperation() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        ApplicationContextProvider.holdApplicationContext(applicationContext);
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext,
            MultifactorAuthenticationPrincipalResolver.identical(), UUID.randomUUID().toString());

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);

        val eval = new DefaultChainingMultifactorAuthenticationBypassProvider();
        eval.addMultifactorAuthenticationProviderBypassEvaluator(
            new RegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(TestMultifactorAuthenticationProvider.ID));

        val principal = CoreAuthenticationTestUtils.getPrincipal(Map.of("cn", List.of("example")));
        val authentication = CoreAuthenticationTestUtils.getAuthentication(principal);
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        val policy = new DefaultRegisteredServiceMultifactorPolicy();
        policy.setBypassEnabled(true);
        policy.setBypassPrincipalAttributeName("cn");
        policy.setBypassPrincipalAttributeValue("^e[x]am.*");
        when(registeredService.getMultifactorPolicy()).thenReturn(policy);
        assertFalse(eval.shouldMultifactorAuthenticationProviderExecute(authentication, registeredService, provider, new MockHttpServletRequest()));
    }

    @Test
    public void verifyNoService() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        ApplicationContextProvider.holdApplicationContext(applicationContext);
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext,
            MultifactorAuthenticationPrincipalResolver.identical(), UUID.randomUUID().toString());
        
        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);

        val eval = new DefaultChainingMultifactorAuthenticationBypassProvider();
        eval.addMultifactorAuthenticationProviderBypassEvaluator(
            new RegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(TestMultifactorAuthenticationProvider.ID));

        val principal = CoreAuthenticationTestUtils.getPrincipal(Map.of("cn", List.of("example")));
        val authentication = CoreAuthenticationTestUtils.getAuthentication(principal);
        assertTrue(eval.shouldMultifactorAuthenticationProviderExecute(authentication, null, provider, new MockHttpServletRequest()));
    }
}
