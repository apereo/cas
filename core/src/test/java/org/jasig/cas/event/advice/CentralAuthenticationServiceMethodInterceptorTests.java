/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.event.advice;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
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
public class CentralAuthenticationServiceMethodInterceptorTests extends
    AbstractCentralAuthenticationServiceTest {

    private CentralAuthenticationServiceMethodInterceptor advice = new CentralAuthenticationServiceMethodInterceptor();

    TicketEvent event;

    public CentralAuthenticationServiceMethodInterceptorTests() {
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

        this.advice.invoke(new MockMethodInvocation(new Object[] {TestUtils
            .getCredentialsWithSameUsernameAndPassword()}, method, ticketId));

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

        this.advice.invoke(new MockMethodInvocation(new Object[] {ticketId}, method, null));

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

        this.advice.invoke(new MockMethodInvocation(new Object[] {serviceTicketId,
            TestUtils.getCredentialsWithSameUsernameAndPassword()}, method, ticketGrantingTicketId));

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

        this.advice.invoke(new MockMethodInvocation(new Object[] {
            ticketId, new SimpleService("test")}, method, serviceTicketId));

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

        this.advice.invoke(new MockMethodInvocation(new Object[] {
            serviceTicketId, new SimpleService("test")}, method, assertion));

        assertNotNull(this.event);
        assertEquals(TicketEvent.VALIDATE_SERVICE_TICKET, this.event
            .getTicketEventType());
    }

    public void testInvalidMethod() throws Throwable {
        Method method = AuthenticationHandler.class.getDeclaredMethods()[0];
        this.advice.invoke(new MockMethodInvocation(new Object[] {}, method, null));

        assertNull(this.event);
    }

    protected class MockApplicationEventPublisher implements
        ApplicationEventPublisher {

        public void publishEvent(ApplicationEvent arg0) {
            CentralAuthenticationServiceMethodInterceptorTests.this.event = (TicketEvent) arg0;
        }
    }
    
    protected class MockMethodInvocation implements MethodInvocation {
        private Object[] arguments;
        
        private Method method;
        
        private Object returnValue;
        
        protected MockMethodInvocation(final Object[] arguments, final Method method, final Object returnValue) {
            this.arguments = arguments;
            this.method = method;
            this.returnValue = returnValue;
        }

        public Method getMethod() {
            return method;
        }

        public Object[] getArguments() {
            return arguments;
        }

        public AccessibleObject getStaticPart() {
            return null;
        }

        public Object getThis() {
            return null;
        }

        public Object proceed() throws Throwable {
            return returnValue;
        }
        
        
    }
}
