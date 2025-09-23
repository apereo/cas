package org.apereo.cas.web.flow.resolver.impl;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockRequestContext;
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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.webflow.action.EventFactorySupport;
import java.util.UUID;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SelectiveMultifactorAuthenticationProviderWebflowEventResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("WebflowEvents")
class SelectiveMultifactorAuthenticationProviderWebflowEventResolverTests {

    @Import(WithMultipleProviders.TestMultifactorTestConfiguration.class)
    @Nested
    class WithMultipleProviders extends BaseCasWebflowMultifactorAuthenticationTests {
        @Autowired
        @Qualifier(CasDelegatingWebflowEventResolver.BEAN_NAME_SELECTIVE_AUTHENTICATION_EVENT_RESOLVER)
        private CasWebflowEventResolver selectiveAuthenticationProviderWebflowEventResolver;

        @Test
        void verifyOperation() throws Throwable {
            val context = MockRequestContext.create(applicationContext);

            val service = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
            servicesManager.save(service);
            WebUtils.putRegisteredService(context, service);

            WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(), context);
            WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService(service.getServiceId()));

            val providers = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(applicationContext).values();
            providers.forEach(provider -> context.addGlobalTransition(provider.getId(), provider.getId()));

            val resolvedEvents = providers.stream()
                .map(provider -> new EventFactorySupport().event(this, provider.getId()))
                .collect(Collectors.toList());
            WebUtils.putResolvedEventsAsAttribute(context, resolvedEvents);
            val result = selectiveAuthenticationProviderWebflowEventResolver.resolve(context);
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("mfa-fake", result.iterator().next().getId());
            assertNotNull(MultifactorAuthenticationWebflowUtils.getResolvedMultifactorAuthenticationProviders(context));
        }

        @TestConfiguration(value = "TestMultifactorTestConfiguration", proxyBeanMethods = false)
        static class TestMultifactorTestConfiguration {
            @Bean
            public MultifactorAuthenticationProvider dummyProvider() {
                return new TestMultifactorAuthenticationProvider();
            }

            @Bean
            public MultifactorAuthenticationProvider fakeProvider() {
                return new TestMultifactorAuthenticationProvider("mfa-fake").setOrder(100);
            }
        }
    }

    @Import(WithProvider.TestMultifactorTestConfiguration.class)
    @Nested
    class WithProvider extends BaseCasWebflowMultifactorAuthenticationTests {
        @Autowired
        @Qualifier(CasDelegatingWebflowEventResolver.BEAN_NAME_SELECTIVE_AUTHENTICATION_EVENT_RESOLVER)
        private CasWebflowEventResolver selectiveAuthenticationProviderWebflowEventResolver;

        @Test
        void verifyOperation() throws Throwable {
            val context = MockRequestContext.create(applicationContext);

            val service = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
            servicesManager.save(service);
            WebUtils.putRegisteredService(context, service);

            WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(), context);
            WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService(service.getServiceId()));

            context.addGlobalTransition(TestMultifactorAuthenticationProvider.ID, TestMultifactorAuthenticationProvider.ID);
            val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);

            val resolvedEvents = CollectionUtils.wrapHashSet(new EventFactorySupport().event(this, provider.getId()));
            WebUtils.putResolvedEventsAsAttribute(context, resolvedEvents);
            val result = selectiveAuthenticationProviderWebflowEventResolver.resolve(context);
            assertNotNull(result);
            assertNotNull(MultifactorAuthenticationWebflowUtils.getResolvedMultifactorAuthenticationProviders(context));
            assertEquals(provider.getId(), result.iterator().next().getId());
        }

        @TestConfiguration(value = "TestMultifactorTestConfiguration", proxyBeanMethods = false)
        static class TestMultifactorTestConfiguration {
            @Bean
            public MultifactorAuthenticationProvider dummyProvider() {
                return new TestMultifactorAuthenticationProvider();
            }
        }
    }

    @Nested
    class WithoutProvider extends BaseCasWebflowMultifactorAuthenticationTests {
        @Autowired
        @Qualifier(CasDelegatingWebflowEventResolver.BEAN_NAME_SELECTIVE_AUTHENTICATION_EVENT_RESOLVER)
        private CasWebflowEventResolver selectiveAuthenticationProviderWebflowEventResolver;
        
        @Test
        void verifyEmptyOperation() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            val service = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
            servicesManager.save(service);
            WebUtils.putRegisteredService(context, service);
            WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(), context);
            WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService(service.getServiceId()));

            context.addGlobalTransition(TestMultifactorAuthenticationProvider.ID, TestMultifactorAuthenticationProvider.ID);

            val resolvedEvents = CollectionUtils.wrapHashSet(new EventFactorySupport().event(this, "mfa-something"));
            WebUtils.putResolvedEventsAsAttribute(context, resolvedEvents);
            val result = selectiveAuthenticationProviderWebflowEventResolver.resolve(context);
            assertNotNull(result);
            assertTrue(MultifactorAuthenticationWebflowUtils.getResolvedMultifactorAuthenticationProviders(context).isEmpty());
        }

        @Test
        void verifyNoProvider() throws Throwable {
            val context = MockRequestContext.create(applicationContext);

            val service = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
            servicesManager.save(service);
            WebUtils.putRegisteredService(context, service);

            WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(), context);
            WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService(service.getServiceId()));

            context.addGlobalTransition(TestMultifactorAuthenticationProvider.ID, TestMultifactorAuthenticationProvider.ID);
            val resolvedEvents = CollectionUtils.wrapHashSet(new EventFactorySupport().event(this, "mfa-something"));
            WebUtils.putResolvedEventsAsAttribute(context, resolvedEvents);
            val result = selectiveAuthenticationProviderWebflowEventResolver.resolve(context);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }
    }

}
