package org.apereo.cas.web.flow.authentication;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.DefaultRegisteredServiceAuthenticationPolicy;
import org.apereo.cas.services.DefaultServicesManager;
import org.apereo.cas.services.InMemoryServiceRegistry;
import org.apereo.cas.ticket.registry.DefaultTicketRegistry;
import org.apereo.cas.ticket.registry.DefaultTicketRegistrySupport;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RequiredAuthenticationHandlersSingleSignOnParticipationStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Webflow")
public class RequiredAuthenticationHandlersSingleSignOnParticipationStrategyTests {
    @Test
    public void verifyNoServiceOrSso() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val svc = CoreAuthenticationTestUtils.getRegisteredService("serviceid1");
        val dao = new InMemoryServiceRegistry(mock(ApplicationEventPublisher.class), List.of(svc), new ArrayList<>());

        val defaultServicesManager = new DefaultServicesManager(dao, mock(ApplicationEventPublisher.class), new HashSet<>());
        defaultServicesManager.load();

        val strategy = new RequiredAuthenticationHandlersSingleSignOnParticipationStrategy(defaultServicesManager,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()),
            new DefaultTicketRegistrySupport(new DefaultTicketRegistry()));

        assertFalse(strategy.supports(context));
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService("serviceid1"));
        assertFalse(strategy.supports(context));
    }

    @Test
    public void verifySsoWithMismatchedHandlers() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val svc = CoreAuthenticationTestUtils.getRegisteredService("serviceid1");
        val policy = new DefaultRegisteredServiceAuthenticationPolicy();
        policy.setRequiredAuthenticationHandlers(Set.of("SomeOtherHandler"));
        when(svc.getAuthenticationPolicy()).thenReturn(policy);
        when(svc.matches(anyString())).thenReturn(Boolean.TRUE);

        val dao = new InMemoryServiceRegistry(mock(ApplicationEventPublisher.class),
            List.of(svc), new ArrayList<>());

        val servicesManager = new DefaultServicesManager(dao,
            mock(ApplicationEventPublisher.class), new HashSet<>());
        servicesManager.load();

        val ticketRegistry = new DefaultTicketRegistry();
        val strategy = new RequiredAuthenticationHandlersSingleSignOnParticipationStrategy(servicesManager,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()),
            new DefaultTicketRegistrySupport(ticketRegistry));

        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService("serviceid1"));
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        assertTrue(strategy.supports(context));
        assertFalse(strategy.isParticipating(context));
    }

    @Test
    public void verifySsoWithHandlers() {
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

        val dao = new InMemoryServiceRegistry(mock(ApplicationEventPublisher.class),
            List.of(svc), new ArrayList<>());

        val servicesManager = new DefaultServicesManager(dao,
            mock(ApplicationEventPublisher.class), new HashSet<>());
        servicesManager.load();

        val ticketRegistry = new DefaultTicketRegistry();
        val strategy = new RequiredAuthenticationHandlersSingleSignOnParticipationStrategy(servicesManager,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()),
            new DefaultTicketRegistrySupport(ticketRegistry));

        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService("serviceid1"));
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        assertTrue(strategy.supports(context));
        assertTrue(strategy.isParticipating(context));
    }
}
