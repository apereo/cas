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
class RegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluatorTests {

    @Test
    void verifyMissingPrincipalAttribute() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        ApplicationContextProvider.holdApplicationContext(applicationContext);
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext,
            MultifactorAuthenticationPrincipalResolver.identical(), UUID.randomUUID().toString());

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);

        val eval = new DefaultChainingMultifactorAuthenticationBypassProvider(applicationContext);
        eval.addMultifactorAuthenticationProviderBypassEvaluator(
            new RegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(TestMultifactorAuthenticationProvider.ID, applicationContext));

        val principal = CoreAuthenticationTestUtils.getPrincipal(Map.of("givenName", List.of("nothing")));
        val authentication = CoreAuthenticationTestUtils.getAuthentication(principal);
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        val policy = new DefaultRegisteredServiceMultifactorPolicy();
        policy.setBypassIfMissingPrincipalAttribute(true);
        policy.setBypassPrincipalAttributeName("cn");
        policy.setBypassPrincipalAttributeValue("^e[x]am.*");
        when(registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);
        assertFalse(eval.shouldMultifactorAuthenticationProviderExecute(authentication, registeredService, 
            provider, new MockHttpServletRequest(), CoreAuthenticationTestUtils.getService()));
    }

    @Test
    void verifyNoMatchOperation() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        ApplicationContextProvider.holdApplicationContext(applicationContext);
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext,
            MultifactorAuthenticationPrincipalResolver.identical(), UUID.randomUUID().toString());

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);

        val eval = new DefaultChainingMultifactorAuthenticationBypassProvider(applicationContext);
        eval.addMultifactorAuthenticationProviderBypassEvaluator(
            new RegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(
                TestMultifactorAuthenticationProvider.ID, applicationContext));

        val principal = CoreAuthenticationTestUtils.getPrincipal(Map.of("cn", List.of("nothing")));
        val authentication = CoreAuthenticationTestUtils.getAuthentication(principal);
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        val policy = new DefaultRegisteredServiceMultifactorPolicy();
        policy.setBypassEnabled(false);
        policy.setBypassPrincipalAttributeName("cn");
        policy.setBypassPrincipalAttributeValue("^e[x]am.*");
        when(registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);
        assertTrue(eval.shouldMultifactorAuthenticationProviderExecute(authentication, registeredService,
            provider, new MockHttpServletRequest(), CoreAuthenticationTestUtils.getService()));
    }

    @Test
    void verifyOperation() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        ApplicationContextProvider.holdApplicationContext(applicationContext);
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext,
            MultifactorAuthenticationPrincipalResolver.identical(), UUID.randomUUID().toString());

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);

        val eval = new DefaultChainingMultifactorAuthenticationBypassProvider(applicationContext);
        eval.addMultifactorAuthenticationProviderBypassEvaluator(
            new RegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(TestMultifactorAuthenticationProvider.ID, applicationContext));

        val principal = CoreAuthenticationTestUtils.getPrincipal(Map.of("cn", List.of("example")));
        val authentication = CoreAuthenticationTestUtils.getAuthentication(principal);
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        val policy = new DefaultRegisteredServiceMultifactorPolicy();
        policy.setBypassEnabled(false);
        policy.setBypassPrincipalAttributeName("cn");
        policy.setBypassPrincipalAttributeValue("^e[x]am.*");
        when(registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);
        assertFalse(eval.shouldMultifactorAuthenticationProviderExecute(authentication, registeredService,
            provider, new MockHttpServletRequest(), CoreAuthenticationTestUtils.getService()));
    }

    @Test
    void verifyNoService() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        ApplicationContextProvider.holdApplicationContext(applicationContext);
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext,
            MultifactorAuthenticationPrincipalResolver.identical(), UUID.randomUUID().toString());

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);

        val eval = new DefaultChainingMultifactorAuthenticationBypassProvider(applicationContext);
        eval.addMultifactorAuthenticationProviderBypassEvaluator(
            new RegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(TestMultifactorAuthenticationProvider.ID, applicationContext));

        val principal = CoreAuthenticationTestUtils.getPrincipal(Map.of("cn", List.of("example")));
        val authentication = CoreAuthenticationTestUtils.getAuthentication(principal);
        assertTrue(eval.shouldMultifactorAuthenticationProviderExecute(authentication, null,
            provider, new MockHttpServletRequest(), CoreAuthenticationTestUtils.getService()));
    }
}
