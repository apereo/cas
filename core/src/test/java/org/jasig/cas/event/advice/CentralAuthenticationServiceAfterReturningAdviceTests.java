/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.event.advice;

import java.lang.reflect.Method;

import org.jasig.cas.AbstractCentralAuthenticationServiceTest;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.event.TicketEvent;
import org.jasig.cas.validation.Assertion;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class CentralAuthenticationServiceAfterReturningAdviceTests extends
    AbstractCentralAuthenticationServiceTest {

    private CentralAuthenticationServiceAfterReturningAdvice advice = new CentralAuthenticationServiceAfterReturningAdvice();

    TicketEvent event;

    public CentralAuthenticationServiceAfterReturningAdviceTests() {
        super();
        this.advice
            .setApplicationEventPublisher(new MockApplicationEventPublisher());
    }

    protected void onSetUp() throws Exception {
        this.event = null;
        this.advice.setTicketRegistry(getTicketRegistry());
    }

    public void testAfterPropertiesSet() throws Exception {
        this.advice.afterPropertiesSet();
    }

    public void testCreateTicketGrantingTicket() throws Throwable {
        final Method method = CentralAuthenticationService.class.getMethod(
            "createTicketGrantingTicket", new Class[] {Credentials.class});
        final String ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());

        this.advice.afterReturning(ticketId, method, new Object[] {TestUtils
            .getCredentialsWithSameUsernameAndPassword()}, null);

        assertNotNull(this.event);
        assertEquals(TicketEvent.CREATE_TICKET_GRANTING_TICKET, this.event
            .getTicketEventType());
    }

    public void testDestroyTicketGrantingTicket() throws Throwable {
        Method method = CentralAuthenticationService.class.getMethod(
            "destroyTicketGrantingTicket", new Class[] {String.class});
        String ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        getCentralAuthenticationService().destroyTicketGrantingTicket(ticketId);

        this.advice.afterReturning(null, method, new Object[] {ticketId}, null);

        assertNotNull(this.event);
        assertEquals(TicketEvent.DESTROY_TICKET_GRANTING_TICKET, this.event
            .getTicketEventType());
    }

    public void testDelegateTicketGrantingTicket() throws Throwable {
        Method method = CentralAuthenticationService.class.getMethod(
            "delegateTicketGrantingTicket", new Class[] {String.class,
                Credentials.class});
        String ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        String serviceTicketId = getCentralAuthenticationService()
            .grantServiceTicket(ticketId, new SimpleService("test"));
        String ticketGrantingTicketId = getCentralAuthenticationService()
            .delegateTicketGrantingTicket(serviceTicketId,
                TestUtils.getCredentialsWithSameUsernameAndPassword());

        this.advice.afterReturning(ticketGrantingTicketId, method,
            new Object[] {serviceTicketId,
                TestUtils.getCredentialsWithSameUsernameAndPassword()}, null);

        assertNotNull(this.event);
        assertEquals(TicketEvent.CREATE_TICKET_GRANTING_TICKET, this.event
            .getTicketEventType());
    }

    public void testGrantServiceTicket() throws Throwable {
        Method method = CentralAuthenticationService.class.getMethod(
            "grantServiceTicket", new Class[] {String.class, Service.class});
        String ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        String serviceTicketId = getCentralAuthenticationService()
            .grantServiceTicket(ticketId, new SimpleService("test"));

        this.advice.afterReturning(serviceTicketId, method, new Object[] {
            ticketId, new SimpleService("test")}, null);

        assertNotNull(this.event);
        assertEquals(TicketEvent.CREATE_SERVCE_TICKET, this.event
            .getTicketEventType());
    }

    public void testValidateServiceTicket() throws Throwable {
        Method method = CentralAuthenticationService.class.getMethod(
            "validateServiceTicket", new Class[] {String.class, Service.class});
        String ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        String serviceTicketId = getCentralAuthenticationService()
            .grantServiceTicket(ticketId, new SimpleService("test"));
        Assertion assertion = getCentralAuthenticationService()
            .validateServiceTicket(serviceTicketId, new SimpleService("test"));

        this.advice.afterReturning(assertion, method, new Object[] {
            serviceTicketId, new SimpleService("test")}, null);

        assertNotNull(this.event);
        assertEquals(TicketEvent.VALIDATE_SERVICE_TICKET, this.event
            .getTicketEventType());
    }

    public void testInvalidMethod() throws Throwable {
        Method method = AuthenticationHandler.class.getDeclaredMethods()[0];
        this.advice.afterReturning(null, method, new Object[] {}, null);

        assertNull(this.event);
    }

    protected class MockApplicationEventPublisher implements
        ApplicationEventPublisher {

        public void publishEvent(ApplicationEvent arg0) {
            CentralAuthenticationServiceAfterReturningAdviceTests.this.event = (TicketEvent) arg0;
        }
    }
}
