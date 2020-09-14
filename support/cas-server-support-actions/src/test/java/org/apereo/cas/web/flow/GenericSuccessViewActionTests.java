package org.apereo.cas.web.flow;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.web.flow.login.GenericSuccessViewAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link GenericSuccessViewAction}
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Tag("WebflowActions")
public class GenericSuccessViewActionTests {
    @Test
    public void verifyRedirect() throws Exception {
        val cas = mock(CentralAuthenticationService.class);
        val servicesManager = mock(ServicesManager.class);
        val serviceFactory = mock(ServiceFactory.class);

        val service = RegisteredServiceTestUtils.getService("https://github.com/apereo/cas");
        when(serviceFactory.createService(anyString())).thenReturn(service);
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        when(servicesManager.findServiceBy(any(Service.class))).thenReturn(registeredService);

        val action = new GenericSuccessViewAction(cas, servicesManager, serviceFactory, service.getId());
        val context = new MockRequestContext();
        context.setExternalContext(new MockExternalContext());
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        val result = action.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
        assertTrue(context.getMockExternalContext().getExternalRedirectRequested());
        assertEquals(service.getId(), context.getMockExternalContext().getExternalRedirectUrl());
    }

    @Test
    public void verifyAuthn() throws Exception {
        val cas = mock(CentralAuthenticationService.class);
        val servicesManager = mock(ServicesManager.class);
        val serviceFactory = mock(ServiceFactory.class);

        val service = RegisteredServiceTestUtils.getService("https://github.com/apereo/cas");
        when(serviceFactory.createService(anyString())).thenReturn(service);
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        when(servicesManager.findServiceBy(any(Service.class))).thenReturn(registeredService);

        val action = new GenericSuccessViewAction(cas, servicesManager, serviceFactory, null);
        val context = new MockRequestContext();
        context.setExternalContext(new MockExternalContext());
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val tgt = new MockTicketGrantingTicket(CoreAuthenticationTestUtils.getAuthentication());
        when(cas.getTicket(any(String.class), any())).thenReturn(tgt);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        
        val result = action.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
        assertNotNull(WebUtils.getAuthentication(context));
    }

    @Test
    public void verifyValidPrincipal() throws InvalidTicketException {
        val cas = mock(CentralAuthenticationService.class);
        val mgr = mock(ServicesManager.class);
        val factory = mock(ServiceFactory.class);

        val authn = mock(Authentication.class);
        when(authn.getPrincipal()).thenReturn(CoreAuthenticationTestUtils.getPrincipal("cas"));
        val tgt = mock(TicketGrantingTicket.class);
        when(tgt.getAuthentication()).thenReturn(authn);

        when(cas.getTicket(any(String.class), any())).thenReturn(tgt);
        val action = new GenericSuccessViewAction(cas, mgr, factory, StringUtils.EMPTY);
        val p = action.getAuthentication("TGT-1");
        assertNotNull(p);
        assertTrue(p.isPresent());
        assertEquals("cas", p.get().getPrincipal().getId());
    }

    @Test
    public void verifyPrincipalCanNotBeDetermined() throws InvalidTicketException {
        val cas = mock(CentralAuthenticationService.class);
        val mgr = mock(ServicesManager.class);
        val factory = mock(ServiceFactory.class);
        when(cas.getTicket(any(String.class), any())).thenThrow(new InvalidTicketException("TGT-1"));
        val action = new GenericSuccessViewAction(cas, mgr, factory, StringUtils.EMPTY);
        val p = action.getAuthentication("TGT-1");
        assertNotNull(p);
        assertFalse(p.isPresent());
    }
}
