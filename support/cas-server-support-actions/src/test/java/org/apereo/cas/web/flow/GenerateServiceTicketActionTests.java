package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DenyAllAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ReturnMappedAttributeReleasePolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.execution.Action;
import jakarta.servlet.http.Cookie;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("WebflowServiceActions")
class GenerateServiceTicketActionTests extends AbstractWebflowActionsTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_GENERATE_SERVICE_TICKET)
    private Action action;
    
    private Ticket ticketGrantingTicket;

    private Service service;

    @BeforeEach
    void onSetUp() throws Throwable {
        this.service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of());
        getServicesManager().save(registeredService);

        val authnResult = getAuthenticationSystemSupport().finalizeAuthenticationTransaction(service,
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
        this.ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(authnResult);
        getTicketRegistry().addTicket(this.ticketGrantingTicket);
    }

    @Test
    void verifyServiceTicketFromCookie() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.getFlowScope().put(CasWebflowConstants.ATTRIBUTE_SERVICE, service);
        context.getFlowScope().put(WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID, ticketGrantingTicket.getId());
        context.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
        context.setHttpRequestCookies(new Cookie("TGT", ticketGrantingTicket.getId()));
        action.execute(context);
        assertNotNull(WebUtils.getServiceTicketFromRequestScope(context));
    }

    @Test
    void verifyTicketGrantingTicketFromRequest() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.getFlowScope().put(CasWebflowConstants.ATTRIBUTE_SERVICE, service);
        context.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
        WebUtils.putTicketGrantingTicketInScopes(context, ticketGrantingTicket);

        action.execute(context);
        assertNotNull(WebUtils.getServiceTicketFromRequestScope(context));
    }

    @Test
    void verifyServiceTicketWithAccessStrategyMapped() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val serviceId = UUID.randomUUID().toString();
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(serviceId, Map.of("Role", Set.of(".*developer.*")));
        registeredService.setAttributeReleasePolicy(new ReturnMappedAttributeReleasePolicy()
            .setAllowedAttributes(Map.of("Role", "groovy { return attributes['eduPersonAffiliation'].get(0) }")));
        getServicesManager().save(registeredService);
        context.getFlowScope().put(CasWebflowConstants.ATTRIBUTE_SERVICE, RegisteredServiceTestUtils.getService(serviceId));
        context.setParameter(CasProtocolConstants.PARAMETER_SERVICE, serviceId);
        WebUtils.putTicketGrantingTicketInScopes(context, ticketGrantingTicket);

        action.execute(context);
        assertNotNull(WebUtils.getServiceTicketFromRequestScope(context));
    }

    @Test
    void verifyServiceTicketWithAccessStrategyDenied() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val serviceId = UUID.randomUUID().toString();

        val registeredService = RegisteredServiceTestUtils.getRegisteredService(serviceId, Map.of("eduPersonAffiliation", Set.of(".*developer.*")));
        registeredService.setAttributeReleasePolicy(new DenyAllAttributeReleasePolicy());
        getServicesManager().save(registeredService);

        context.getFlowScope().put(CasWebflowConstants.ATTRIBUTE_SERVICE, RegisteredServiceTestUtils.getService(serviceId));
        context.setParameter(CasProtocolConstants.PARAMETER_SERVICE, serviceId);
        WebUtils.putTicketGrantingTicketInScopes(context, ticketGrantingTicket);

        action.execute(context);
        assertNotNull(WebUtils.getServiceTicketFromRequestScope(context));
    }

    @Test
    void verifyServiceTicketWithAccessStrategyMultivalued() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val serviceId = UUID.randomUUID().toString();

        val registeredService = RegisteredServiceTestUtils.getRegisteredService(serviceId,
            Map.of("eduPersonAffiliation", Set.of(".*developer.*")));
        registeredService.setAttributeReleasePolicy(new ReturnMappedAttributeReleasePolicy()
            .setAllowedAttributes(Map.of("eduPersonAffiliation", "groovy { return 'engineers' }")));

        getServicesManager().save(registeredService);

        context.getFlowScope().put(CasWebflowConstants.ATTRIBUTE_SERVICE, RegisteredServiceTestUtils.getService(serviceId));
        context.setParameter(CasProtocolConstants.PARAMETER_SERVICE, serviceId);
        WebUtils.putTicketGrantingTicketInScopes(context, ticketGrantingTicket);

        action.execute(context);
        assertNotNull(WebUtils.getServiceTicketFromRequestScope(context));
    }

    @Test
    void verifyTicketGrantingTicketNoTgt() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.getFlowScope().put(CasWebflowConstants.ATTRIBUTE_SERVICE, service);
        context.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
        val tgt = mock(TicketGrantingTicket.class);
        when(tgt.getId()).thenReturn("bleh");
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, action.execute(context).getId());
    }

    @Test
    void verifyTicketGrantingTicketExpiredTgt() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.getFlowScope().put(CasWebflowConstants.ATTRIBUTE_SERVICE, service);
        context.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
        WebUtils.putTicketGrantingTicketInScopes(context, ticketGrantingTicket);
        ticketGrantingTicket.markTicketExpired();
        getTicketRegistry().updateTicket(ticketGrantingTicket);
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, action.execute(context).getId());
    }

    @Test
    void verifyTicketGrantingTicketNotTgtButGateway() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.getFlowScope().put(CasWebflowConstants.ATTRIBUTE_SERVICE, service);
        context.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
        context.setParameter(CasProtocolConstants.PARAMETER_GATEWAY, "true");
        val tgt = mock(TicketGrantingTicket.class);
        when(tgt.getId()).thenReturn("bleh");
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        assertEquals(CasWebflowConstants.TRANSITION_ID_GATEWAY, action.execute(context).getId());
    }

    @Test
    void verifyWarnCookie() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val randomService = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
        context.getFlowScope().put(CasWebflowConstants.ATTRIBUTE_SERVICE, randomService);
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(randomService.getId());
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy()
            .setUnauthorizedRedirectUrl(new URI("https://github.com")));
        getServicesManager().save(registeredService);
        WebUtils.putWarningCookie(context, Boolean.TRUE);
        WebUtils.putTicketGrantingTicketInScopes(context, ticketGrantingTicket);
        assertEquals(CasWebflowConstants.STATE_ID_WARN, action.execute(context).getId());
    }
}
