package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.login.GenericSuccessViewAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link GenericSuccessViewAction}
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Tag("WebflowActions")
@TestPropertySource(properties = "cas.view.authorized-services-on-successful-login=true")
class GenericSuccessViewActionTests extends AbstractWebflowActionsTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_GENERIC_SUCCESS_VIEW)
    private Action genericSuccessViewAction;

    @BeforeEach
    public void setup() {
        casProperties.getView().setDefaultRedirectUrl(null);
        getServicesManager().deleteAll();
    }

    @Test
    void verifyAuthzServices() throws Throwable {
        val registeredService1 = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString(), Map.of());
        getServicesManager().save(registeredService1);

        val registeredService2 = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        registeredService2.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(false, false));
        getServicesManager().save(registeredService2);

        val context = new MockRequestContext();
        val tgt = new MockTicketGrantingTicket("casuser");
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        getTicketRegistry().addTicket(tgt);

        context.setExternalContext(new MockExternalContext());
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val result = genericSuccessViewAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
        assertNotNull(WebUtils.getAuthorizedServices(context));
        val list = WebUtils.getAuthorizedServices(context);
        assertEquals(1, list.size());
    }

    @Test
    void verifyRedirect() throws Throwable {
        val servicesManager = mock(ServicesManager.class);
        val serviceFactory = mock(ServiceFactory.class);

        val service = RegisteredServiceTestUtils.getService("https://github.com/apereo/cas");
        when(serviceFactory.createService(anyString())).thenReturn(service);
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        when(servicesManager.findServiceBy(any(Service.class))).thenReturn(registeredService);

        casProperties.getView().setDefaultRedirectUrl(service.getId());
        val action = new GenericSuccessViewAction(getTicketRegistry(), servicesManager, serviceFactory, casProperties);
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
    void verifyAuthn() throws Throwable {
        val servicesManager = mock(ServicesManager.class);
        val serviceFactory = mock(ServiceFactory.class);

        val service = RegisteredServiceTestUtils.getService("https://github.com/apereo/cas");
        when(serviceFactory.createService(anyString())).thenReturn(service);
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        when(servicesManager.findServiceBy(any(Service.class))).thenReturn(registeredService);

        val action = new GenericSuccessViewAction(getTicketRegistry(), servicesManager, serviceFactory, casProperties);
        val context = new MockRequestContext();
        context.setExternalContext(new MockExternalContext());
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val tgt = new MockTicketGrantingTicket(CoreAuthenticationTestUtils.getAuthentication());
        getTicketRegistry().addTicket(tgt);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);

        val result = action.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
        assertNotNull(WebUtils.getAuthentication(context));
    }

    @Test
    void verifyValidPrincipal() throws Throwable {
        val mgr = mock(ServicesManager.class);
        val factory = mock(ServiceFactory.class);
        val tgt = new MockTicketGrantingTicket(CoreAuthenticationTestUtils.getAuthentication("cas"));
        getTicketRegistry().addTicket(tgt);
        val action = new GenericSuccessViewAction(getTicketRegistry(), mgr, factory, casProperties);
        val p = action.getAuthentication(tgt.getId());
        assertNotNull(p);
        assertTrue(p.isPresent());
        assertEquals("cas", p.get().getPrincipal().getId());
    }

    @Test
    void verifyPrincipalCanNotBeDetermined() throws Throwable {
        val mgr = mock(ServicesManager.class);
        val factory = mock(ServiceFactory.class);
        val action = new GenericSuccessViewAction(getTicketRegistry(), mgr, factory, casProperties);
        val p = action.getAuthentication("TGT-1");
        assertNotNull(p);
        assertFalse(p.isPresent());
    }
}
