/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.remoting.server;

import java.net.MalformedURLException;
import java.net.URL;

import org.jasig.cas.AbstractCentralAuthenticationServiceTest;
import org.jasig.cas.authentication.principal.HttpBasedServiceCredentials;
import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.validation.UsernamePasswordCredentialsValidator;
import org.springframework.validation.Validator;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class RemoteCentralAuthenticationServiceTests extends
    AbstractCentralAuthenticationServiceTest {

    private RemoteCentralAuthenticationService remoteCentralAuthenticationService;

    protected void onSetUp() throws Exception {
        this.remoteCentralAuthenticationService = new RemoteCentralAuthenticationService();

        this.remoteCentralAuthenticationService
            .setCentralAuthenticationService(getCentralAuthenticationService());

        Validator[] validators = new Validator[1];
        validators[0] = new UsernamePasswordCredentialsValidator();

        this.remoteCentralAuthenticationService.setValidators(validators);
        this.remoteCentralAuthenticationService.afterPropertiesSet();
    }

    public void testAfterPropertiesSet() {
        RemoteCentralAuthenticationService c = new RemoteCentralAuthenticationService();

        try {
            c.afterPropertiesSet();
            fail("Exception expected.");
        } catch (Exception e) {
            return;
        }
    }

    public void testInvalidCredentials() throws TicketException {
        try {
            this.remoteCentralAuthenticationService
                .createTicketGrantingTicket(new UsernamePasswordCredentials());
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            return;
        }
    }

    public void testNullCredentials() throws TicketException {
        try {
            this.remoteCentralAuthenticationService
                .createTicketGrantingTicket(null);
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            return;
        }
    }

    public void testValidCredentials() throws TicketException {
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setUsername("test");
        c.setPassword("test");
        this.remoteCentralAuthenticationService.createTicketGrantingTicket(c);
    }

    public void testDontUseValidatorsToCheckValidCredentials() {
        try {
            UsernamePasswordCredentials c = new UsernamePasswordCredentials();
            this.remoteCentralAuthenticationService.setValidators(null);
            this.remoteCentralAuthenticationService
                .createTicketGrantingTicket(c);
            fail("TicketException expected.");
        } catch (TicketException e) {
            return;
        }
    }

    public void testDestroyTicketGrantingTicket() {
        this.remoteCentralAuthenticationService
            .destroyTicketGrantingTicket("test");
    }

    public void testGrantServiceTicketWithValidTicketGrantingTicket()
        throws TicketException {
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setUsername("test");
        c.setPassword("test");

        final String ticketId = this.remoteCentralAuthenticationService
            .createTicketGrantingTicket(c);
        this.remoteCentralAuthenticationService.grantServiceTicket(ticketId,
            new SimpleService("test"));
    }

    public void testGrantServiceTicketWithValidCredentials()
        throws TicketException {
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setUsername("test");
        c.setPassword("test");
        final String ticketGrantingTicketId = this.remoteCentralAuthenticationService
            .createTicketGrantingTicket(c);
        this.remoteCentralAuthenticationService.grantServiceTicket(
            ticketGrantingTicketId, new SimpleService("Test"), c);
    }

    public void testGrantServiceTicketWithNullCredentials()
        throws TicketException {
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setUsername("test");
        c.setPassword("test");
        final String ticketGrantingTicketId = this.remoteCentralAuthenticationService
            .createTicketGrantingTicket(c);
        this.remoteCentralAuthenticationService.grantServiceTicket(
            ticketGrantingTicketId, new SimpleService("Test"), null);
    }

    public void testGrantServiceTicketWithEmptyCredentials()
        throws TicketException {
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setUsername("test");
        c.setPassword("test");
        final String ticketGrantingTicketId = this.remoteCentralAuthenticationService
            .createTicketGrantingTicket(c);
        try {
            this.remoteCentralAuthenticationService.grantServiceTicket(
                ticketGrantingTicketId, new SimpleService("Test"),
                new UsernamePasswordCredentials());
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            return;
        }
    }

    public void testValidateServiceTicketWithValidService()
        throws TicketException {
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setUsername("test");
        c.setPassword("test");
        final String ticketGrantingTicket = this.remoteCentralAuthenticationService
            .createTicketGrantingTicket(c);
        final String serviceTicket = this.remoteCentralAuthenticationService
            .grantServiceTicket(ticketGrantingTicket, new SimpleService("test"));

        this.remoteCentralAuthenticationService.validateServiceTicket(
            serviceTicket, new SimpleService("test"));
    }

    public void testDelegateTicketGrantingTicketWithValidCredentials()
        throws TicketException, MalformedURLException {
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setUsername("test");
        c.setPassword("test");
        final String ticketGrantingTicket = this.remoteCentralAuthenticationService
            .createTicketGrantingTicket(c);
        final String serviceTicket = this.remoteCentralAuthenticationService
            .grantServiceTicket(ticketGrantingTicket, new SimpleService("test"));
        this.remoteCentralAuthenticationService.delegateTicketGrantingTicket(
            serviceTicket, new HttpBasedServiceCredentials(new URL(
                "https://www.acs.rutgers.edu")));
    }

    public void testDelegateTicketGrantingTicketWithInvalidCredentials()
        throws TicketException {
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setUsername("test");
        c.setPassword("test");
        final String ticketGrantingTicket = this.remoteCentralAuthenticationService
            .createTicketGrantingTicket(c);
        final String serviceTicket = this.remoteCentralAuthenticationService
            .grantServiceTicket(ticketGrantingTicket, new SimpleService("test"));
        try {
            this.remoteCentralAuthenticationService
                .delegateTicketGrantingTicket(serviceTicket,
                    new UsernamePasswordCredentials());
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            return;
        }

    }
}
