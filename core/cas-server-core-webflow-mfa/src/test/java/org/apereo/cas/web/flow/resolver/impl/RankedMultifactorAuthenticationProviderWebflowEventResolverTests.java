package org.apereo.cas.web.flow.resolver.impl;

import module java.base;
import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.expression.support.LiteralExpression;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;
import org.springframework.webflow.engine.support.DefaultTransitionCriteria;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RankedMultifactorAuthenticationProviderWebflowEventResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WebflowEvents")
@Import(RankedMultifactorAuthenticationProviderWebflowEventResolverTests.MultifactorTestConfiguration.class)
class RankedMultifactorAuthenticationProviderWebflowEventResolverTests extends BaseCasWebflowMultifactorAuthenticationTests {
    @Autowired
    @Qualifier("rankedAuthenticationProviderWebflowEventResolver")
    private CasDelegatingWebflowEventResolver resolver;

    @Test
    void verifyWithNoTicketOrService() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, resolver.resolve(context).iterator().next().getId());
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());

        val service = RegisteredServiceTestUtils.getRegisteredService(Map.of());
        servicesManager.save(service);
        WebUtils.putRegisteredService(context, service);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, resolver.resolve(context).iterator().next().getId());

        val tgt = new MockTicketGrantingTicket("casuser");
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, resolver.resolve(context).iterator().next().getId());
    }

    @Test
    void verifyAuthnHandledWithRenew() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setParameter(CasProtocolConstants.PARAMETER_RENEW, "true");

        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());

        val service = RegisteredServiceTestUtils.getRegisteredService(Map.of());
        servicesManager.save(service);
        WebUtils.putRegisteredService(context, service);

        val tgt = new MockTicketGrantingTicket("casuser");
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        ticketRegistry.addTicket(tgt);

        WebUtils.putCredential(context, RegisteredServiceTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon"));
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, resolver.resolve(context).iterator().next().getId());
    }

    @Test
    void verifyAuthnResolvesEvent() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());

        val service = RegisteredServiceTestUtils.getRegisteredService(Map.of());
        servicesManager.save(service);
        WebUtils.putRegisteredService(context, service);

        val tgt = new MockTicketGrantingTicket("casuser");
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        ticketRegistry.addTicket(tgt);

        WebUtils.putCredential(context, RegisteredServiceTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon"));
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, resolver.resolve(context).iterator().next().getId());
    }

    @Test
    void verifyAuthnResolvesMfaEvent() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setParameter(casProperties.getAuthn().getMfa().getTriggers().getHttp().getRequestParameter(), TestMultifactorAuthenticationProvider.ID);

        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
        val service = RegisteredServiceTestUtils.getRegisteredService(Map.of());
        servicesManager.save(service);
        WebUtils.putRegisteredService(context, service);

        val tgt = new MockTicketGrantingTicket("casuser");
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        ticketRegistry.addTicket(tgt);

        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        WebUtils.putCredential(context,
            RegisteredServiceTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon"));

        val targetResolver = new DefaultTargetStateResolver(TestMultifactorAuthenticationProvider.ID);
        val transition = new Transition(new DefaultTransitionCriteria(
            new LiteralExpression(TestMultifactorAuthenticationProvider.ID)), targetResolver);
        context.getRootFlow().getGlobalTransitionSet().add(transition);
        assertEquals(TestMultifactorAuthenticationProvider.ID, resolver.resolveSingle(context).getId());
    }

    @Test
    void verifyAuthnResolvesMfaContextValidatedNoForceExecution() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setParameter(casProperties.getAuthn().getMfa().getTriggers().getHttp().getRequestParameter(), TestMultifactorAuthenticationProvider.ID);

        val tgt = new MockTicketGrantingTicket("casuser", Map.of(),
            Map.of(casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute(), List.of(TestMultifactorAuthenticationProvider.ID)));
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        ticketRegistry.addTicket(tgt);

        WebUtils.putCredential(context,
            RegisteredServiceTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon"));

        val targetResolver = new DefaultTargetStateResolver(TestMultifactorAuthenticationProvider.ID);
        val transition = new Transition(new DefaultTransitionCriteria(
            new LiteralExpression(TestMultifactorAuthenticationProvider.ID)), targetResolver);
        context.getRootFlow().getGlobalTransitionSet().add(transition);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, resolver.resolve(context).iterator().next().getId());
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(Map.of());
        WebUtils.putRegisteredService(context, registeredService);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, resolver.resolveSingle(context).getId());
    }

    @Test
    void verifyAuthnResolvesMfaContextValidated() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setParameter(casProperties.getAuthn().getMfa().getTriggers().getHttp().getRequestParameter(), TestMultifactorAuthenticationProvider.ID);

        val tgt = new MockTicketGrantingTicket("casuser", Map.of(),
            Map.of(casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute(), List.of(TestMultifactorAuthenticationProvider.ID)));
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        ticketRegistry.addTicket(tgt);

        WebUtils.putCredential(context,
            RegisteredServiceTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon"));

        val targetResolver = new DefaultTargetStateResolver(TestMultifactorAuthenticationProvider.ID);
        val transition = new Transition(new DefaultTransitionCriteria(
            new LiteralExpression(TestMultifactorAuthenticationProvider.ID)), targetResolver);
        context.getRootFlow().getGlobalTransitionSet().add(transition);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, resolver.resolve(context).iterator().next().getId());

        val registeredService = RegisteredServiceTestUtils.getRegisteredService(Map.of());
        val multifactorPolicy = new DefaultRegisteredServiceMultifactorPolicy();
        multifactorPolicy.setForceExecution(true);
        registeredService.setMultifactorAuthenticationPolicy(multifactorPolicy);
        WebUtils.putRegisteredService(context, registeredService);
        assertEquals(TestMultifactorAuthenticationProvider.ID, resolver.resolveSingle(context).getId());
    }

    @Test
    void verifyAddDelegate() {
        assertDoesNotThrow(() -> {
            resolver.addDelegate(mock(CasWebflowEventResolver.class));
            resolver.addDelegate(mock(CasWebflowEventResolver.class), 0);
        });
    }

    @TestConfiguration(value = "MultifactorTestConfiguration", proxyBeanMethods = false)
    static class MultifactorTestConfiguration {
        @Bean
        public MultifactorAuthenticationProvider dummyProvider() {
            return new TestMultifactorAuthenticationProvider();
        }
    }
}
