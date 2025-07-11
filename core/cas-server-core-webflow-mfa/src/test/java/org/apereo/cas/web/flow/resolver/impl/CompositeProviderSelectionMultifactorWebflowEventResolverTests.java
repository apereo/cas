package org.apereo.cas.web.flow.resolver.impl;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.ChainingMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.DefaultChainingMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.expression.support.LiteralExpression;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;
import org.springframework.webflow.engine.support.DefaultTransitionCriteria;
import org.springframework.webflow.execution.Event;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CompositeProviderSelectionMultifactorWebflowEventResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WebflowEvents")
class CompositeProviderSelectionMultifactorWebflowEventResolverTests {

    @TestConfiguration(value = "MultifactorTestConfiguration", proxyBeanMethods = false)
    static class MultifactorTestConfiguration {
        @Bean
        public MultifactorAuthenticationProvider dummyProvider() {
            return new TestMultifactorAuthenticationProvider();
        }
    }

    @TestConfiguration(value = "MultifactorBypassTestConfiguration", proxyBeanMethods = false)
    static class MultifactorBypassTestConfiguration {
        @Bean
        public MultifactorAuthenticationProvider dummyProvider() {
            val provider = new TestMultifactorAuthenticationProvider();
            val bypass = mock(MultifactorAuthenticationProviderBypassEvaluator.class);
            provider.setBypassEvaluator(bypass);
            return provider;
        }
    }

    @Import(CompositeProviderSelectionMultifactorWebflowEventResolverTests.MultifactorBypassTestConfiguration.class)
    @Nested
    @TestPropertySource(properties = "cas.authn.mfa.core.provider-selection.provider-selection-enabled=true")
    class BypassTests extends BaseCasWebflowMultifactorAuthenticationTests {
        @Autowired
        @Qualifier(CasDelegatingWebflowEventResolver.BEAN_NAME_SELECTIVE_AUTHENTICATION_EVENT_RESOLVER)
        private CasWebflowEventResolver compositeResolver;

