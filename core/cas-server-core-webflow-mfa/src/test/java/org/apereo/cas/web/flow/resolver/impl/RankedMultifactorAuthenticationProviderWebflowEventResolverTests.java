package org.apereo.cas.web.flow.resolver.impl;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.CentralAuthenticationService;
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
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.expression.support.LiteralExpression;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;
import org.springframework.webflow.engine.support.DefaultTransitionCriteria;
import org.springframework.webflow.test.MockRequestContext;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RankedMultifactorAuthenticationProviderWebflowEventResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Webflow")
@DirtiesContext
public class RankedMultifactorAuthenticationProviderWebflowEventResolverTests extends BaseCasWebflowMultifactorAuthenticationTests {
    @Autowired
    @Qualifier("rankedAuthenticationProviderWebflowEventResolver")
    private CasDelegatingWebflowEventResolver resolver;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService cas;

    @BeforeEach
    public void setup() {
        this.servicesManager.deleteAll();
    }
    
    @Test
    public void verifyWithNoTicketOrService() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

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
    public void verifyAuthnResolvesEvent() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());

        val service = RegisteredServiceTestUtils.getRegisteredService(Map.of());
        servicesManager.save(service);
        WebUtils.putRegisteredService(context, service);

        val tgt = new MockTicketGrantingTicket("casuser");
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        cas.addTicket(tgt);

        WebUtils.putCredential(context, RegisteredServiceTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon"));
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, resolver.resolve(context).iterator().next().getId());
    }

    @Test
    public void verifyAuthnResolvesMfaEvent() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.addParameter(casProperties.getAuthn().getMfa().getRequestParameter(), TestMultifactorAuthenticationProvider.ID);
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
        val service = RegisteredServiceTestUtils.getRegisteredService(Map.of());
        servicesManager.save(service);
        WebUtils.putRegisteredService(context, service);

        val tgt = new MockTicketGrantingTicket("casuser");
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        cas.addTicket(tgt);

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
    public void verifyAuthnResolvesMfaContextValidated() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.addParameter(casProperties.getAuthn().getMfa().getRequestParameter(), TestMultifactorAuthenticationProvider.ID);
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val tgt = new MockTicketGrantingTicket("casuser", Map.of(),
            Map.of(casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(), List.of(TestMultifactorAuthenticationProvider.ID)));
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        cas.addTicket(tgt);

        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
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
        registeredService.setMultifactorPolicy(multifactorPolicy);
        WebUtils.putRegisteredService(context, registeredService);
        assertEquals(TestMultifactorAuthenticationProvider.ID, resolver.resolveSingle(context).getId());
    }

    @Test
    public void verifyAddDelegate() {
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                resolver.addDelegate(mock(CasWebflowEventResolver.class));
                resolver.addDelegate(mock(CasWebflowEventResolver.class), 0);
            }
        });
    }
}
