package org.apereo.cas.web.flow.authentication;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.AnyAuthenticationHandlerRegisteredServiceAuthenticationPolicyCriteria;
import org.apereo.cas.services.DefaultRegisteredServiceAuthenticationPolicy;
import org.apereo.cas.services.DefaultServicesManager;
import org.apereo.cas.services.InMemoryServiceRegistry;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.ServicesManagerConfigurationContext;
import org.apereo.cas.ticket.registry.DefaultTicketRegistry;
import org.apereo.cas.ticket.registry.DefaultTicketRegistrySupport;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.support.WebUtils;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
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
 * This is {@link RegisteredServiceAuthenticationPolicySingleSignOnParticipationStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Authentication")
public class RegisteredServiceAuthenticationPolicySingleSignOnParticipationStrategyTests {
    private AuthenticationEventExecutionPlan authenticationEventExecutionPlan;

    @BeforeEach
    public void setup() {
        this.authenticationEventExecutionPlan = new DefaultAuthenticationEventExecutionPlan();
    }

    @Test
    public void verifyInputFails() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val regService = RegisteredServiceTestUtils.getRegisteredService("serviceid1");
        val servicesManager = getServicesManager(regService);
        servicesManager.load();
        val strategy = getSingleSignOnParticipationStrategy(applicationContext, servicesManager, new DefaultTicketRegistry());
        assertTrue(strategy.isParticipating(context));
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService("serviceid1"));
        assertTrue(strategy.isParticipating(context));
        regService.setAuthenticationPolicy(new DefaultRegisteredServiceAuthenticationPolicy()
            .setRequiredAuthenticationHandlers(Set.of("Handler1")));
        assertTrue(strategy.isParticipating(context));
    }

    @Test
    public void verifyNoServiceOrSso() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val servicesManager = getServicesManager(CoreAuthenticationTestUtils.getRegisteredService("serviceid1"));
        servicesManager.load();

        val strategy = getSingleSignOnParticipationStrategy(applicationContext, servicesManager, new DefaultTicketRegistry());

        assertFalse(strategy.supports(context));
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService("serviceid1"));
        assertFalse(strategy.supports(context));
        assertEquals(0, strategy.getOrder());
    }

    @Test
    public void verifySsoWithMismatchedHandlers() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val svc = CoreAuthenticationTestUtils.getRegisteredService("serviceid1");
        val policy = new DefaultRegisteredServiceAuthenticationPolicy();
        policy.setRequiredAuthenticationHandlers(Set.of("SomeOtherHandler"));
        when(svc.getAuthenticationPolicy()).thenReturn(policy);
        when(svc.matches(anyString())).thenReturn(Boolean.TRUE);

        val servicesManager = getServicesManager(svc);
        servicesManager.load();

        val ticketRegistry = new DefaultTicketRegistry();
        val strategy = getSingleSignOnParticipationStrategy(applicationContext, servicesManager, ticketRegistry);

        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService("serviceid1"));
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        assertTrue(strategy.supports(context));
        assertFalse(strategy.isParticipating(context));
    }

    @Test
    public void verifySsoWithOneAuthNHandler() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val svc = CoreAuthenticationTestUtils.getRegisteredService("serviceid1");
        val policy = new DefaultRegisteredServiceAuthenticationPolicy();
        policy.setRequiredAuthenticationHandlers(Set.of(SimpleTestUsernamePasswordAuthenticationHandler.class.getSimpleName()));
        when(svc.getAuthenticationPolicy()).thenReturn(policy);
        when(svc.matches(anyString())).thenReturn(Boolean.TRUE);
        val servicesManager = getServicesManager(svc);
        servicesManager.load();

        authenticationEventExecutionPlan.registerAuthenticationHandler(new SimpleTestUsernamePasswordAuthenticationHandler());
        val ticketRegistry = new DefaultTicketRegistry();
        val strategy = getSingleSignOnParticipationStrategy(applicationContext, servicesManager, ticketRegistry);

        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService("serviceid1"));
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        assertTrue(strategy.supports(context));
        assertTrue(strategy.isParticipating(context));
    }

    @Test
    public void verifySsoWithMultipleAuthNHandlerAnyCriteria() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val svc = CoreAuthenticationTestUtils.getRegisteredService("serviceid1");
        val policy = new DefaultRegisteredServiceAuthenticationPolicy();
        policy.setRequiredAuthenticationHandlers(Set.of("SomeOtherHandler1", SimpleTestUsernamePasswordAuthenticationHandler.class.getSimpleName()));
        policy.setCriteria(new AnyAuthenticationHandlerRegisteredServiceAuthenticationPolicyCriteria());

        when(svc.getAuthenticationPolicy()).thenReturn(policy);
        when(svc.matches(anyString())).thenReturn(Boolean.TRUE);
        val servicesManager = getServicesManager(svc);
        servicesManager.load();

        authenticationEventExecutionPlan.registerAuthenticationHandlers(
            List.of(new SimpleTestUsernamePasswordAuthenticationHandler(), new SimpleTestUsernamePasswordAuthenticationHandler("SomeOtherHandler1")));
        val ticketRegistry = new DefaultTicketRegistry();
        val strategy = getSingleSignOnParticipationStrategy(applicationContext, servicesManager, ticketRegistry);

        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService("serviceid1"));
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        assertTrue(strategy.supports(context));
        assertTrue(strategy.isParticipating(context));
    }

    private SingleSignOnParticipationStrategy getSingleSignOnParticipationStrategy(
        final ConfigurableApplicationContext applicationContext,
        final ServicesManager servicesManager,
        final TicketRegistry ticketRegistry) {

        return new RegisteredServiceAuthenticationPolicySingleSignOnParticipationStrategy(servicesManager,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()),
            new DefaultTicketRegistrySupport(ticketRegistry),
            applicationContext, authenticationEventExecutionPlan);
    }

    private static ServicesManager getServicesManager(final RegisteredService svc) {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val dao = new InMemoryServiceRegistry(appCtx, List.of(svc), new ArrayList<>());
        val context = ServicesManagerConfigurationContext.builder()
            .serviceRegistry(dao)
            .applicationContext(appCtx)
            .environments(new HashSet<>(0))
            .servicesCache(Caffeine.newBuilder().build())
            .build();
        return new DefaultServicesManager(context);
    }
}