        @Test
        void verifyCompositeBypass() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            val provider = new DefaultChainingMultifactorAuthenticationProvider(applicationContext,
                new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties));
            val event = new EventFactorySupport().event(this,
                ChainingMultifactorAuthenticationProvider.DEFAULT_IDENTIFIER,
                new LocalAttributeMap<>(MultifactorAuthenticationProvider.class.getName(), provider));
            val resolvedEvents = CollectionUtils.wrapHashSet(event);
            WebUtils.putResolvedEventsAsAttribute(context, resolvedEvents);
            val result = compositeResolver.resolve(context);
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

    }

    @Import(CompositeProviderSelectionMultifactorWebflowEventResolverTests.MultifactorTestConfiguration.class)
    @Nested
    @TestPropertySource(properties = "cas.authn.mfa.core.provider-selection.provider-selection-enabled=true")
    class DefaultTests extends BaseCasWebflowMultifactorAuthenticationTests {
        @Autowired
        @Qualifier(CasDelegatingWebflowEventResolver.BEAN_NAME_SELECTIVE_AUTHENTICATION_EVENT_RESOLVER)
        private CasWebflowEventResolver compositeResolver;

        @Autowired
        @Qualifier("multifactorAuthenticationProviderSelectionCookieGenerator")
        private CasCookieBuilder multifactorAuthenticationProviderSelectionCookieGenerator;

        @Test
        void verifyCompositeWithCookie() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            multifactorAuthenticationProviderSelectionCookieGenerator.addCookie(
                context.getHttpServletRequest(),
                context.getHttpServletResponse(), TestMultifactorAuthenticationProvider.ID);
            context.setRequestCookiesFromResponse();

            val provider = new DefaultChainingMultifactorAuthenticationProvider(applicationContext,
                new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties));

            val mfa = new TestMultifactorAuthenticationProvider();
            val bypass = mock(MultifactorAuthenticationProviderBypassEvaluator.class);
            when(bypass.shouldMultifactorAuthenticationProviderExecute(any(Authentication.class), any(RegisteredService.class),
                any(MultifactorAuthenticationProvider.class), any(HttpServletRequest.class), any(Service.class))).thenReturn(true);
            mfa.setBypassEvaluator(bypass);
            provider.addMultifactorAuthenticationProvider(mfa);

            val event = new EventFactorySupport().event(this,
                ChainingMultifactorAuthenticationProvider.DEFAULT_IDENTIFIER,
                new LocalAttributeMap<>(MultifactorAuthenticationProvider.class.getName(), provider));
            val resolvedEvents = CollectionUtils.wrapHashSet(event);
            val result = assertCompositeProvider(context, resolvedEvents, RegisteredServiceTestUtils.getAuthentication());
            assertEquals(TestMultifactorAuthenticationProvider.ID, result.iterator().next().getId());
        }

        @Test
        void verifyComposite() throws Throwable {
            val provider = new DefaultChainingMultifactorAuthenticationProvider(applicationContext,
                new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties));

            val mfa = new TestMultifactorAuthenticationProvider();
            val bypass = mock(MultifactorAuthenticationProviderBypassEvaluator.class);
            when(bypass.shouldMultifactorAuthenticationProviderExecute(any(Authentication.class), any(RegisteredService.class),
                any(MultifactorAuthenticationProvider.class), any(HttpServletRequest.class), any(Service.class))).thenReturn(true);
            mfa.setBypassEvaluator(bypass);
            provider.addMultifactorAuthenticationProvider(mfa);

            val event = new EventFactorySupport().event(this,
                ChainingMultifactorAuthenticationProvider.DEFAULT_IDENTIFIER,
                new LocalAttributeMap<>(MultifactorAuthenticationProvider.class.getName(), provider));
            val resolvedEvents = CollectionUtils.wrapHashSet(event);
            val result = assertCompositeProvider(resolvedEvents, RegisteredServiceTestUtils.getAuthentication());
            assertEquals(provider.getId(), result.iterator().next().getId());
        }

        @Test
        void verifyCompositeWithAuthnContextValidated() throws Throwable {
            TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);

            val provider = new DefaultChainingMultifactorAuthenticationProvider(applicationContext,
                new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties));
            provider.addMultifactorAuthenticationProvider(new TestMultifactorAuthenticationProvider());

            val event = new EventFactorySupport().event(this,
                ChainingMultifactorAuthenticationProvider.DEFAULT_IDENTIFIER,
                new LocalAttributeMap<>(MultifactorAuthenticationProvider.class.getName(), provider));
            val resolvedEvents = CollectionUtils.wrapHashSet(event);

            val attributes = new HashMap<String, List<Object>>();
            attributes.put(casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute(),
                List.of(TestMultifactorAuthenticationProvider.ID));
            val authentication = RegisteredServiceTestUtils.getAuthentication("casuser", attributes);
            val result = assertCompositeProvider(resolvedEvents, authentication);
            assertEquals(TestMultifactorAuthenticationProvider.ID, result.iterator().next().getId());
        }

        @Test
        void verifyNoComposite() throws Throwable {
            val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
            val resolvedEvents = CollectionUtils.wrapHashSet(new EventFactorySupport().event(this, provider.getId()));
            val result = assertCompositeProvider(resolvedEvents, RegisteredServiceTestUtils.getAuthentication());
            assertEquals(provider.getId(), result.iterator().next().getId());
        }

        private Set<Event> assertCompositeProvider(final Set<Event> resolvedEvents,
                                                   final Authentication authentication) throws Throwable {
            return assertCompositeProvider(MockRequestContext.create(applicationContext), resolvedEvents, authentication);
        }

        private Set<Event> assertCompositeProvider(final MockRequestContext context, final Set<Event> resolvedEvents,
                                                   final Authentication authentication) throws Throwable {
            val service = RegisteredServiceTestUtils.getRegisteredService();
            servicesManager.save(service);
            WebUtils.putRegisteredService(context, service);
            WebUtils.putAuthentication(authentication, context);
            WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());

            val targetResolver = new DefaultTargetStateResolver(TestMultifactorAuthenticationProvider.ID);
            val transition = new Transition(new DefaultTransitionCriteria(
                new LiteralExpression(TestMultifactorAuthenticationProvider.ID)), targetResolver);
            context.getRootFlow().getGlobalTransitionSet().add(transition);
            WebUtils.putResolvedEventsAsAttribute(context, resolvedEvents);
            val result = compositeResolver.resolve(context);
            assertNotNull(result);
            assertNotNull(MultifactorAuthenticationWebflowUtils.getResolvedMultifactorAuthenticationProviders(context));
            return result;
        }
    }
}
