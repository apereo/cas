package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DenyAllAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ReturnMappedAttributeReleasePolicy;
import org.apereo.cas.ticket.TicketGrantingTicket;
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
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import javax.servlet.http.Cookie;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("WebflowActions")
public class GenerateServiceTicketActionTests extends AbstractWebflowActionsTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_GENERATE_SERVICE_TICKET)
    private Action action;

    private TicketGrantingTicket ticketGrantingTicket;

    private Service service;

    @BeforeEach
    public void onSetUp() {
        getServicesManager().deleteAll();
        this.service = RegisteredServiceTestUtils.getService();
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of());
        getServicesManager().save(registeredService);

        val authnResult = getAuthenticationSystemSupport().finalizeAuthenticationTransaction(service,
                CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
        this.ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(authnResult);
        getTicketRegistry().addTicket(this.ticketGrantingTicket);
    }

    @Test
    public void verifyServiceTicketFromCookie() throws Exception {
        val context = new MockRequestContext();
        context.getFlowScope().put(CasWebflowConstants.ATTRIBUTE_SERVICE, service);
        context.getFlowScope().put(WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID, this.ticketGrantingTicket.getId());
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(
            new MockServletContext(), request, new MockHttpServletResponse()));
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
        request.setCookies(new Cookie("TGT", this.ticketGrantingTicket.getId()));
        this.action.execute(context);
        assertNotNull(WebUtils.getServiceTicketFromRequestScope(context));
    }

    @Test
    public void verifyTicketGrantingTicketFromRequest() throws Exception {
        val context = new MockRequestContext();
        context.getFlowScope().put(CasWebflowConstants.ATTRIBUTE_SERVICE, service);
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
        WebUtils.putTicketGrantingTicketInScopes(context, this.ticketGrantingTicket);

        this.action.execute(context);
        assertNotNull(WebUtils.getServiceTicketFromRequestScope(context));
    }

    @Test
    public void verifyServiceTicketWithAccessStrategyMapped() throws Exception {
        val context = new MockRequestContext();
        val serviceId = UUID.randomUUID().toString();
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(serviceId, Map.of("Role", Set.of(".*developer.*")));
        registeredService.setAttributeReleasePolicy(new ReturnMappedAttributeReleasePolicy(
            Map.of("Role", "groovy { return attributes['eduPersonAffiliation'].get(0) }")));
        getServicesManager().save(registeredService);

        context.getFlowScope().put(CasWebflowConstants.ATTRIBUTE_SERVICE, RegisteredServiceTestUtils.getService(serviceId));
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, serviceId);
        WebUtils.putTicketGrantingTicketInScopes(context, this.ticketGrantingTicket);

        this.action.execute(context);
        assertNotNull(WebUtils.getServiceTicketFromRequestScope(context));
    }

    @Test
    public void verifyServiceTicketWithAccessStrategyDenied() throws Exception {
        val context = new MockRequestContext();
        val serviceId = UUID.randomUUID().toString();

        val registeredService = RegisteredServiceTestUtils.getRegisteredService(serviceId, Map.of("eduPersonAffiliation", Set.of(".*developer.*")));
        registeredService.setAttributeReleasePolicy(new DenyAllAttributeReleasePolicy());
        getServicesManager().save(registeredService);

        context.getFlowScope().put(CasWebflowConstants.ATTRIBUTE_SERVICE, RegisteredServiceTestUtils.getService(serviceId));
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, serviceId);
        WebUtils.putTicketGrantingTicketInScopes(context, this.ticketGrantingTicket);

        this.action.execute(context);
        assertNotNull(WebUtils.getServiceTicketFromRequestScope(context));
    }

    @Test
    public void verifyServiceTicketWithAccessStrategyMultivalued() throws Exception {
        val context = new MockRequestContext();
        val serviceId = UUID.randomUUID().toString();

        val registeredService = RegisteredServiceTestUtils.getRegisteredService(serviceId, Map.of("eduPersonAffiliation", Set.of(".*developer.*")));
        registeredService.setAttributeReleasePolicy(new ReturnMappedAttributeReleasePolicy(
            Map.of("eduPersonAffiliation", "groovy { return 'engineers' }")));

        getServicesManager().save(registeredService);

        context.getFlowScope().put(CasWebflowConstants.ATTRIBUTE_SERVICE, RegisteredServiceTestUtils.getService(serviceId));
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, serviceId);
        WebUtils.putTicketGrantingTicketInScopes(context, this.ticketGrantingTicket);

        this.action.execute(context);
        assertNotNull(WebUtils.getServiceTicketFromRequestScope(context));
    }

    @Test
    public void verifyTicketGrantingTicketNoTgt() throws Exception {
        val context = new MockRequestContext();
        context.getFlowScope().put(CasWebflowConstants.ATTRIBUTE_SERVICE, service);

        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());

        val tgt = mock(TicketGrantingTicket.class);
        when(tgt.getId()).thenReturn("bleh");
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);

        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, this.action.execute(context).getId());
    }

    @Test
    public void verifyTicketGrantingTicketExpiredTgt() throws Exception {
        val context = new MockRequestContext();
        context.getFlowScope().put(CasWebflowConstants.ATTRIBUTE_SERVICE, service);
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
        WebUtils.putTicketGrantingTicketInScopes(context, this.ticketGrantingTicket);
        this.ticketGrantingTicket.markTicketExpired();
        getTicketRegistry().updateTicket(this.ticketGrantingTicket);
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, this.action.execute(context).getId());
    }

    @Test
    public void verifyTicketGrantingTicketNotTgtButGateway() throws Exception {
        val context = new MockRequestContext();
        context.getFlowScope().put(CasWebflowConstants.ATTRIBUTE_SERVICE, service);
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_GATEWAY, "true");
        val tgt = mock(TicketGrantingTicket.class);
        when(tgt.getId()).thenReturn("bleh");
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        assertEquals(CasWebflowConstants.TRANSITION_ID_GATEWAY, this.action.execute(context).getId());
    }

    @Test
    public void verifyWarnCookie() throws Exception {
        val context = new MockRequestContext();
        val randomService = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
        context.getFlowScope().put(CasWebflowConstants.ATTRIBUTE_SERVICE, randomService);
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(randomService.getId());
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy()
            .setUnauthorizedRedirectUrl(new URI("https://github.com")));
        getServicesManager().save(registeredService);

        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        WebUtils.putWarningCookie(context, Boolean.TRUE);
        WebUtils.putTicketGrantingTicketInScopes(context, this.ticketGrantingTicket);
        assertEquals(CasWebflowConstants.STATE_ID_WARN, this.action.execute(context).getId());
    }
}
