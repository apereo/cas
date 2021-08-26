package org.apereo.cas.web.flow.resolver.impl;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ServiceTicketRequestWebflowEventResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowEvents")
public class ServiceTicketRequestWebflowEventResolverTests extends BaseCasWebflowMultifactorAuthenticationTests {
    @Autowired
    @Qualifier("serviceTicketRequestWebflowEventResolver")
    private CasWebflowEventResolver serviceTicketRequestWebflowEventResolver;

    @BeforeEach
    public void beforeEach() {
        servicesManager.deleteAll();
    }

    @Test
    public void verifyAttemptWithoutCredential() {
        val context = new MockRequestContext();

        val request = new MockHttpServletRequest();

        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);

        val service = RegisteredServiceTestUtils.getService("service-ticket-request");
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of());
        servicesManager.save(registeredService);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        WebUtils.putServiceIntoFlowScope(context, service);
        val event = serviceTicketRequestWebflowEventResolver.resolveSingle(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_GENERATE_SERVICE_TICKET, event.getId());
    }

    @Test
    public void verifyServiceTicketRequestSkipped() {
        val context = new MockRequestContext();

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_RENEW, "true");

        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val tgt = new MockTicketGrantingTicket("casuser");

        val service = RegisteredServiceTestUtils.getService("service-ticket-request");
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId());
        servicesManager.save(registeredService);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        WebUtils.putServiceIntoFlowScope(context, service);
        val event = serviceTicketRequestWebflowEventResolver.resolveSingle(context);
        assertNull(event);
    }

    @Test
    public void verifyServiceTicketRequestCreated() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);

        val service = RegisteredServiceTestUtils.getService("service-ticket-request");
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId());
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(true, true));
        servicesManager.save(registeredService);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        WebUtils.putServiceIntoFlowScope(context, service);
        WebUtils.putCredential(context, RegisteredServiceTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon"));
        val event = serviceTicketRequestWebflowEventResolver.resolveSingle(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_GENERATE_SERVICE_TICKET, event.getId());
    }

    @Test
    public void verifyServiceTicketRequestPrincipalMismatch() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val tgt = new MockTicketGrantingTicket("randomuser");
        ticketRegistry.addTicket(tgt);

        val service = RegisteredServiceTestUtils.getService("service-ticket-request");
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId());
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(true, true));
        servicesManager.save(registeredService);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        WebUtils.putServiceIntoFlowScope(context, service);
        WebUtils.putCredential(context, RegisteredServiceTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon"));
        assertNull(serviceTicketRequestWebflowEventResolver.resolveSingle(context));
    }

    @Test
    public void verifyServiceTicketRequestFailsAuthN() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);

        val service = RegisteredServiceTestUtils.getService("service-ticket-request");
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId());
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(true, true));
        servicesManager.save(registeredService);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        WebUtils.putServiceIntoFlowScope(context, service);
        WebUtils.putCredential(context, RegisteredServiceTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "badP@ass"));
        val event = serviceTicketRequestWebflowEventResolver.resolveSingle(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, event.getId());
    }

    @Test
    public void verifyServiceTicketRequestWithRenew() {
        val context = new MockRequestContext();

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_RENEW, "true");

        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);

        val service = RegisteredServiceTestUtils.getService("service-ticket-request");
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId());
        servicesManager.save(registeredService);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        WebUtils.putServiceIntoFlowScope(context, service);
        val event = serviceTicketRequestWebflowEventResolver.resolveSingle(context);
        assertNull(event);

    }

}
