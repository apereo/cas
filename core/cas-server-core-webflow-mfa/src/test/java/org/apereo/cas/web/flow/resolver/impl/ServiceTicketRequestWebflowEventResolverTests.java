package org.apereo.cas.web.flow.resolver.impl;

import module java.base;
import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ServiceTicketRequestWebflowEventResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowEvents")
class ServiceTicketRequestWebflowEventResolverTests extends BaseCasWebflowMultifactorAuthenticationTests {
    @Autowired
    @Qualifier(CasWebflowEventResolver.BEAN_NAME_SERVICE_TICKET_EVENT_RESOLVER)
    private CasWebflowEventResolver serviceTicketRequestWebflowEventResolver;
    
    @Test
    void verifyAttemptWithoutCredential() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

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
    void verifyServiceTicketRequestSkipped() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setParameter(CasProtocolConstants.PARAMETER_RENEW, "true");
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
    void verifyServiceTicketRequestCreated() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
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
    void verifyServiceTicketRequestPrincipalMismatch() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
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
    void verifyServiceTicketRequestFailsAuthN() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
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
    void verifyServiceTicketRequestWithRenew() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setParameter(CasProtocolConstants.PARAMETER_RENEW, "true");
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
