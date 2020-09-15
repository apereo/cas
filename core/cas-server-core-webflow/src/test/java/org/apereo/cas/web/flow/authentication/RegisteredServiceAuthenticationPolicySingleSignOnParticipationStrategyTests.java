package org.apereo.cas.web.flow.authentication;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.AllowedAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria;
import org.apereo.cas.services.DefaultRegisteredServiceAuthenticationPolicy;
import org.apereo.cas.services.DefaultServicesManager;
import org.apereo.cas.services.ExcludedAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria;
import org.apereo.cas.services.InMemoryServiceRegistry;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManagerConfigurationContext;
import org.apereo.cas.ticket.registry.DefaultTicketRegistry;
import org.apereo.cas.ticket.registry.DefaultTicketRegistrySupport;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.support.WebUtils;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RegisteredServiceAuthenticationPolicySingleSignOnParticipationStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Webflow")
public class RegisteredServiceAuthenticationPolicySingleSignOnParticipationStrategyTests {
    @Test
    public void verifyNoServiceOrPolicy() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val svc = CoreAuthenticationTestUtils.getRegisteredService("serviceid1");
        val policy = new DefaultRegisteredServiceAuthenticationPolicy();
        policy.setCriteria(null);
        when(svc.getAuthenticationPolicy()).thenReturn(policy);
        val ticketRegistry = new DefaultTicketRegistry();
        val strategy = getSingleSignOnStrategy(svc, ticketRegistry);
        assertTrue(strategy.isParticipating(context));
        
        WebUtils.putRegisteredService(context, svc);
        assertEquals(0, strategy.getOrder());
        assertFalse(strategy.supports(context));

        when(svc.getAuthenticationPolicy()).thenReturn(null);
        assertTrue(strategy.isParticipating(context));
    }

    @Test
    public void verifyNoServiceOrSso() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val svc = CoreAuthenticationTestUtils.getRegisteredService("serviceid1");
        val ticketRegistry = new DefaultTicketRegistry();
        val strategy = getSingleSignOnStrategy(svc, ticketRegistry);

        assertFalse(strategy.supports(context));
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService("serviceid1"));
        assertFalse(strategy.supports(context));
    }

    @Test
    public void verifySsoWithMismatchedHandlers() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val svc = CoreAuthenticationTestUtils.getRegisteredService("serviceid1");
        val policy = new DefaultRegisteredServiceAuthenticationPolicy();
        policy.setRequiredAuthenticationHandlers(Set.of("SomeOtherHandler"));
        policy.setCriteria(new AllowedAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria());
        when(svc.getAuthenticationPolicy()).thenReturn(policy);
        when(svc.matches(anyString())).thenReturn(Boolean.TRUE);

        val ticketRegistry = new DefaultTicketRegistry();
        val strategy = getSingleSignOnStrategy(svc, ticketRegistry);

        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService("serviceid1"));
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        assertTrue(strategy.supports(context));
        assertFalse(strategy.isParticipating(context));
    }

    @Test
    public void verifySsoWithHandlers() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val svc = CoreAuthenticationTestUtils.getRegisteredService("serviceid1");
        val policy = new DefaultRegisteredServiceAuthenticationPolicy();
        policy.setRequiredAuthenticationHandlers(
            Set.of(SimpleTestUsernamePasswordAuthenticationHandler.class.getSimpleName()));
        when(svc.getAuthenticationPolicy()).thenReturn(policy);
        when(svc.matches(anyString())).thenReturn(Boolean.TRUE);

        val ticketRegistry = new DefaultTicketRegistry();
        val strategy = getSingleSignOnStrategy(svc, ticketRegistry);

        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService("serviceid1"));
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        assertTrue(strategy.supports(context));
        assertTrue(strategy.isParticipating(context));
    }

    @Test
    public void verifySsoWithExcludedHandlers() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val svc = CoreAuthenticationTestUtils.getRegisteredService("serviceid1");
        val policy = new DefaultRegisteredServiceAuthenticationPolicy();
        policy.setCriteria(new ExcludedAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria());
        policy.setExcludedAuthenticationHandlers(
            Set.of(SimpleTestUsernamePasswordAuthenticationHandler.class.getName()));
        when(svc.getAuthenticationPolicy()).thenReturn(policy);
        when(svc.matches(anyString())).thenReturn(Boolean.TRUE);

        val ticketRegistry = new DefaultTicketRegistry();
        val strategy = getSingleSignOnStrategy(svc, ticketRegistry);

        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService("serviceid1"));
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        assertTrue(strategy.supports(context));
        assertFalse(strategy.isParticipating(context));
    }

    private static SingleSignOnParticipationStrategy getSingleSignOnStrategy(final RegisteredService svc,
        final TicketRegistry ticketRegistry) {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();

        val context = ServicesManagerConfigurationContext.builder()
            .serviceRegistry(new InMemoryServiceRegistry(appCtx, List.of(svc), List.of()))
            .applicationContext(appCtx)
            .environments(new HashSet<>(0))
            .servicesCache(Caffeine.newBuilder().build())
            .build();
        val servicesManager = new DefaultServicesManager(context);
        servicesManager.load();

        val authenticationExecutionPlan = new DefaultAuthenticationEventExecutionPlan();
        authenticationExecutionPlan.registerAuthenticationHandler(new SimpleTestUsernamePasswordAuthenticationHandler());

        val strategy = new RegisteredServiceAuthenticationPolicySingleSignOnParticipationStrategy(servicesManager,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()),
            new DefaultTicketRegistrySupport(ticketRegistry),
            authenticationExecutionPlan, appCtx);
        return strategy;
    }
}
