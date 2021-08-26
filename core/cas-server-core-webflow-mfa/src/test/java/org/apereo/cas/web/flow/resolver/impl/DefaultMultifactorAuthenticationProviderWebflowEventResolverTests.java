package org.apereo.cas.web.flow.resolver.impl;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.expression.support.LiteralExpression;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;
import org.springframework.webflow.engine.support.DefaultTransitionCriteria;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultMultifactorAuthenticationProviderWebflowEventResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("WebflowEvents")
@Import(DefaultMultifactorAuthenticationProviderWebflowEventResolverTests.MultifactorAuthenticationTestConfiguration.class)
@TestPropertySource(properties = "cas.authn.mfa.triggers.global.global-provider-id=mfa-dummy")
public class DefaultMultifactorAuthenticationProviderWebflowEventResolverTests extends BaseCasWebflowMultifactorAuthenticationTests {
    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;

    @Autowired
    @Qualifier("globalAuthenticationPolicyWebflowEventResolver")
    private CasWebflowEventResolver globalAuthenticationPolicyWebflowEventResolver;

    @BeforeEach
    public void setup() {
        super.setup();
        servicesManager.deleteAll();
    }

    @Test
    public void verifyEventResolverWithMfa() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        val targetResolver = new DefaultTargetStateResolver(TestMultifactorAuthenticationProvider.ID);
        val transition = new Transition(new DefaultTransitionCriteria(
            new LiteralExpression(TestMultifactorAuthenticationProvider.ID)), targetResolver);
        context.getRootFlow().getGlobalTransitionSet().add(transition);
        
        val tgt = new MockTicketGrantingTicket("casuser");
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        WebUtils.putAuthentication(tgt.getAuthentication(), context);
        val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
        WebUtils.putServiceIntoFlowScope(context, service);

        val registeredService = RegisteredServiceTestUtils.getRegisteredService(Map.of());
        registeredService.setServiceId(service.getId());
        servicesManager.save(registeredService);
        WebUtils.putRegisteredService(context, registeredService);

        val builder = mock(AuthenticationResultBuilder.class);
        when(builder.getInitialAuthentication()).thenReturn(Optional.of(tgt.getAuthentication()));
        when(builder.collect(any(Authentication.class))).thenReturn(builder);
        WebUtils.putAuthenticationResultBuilder(builder, context);

        assertEquals(TestMultifactorAuthenticationProvider.ID,
            globalAuthenticationPolicyWebflowEventResolver.resolve(context).iterator().next().getId());
    }

    @Test
    public void verifyEventResolverWithMfaIgnoresExecForService() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val tgt = new MockTicketGrantingTicket("casuser");
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        WebUtils.putAuthentication(tgt.getAuthentication(), context);
        val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
        WebUtils.putServiceIntoFlowScope(context, service);
        
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(Map.of());
        registeredService.setServiceId(service.getId());
        registeredService.setMultifactorPolicy(new DefaultRegisteredServiceMultifactorPolicy().setBypassEnabled(true));
        servicesManager.save(registeredService);
        WebUtils.putRegisteredService(context, registeredService);

        val builder = mock(AuthenticationResultBuilder.class);
        when(builder.getInitialAuthentication()).thenReturn(Optional.of(tgt.getAuthentication()));
        when(builder.collect(any(Authentication.class))).thenReturn(builder);
        WebUtils.putAuthenticationResultBuilder(builder, context);

        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS,
            initialAuthenticationAttemptWebflowEventResolver.resolve(context).iterator().next().getId());
    }

    @TestConfiguration("MultifactorAuthenticationTestConfiguration")
    public static class MultifactorAuthenticationTestConfiguration {
        @Bean
        public MultifactorAuthenticationProvider dummyProvider() {
            return new TestMultifactorAuthenticationProvider();
        }
    }
}

