package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy;
import org.apereo.cas.services.DefaultServicesManager;
import org.apereo.cas.services.InMemoryServiceRegistry;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManagerConfigurationContext;
import org.apereo.cas.ticket.registry.DefaultTicketRegistry;
import org.apereo.cas.ticket.registry.DefaultTicketRegistrySupport;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.support.WebUtils;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DelegatedAuthenticationSingleSignOnParticipationStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Simple")
public class DelegatedAuthenticationSingleSignOnParticipationStrategyTests {
    @Test
    public void verifyNoServiceOrPolicy() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val svc = CoreAuthenticationTestUtils.getRegisteredService("serviceid1");
        val policy = new DefaultRegisteredServiceAccessStrategy();
        when(svc.getAccessStrategy()).thenReturn(policy);
        val ticketRegistry = new DefaultTicketRegistry();
        
        val strategy = getSingleSignOnStrategy(svc, ticketRegistry);
        assertFalse(strategy.supports(context));
        assertTrue(strategy.isParticipating(context));

        WebUtils.putRegisteredService(context, svc);
        assertEquals(0, strategy.getOrder());
        assertTrue(strategy.supports(context));
        assertTrue(strategy.isParticipating(context));

        policy.setDelegatedAuthenticationPolicy(null);
        assertFalse(strategy.supports(context));
        assertTrue(strategy.isParticipating(context));
    }

    @Test
    public void verifySsoWithMismatchedClient() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val svc = CoreAuthenticationTestUtils.getRegisteredService("serviceid1");
        val policy = new DefaultRegisteredServiceAccessStrategy();
        policy.setDelegatedAuthenticationPolicy(
            new DefaultRegisteredServiceDelegatedAuthenticationPolicy().setAllowedProviders(List.of("Client2")));
        when(svc.getAccessStrategy()).thenReturn(policy);
        when(svc.matches(anyString())).thenReturn(Boolean.TRUE);

        val ticketRegistry = new DefaultTicketRegistry();
        val strategy = getSingleSignOnStrategy(svc, ticketRegistry);

        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService("serviceid1"));
        val authentication = CoreAuthenticationTestUtils.getAuthentication(
            Map.of(ClientCredential.AUTHENTICATION_ATTRIBUTE_CLIENT_NAME, List.of("CAS")));

        val tgt = new MockTicketGrantingTicket(authentication);
        ticketRegistry.addTicket(tgt);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        assertTrue(strategy.supports(context));
        assertFalse(strategy.isParticipating(context));
    }

    @Test
    public void verifySsoWithMissingClientAndExclusive() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val svc = CoreAuthenticationTestUtils.getRegisteredService("serviceid1");
        val policy = new DefaultRegisteredServiceAccessStrategy();
        policy.setDelegatedAuthenticationPolicy(
            new DefaultRegisteredServiceDelegatedAuthenticationPolicy()
                .setExclusive(true)
                .setAllowedProviders(List.of("CAS")));
        when(svc.getAccessStrategy()).thenReturn(policy);
        when(svc.matches(anyString())).thenReturn(Boolean.TRUE);

        val ticketRegistry = new DefaultTicketRegistry();
        val strategy = getSingleSignOnStrategy(svc, ticketRegistry);

        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService("serviceid1"));
        val authentication = CoreAuthenticationTestUtils.getAuthentication(Map.of());

        val tgt = new MockTicketGrantingTicket(authentication);
        ticketRegistry.addTicket(tgt);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        assertTrue(strategy.supports(context));
        assertFalse(strategy.isParticipating(context));
    }


    private static SingleSignOnParticipationStrategy getSingleSignOnStrategy(
        final RegisteredService svc,
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

        val strategy = new DelegatedAuthenticationSingleSignOnParticipationStrategy(servicesManager,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()),
            new DefaultTicketRegistrySupport(ticketRegistry));
        return strategy;
    }

}
