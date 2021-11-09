package org.apereo.cas.authentication.mfa;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.binding.expression.support.LiteralExpression;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;
import org.springframework.webflow.engine.support.DefaultTransitionCriteria;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link MultifactorAuthenticationUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */

@Tag("MFA")
public class MultifactorAuthenticationUtilsTests {

    @Test
    public void verifyMissingTransition() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setCurrentEvent(new Event(this, "currentState"));
        context.setCurrentTransition(new Transition());
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        assertThrows(AuthenticationException.class,
            () -> MultifactorAuthenticationUtils.validateEventIdForMatchingTransitionInContext("unknown", Optional.of(context), Map.of()));
    }

    @Test
    public void verifyMissingProviderEvent() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        assertNotNull(MultifactorAuthenticationUtils.evaluateEventForProviderInContext(
            MultifactorAuthenticationTestUtils.getPrincipal("casuser"),
            MultifactorAuthenticationTestUtils.getRegisteredService(),
            Optional.of(context),
            null));
    }

    @Test
    public void verifyAvailProviders() {
        val appCtx = mock(ApplicationContext.class);
        when(appCtx.getBeansOfType(any(), anyBoolean(), anyBoolean())).thenThrow(new RuntimeException());
        assertNotNull(MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(appCtx));
    }

    @Test
    public void verifyProviders() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        ApplicationContextProvider.holdApplicationContext(null);
        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        assertFalse(MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderFromApplicationContext(provider.getId()).isPresent());
    }

    @Test
    public void verifyResolveBySingleAttribute() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        ApplicationContextProvider.holdApplicationContext(applicationContext);
        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        assertTrue(MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderFromApplicationContext(provider.getId()).isPresent());

        val targetResolver = new DefaultTargetStateResolver(TestMultifactorAuthenticationProvider.ID);
        val transition = new Transition(new DefaultTransitionCriteria(
            new LiteralExpression(TestMultifactorAuthenticationProvider.ID)), targetResolver);
        context.getRootFlow().getGlobalTransitionSet().add(transition);

        val result = MultifactorAuthenticationUtils.resolveEventViaSingleAttribute(MultifactorAuthenticationTestUtils.getPrincipal("casuser"),
            List.of("mfa-value1"), MultifactorAuthenticationTestUtils.getRegisteredService(),
            Optional.of(context), provider, (s, mfaProvider) -> RegexUtils.find("mismatch-.+", s));
        assertNull(result);
    }

    @Test
    public void verifyResolveByAttribute() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        ApplicationContextProvider.holdApplicationContext(applicationContext);
        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        assertTrue(MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderFromApplicationContext(provider.getId()).isPresent());

        val targetResolver = new DefaultTargetStateResolver(TestMultifactorAuthenticationProvider.ID);
        val transition = new Transition(new DefaultTransitionCriteria(
            new LiteralExpression(TestMultifactorAuthenticationProvider.ID)), targetResolver);
        context.getRootFlow().getGlobalTransitionSet().add(transition);

        val result = MultifactorAuthenticationUtils.resolveEventViaMultivaluedAttribute(MultifactorAuthenticationTestUtils.getPrincipal("casuser"),
            List.of("mfa-value"), MultifactorAuthenticationTestUtils.getRegisteredService(),
            Optional.of(context), provider, (s, mfaProvider) -> RegexUtils.find("mfa-.+", s));
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void verifyMultivaluedAttrs() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        ApplicationContextProvider.holdApplicationContext(applicationContext);
        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        var result = MultifactorAuthenticationUtils.resolveEventViaMultivaluedAttribute(MultifactorAuthenticationTestUtils.getPrincipal("casuser"),
            List.of("some-value"), MultifactorAuthenticationTestUtils.getRegisteredService(), Optional.of(context), provider,
            (s, mfaProvider) -> {
                throw new RuntimeException("Bad Predicate");
            });
        assertNotNull(result);
        assertTrue(result.isEmpty());

        result = MultifactorAuthenticationUtils.resolveEventViaMultivaluedAttribute(MultifactorAuthenticationTestUtils.getPrincipal("casuser"),
            "some-value", MultifactorAuthenticationTestUtils.getRegisteredService(), Optional.of(context), provider,
            (s, mfaProvider) -> {
                throw new RuntimeException("Bad Predicate");
            });
        assertNull(result);
    }

    @Test
    public void verifyProviderByService() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        ApplicationContextProvider.holdApplicationContext(applicationContext);
        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        assertTrue(MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderFromApplicationContext(provider.getId()).isPresent());

        val registeredService = MultifactorAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getMultifactorPolicy().getMultifactorAuthenticationProviders()).thenReturn(Set.of("mfa-other"));
        var result = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderForService(registeredService, applicationContext);
        assertNotNull(result);
        assertTrue(result.isEmpty());

        when(registeredService.getMultifactorPolicy().getMultifactorAuthenticationProviders()).thenReturn(Set.of(provider.getId()));
        result = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderForService(registeredService, applicationContext);
        assertNotNull(result);
        assertFalse(result.isEmpty());

        when(registeredService.getMultifactorPolicy()).thenReturn(null);
        assertNull(MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderForService(registeredService, applicationContext));
    }
}
