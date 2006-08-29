/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.services.advice;

import java.net.MalformedURLException;
import java.net.URL;

import org.jasig.cas.AbstractCentralAuthenticationServiceTest;
import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.services.DefaultServiceRegistry;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServiceRegistry;
import org.jasig.cas.services.ServiceRegistryManager;
import org.jasig.cas.services.UnauthorizedServiceException;
import org.jasig.cas.ticket.TicketException;

public class ServiceAllowedToProxyMethodBeforeAdviceTests extends
    AbstractCentralAuthenticationServiceTest {

    private ServiceAllowedToProxyMethodBeforeAdvice advice;

    private ServiceRegistry serviceRegistry;

    protected void onSetUp() throws Exception {
        this.serviceRegistry = new DefaultServiceRegistry();
        this.advice = new ServiceAllowedToProxyMethodBeforeAdvice();
        this.advice.setTicketRegistry(getTicketRegistry());
        this.advice.setServiceRegistry(this.serviceRegistry);
        this.advice.afterPropertiesSet();

        RegisteredService r = new RegisteredService("test", true, false, null,
            new URL("http://www.rutgers.edu"));

        ((ServiceRegistryManager) this.serviceRegistry).addService(r);
    }

    public void testAfterPropertiesSetNoTicketRegistry() {
        try {
            this.advice.setTicketRegistry(null);
            this.advice.afterPropertiesSet();
            fail("Exception expected.");
        } catch (Exception e) {
            return;
        }
    }

    public void testAfterPropertiesSetNoServiceRegistry() {
        try {
            this.advice.setServiceRegistry(null);
            this.advice.afterPropertiesSet();
            fail("Exception expected.");
        } catch (Exception e) {
            return;
        }
    }

    public void testServiceNotFound() throws TicketException {
        callAdvice();
    }

    public void testServiceFoundById() throws TicketException,
        UnauthorizedServiceException {
        String ticketGrantingTicketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        String serviceTicketId = getCentralAuthenticationService()
            .grantServiceTicket(ticketGrantingTicketId,
                new SimpleService("test"));
        this.advice.before(null, new Object[] {serviceTicketId,
            TestUtils.getCredentialsWithSameUsernameAndPassword()}, null);
    }

    public void testServiceFoundByUrl() throws TicketException,
        UnauthorizedServiceException {
        String ticketGrantingTicketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        String serviceTicketId = getCentralAuthenticationService()
            .grantServiceTicket(ticketGrantingTicketId,
                new SimpleService("http://www.rutgers.edu"));
        this.advice.before(null, new Object[] {serviceTicketId,
            TestUtils.getCredentialsWithSameUsernameAndPassword()}, null);
    }

    public void testServiceNoProxying() throws TicketException,
        MalformedURLException {

        RegisteredService r = new RegisteredService("test2", false, false,
            null, new URL("http://www.rutgers.com"));
        ((ServiceRegistryManager) this.serviceRegistry).addService(r);

        callAdvice();
    }

    private void callAdvice() throws TicketException {
        String ticketGrantingTicketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        String serviceTicketId = getCentralAuthenticationService()
            .grantServiceTicket(ticketGrantingTicketId,
                new SimpleService("test2"));
        try {
            this.advice.before(null, new Object[] {serviceTicketId,
                TestUtils.getCredentialsWithSameUsernameAndPassword()}, null);
            fail("Exception expected.");
        } catch (UnauthorizedServiceException e) {
            return;
        }
    }
}
