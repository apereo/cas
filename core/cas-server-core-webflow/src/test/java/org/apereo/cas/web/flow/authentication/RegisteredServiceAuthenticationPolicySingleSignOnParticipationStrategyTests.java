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
import org.apereo.cas.services.DefaultServicesManagerRegisteredServiceLocator;
import org.apereo.cas.services.ExcludedAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria;
import org.apereo.cas.services.InMemoryServiceRegistry;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManagerConfigurationContext;
import org.apereo.cas.ticket.registry.DefaultTicketRegistry;
import org.apereo.cas.ticket.registry.DefaultTicketRegistrySupport;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.flow.SingleSignOnParticipationRequest;
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
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegisteredServiceAuthenticationPolicySingleSignOnParticipationStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Webflow")
public class RegisteredServiceAuthenticationPolicySingleSignOnParticipationStrategyTests {
    private static SingleSignOnParticipationStrategy getSingleSignOnStrategy(final RegisteredService svc,
                                                                             final TicketRegistry ticketRegistry) {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();

        val context = ServicesManagerConfigurationContext.builder()
            .serviceRegistry(new InMemoryServiceRegistry(appCtx, List.of(svc), List.of()))
            .applicationContext(appCtx)
            .environments(new HashSet<>(0))
            .servicesCache(Caffeine.newBuilder().build())
            .registeredServiceLocators(List.of(new DefaultServicesManagerRegisteredServiceLocator()))
            .build();
        val servicesManager = new DefaultServicesManager(context);
        servicesManager.load();

        val authenticationExecutionPlan = new DefaultAuthenticationEventExecutionPlan();
        authenticationExecutionPlan.registerAuthenticationHandler(new SimpleTestUsernamePasswordAuthenticationHandler());

        return new RegisteredServiceAuthenticationPolicySingleSignOnParticipationStrategy(servicesManager,
            new DefaultTicketRegistrySupport(ticketRegistry),
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()),
            authenticationExecutionPlan, appCtx);
    }

    @Test
    public void verifyNoServiceOrPolicy() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val svc = RegisteredServiceTestUtils.getRegisteredService("serviceid1", Map.of());
        val policy = new DefaultRegisteredServiceAuthenticationPolicy();
        policy.setCriteria(null);
        svc.setAuthenticationPolicy(policy);
        val ticketRegistry = new DefaultTicketRegistry();
        val strategy = getSingleSignOnStrategy(svc, ticketRegistry);

        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(request)
            .requestContext(context)
            .build();
        assertTrue(strategy.isParticipating(ssoRequest));

        WebUtils.putRegisteredService(context, svc);
        assertEquals(0, strategy.getOrder());
        assertFalse(strategy.supports(ssoRequest));
        svc.setAuthenticationPolicy(null);
        assertTrue(strategy.isParticipating(ssoRequest));
    }

    @Test
    public void verifyNoServiceOrSso() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val svc = RegisteredServiceTestUtils.getRegisteredService("serviceid1");
        val ticketRegistry = new DefaultTicketRegistry();
        val strategy = getSingleSignOnStrategy(svc, ticketRegistry);

        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(request)
            .requestContext(context)
            .build();
        assertFalse(strategy.supports(ssoRequest));
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService("unknown"));
        assertFalse(strategy.supports(ssoRequest));
    }

    @Test
    public void verifySsoWithMismatchedHandlers() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val svc = RegisteredServiceTestUtils.getRegisteredService("serviceid1", Map.of());
        val policy = new DefaultRegisteredServiceAuthenticationPolicy();
        policy.setRequiredAuthenticationHandlers(Set.of("SomeOtherHandler"));
        policy.setCriteria(new AllowedAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria());
        svc.setAuthenticationPolicy(policy);

        val ticketRegistry = new DefaultTicketRegistry();
        val strategy = getSingleSignOnStrategy(svc, ticketRegistry);

        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService("serviceid1"));
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);

        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(request)
            .requestContext(context)
            .build();
        assertTrue(strategy.supports(ssoRequest));
        assertFalse(strategy.isParticipating(ssoRequest));
    }

    @Test
    public void verifySsoWithHandlers() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val svc = RegisteredServiceTestUtils.getRegisteredService("serviceid1", Map.of());
        val policy = new DefaultRegisteredServiceAuthenticationPolicy();
        policy.setRequiredAuthenticationHandlers(
            Set.of(SimpleTestUsernamePasswordAuthenticationHandler.class.getSimpleName()));
        svc.setAuthenticationPolicy(policy);

        val ticketRegistry = new DefaultTicketRegistry();
        val strategy = getSingleSignOnStrategy(svc, ticketRegistry);

        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService("serviceid1"));
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);

        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(request)
            .requestContext(context)
            .build();
        assertTrue(strategy.supports(ssoRequest));
        assertTrue(strategy.isParticipating(ssoRequest));
    }

    @Test
    public void verifySsoWithExcludedHandlers() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val svc = RegisteredServiceTestUtils.getRegisteredService("serviceid1", Map.of());
        val policy = new DefaultRegisteredServiceAuthenticationPolicy();
        policy.setCriteria(new ExcludedAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria());
        policy.setExcludedAuthenticationHandlers(
            Set.of(SimpleTestUsernamePasswordAuthenticationHandler.class.getName()));
        svc.setAuthenticationPolicy(policy);

        val ticketRegistry = new DefaultTicketRegistry();
        val strategy = getSingleSignOnStrategy(svc, ticketRegistry);

        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService("serviceid1"));
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);

        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(request)
            .requestContext(context)
            .build();
        assertTrue(strategy.supports(ssoRequest));
        assertFalse(strategy.isParticipating(ssoRequest));
    }
}
