/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.remoting.server;

import java.net.MalformedURLException;
import java.net.URL;

import org.jasig.cas.CentralAuthenticationServiceImpl;
import org.jasig.cas.authentication.AuthenticationManagerImpl;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.handler.support.HttpBasedServiceCredentialsAuthenticationHandler;
import org.jasig.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.HttpBasedServiceCredentials;
import org.jasig.cas.authentication.principal.HttpBasedServiceCredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.ticket.registry.DefaultTicketRegistry;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.jasig.cas.validation.UsernamePasswordCredentialsValidator;
import org.springframework.validation.Validator;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class RemoteCentralAuthenticationServiceTests extends TestCase {

    private CentralAuthenticationServiceImpl centralAuthenticationService;

    private RemoteCentralAuthenticationService remoteCentralAuthenticationService;

    protected void setUp() throws Exception {
        super.setUp();
        this.centralAuthenticationService = new CentralAuthenticationServiceImpl();
        this.centralAuthenticationService
            .setServiceTicketExpirationPolicy(new NeverExpiresExpirationPolicy());
        this.centralAuthenticationService
            .setTicketGrantingTicketExpirationPolicy(new NeverExpiresExpirationPolicy());
        this.centralAuthenticationService
            .setTicketRegistry(new DefaultTicketRegistry());
        this.centralAuthenticationService
            .setTicketGrantingTicketUniqueTicketIdGenerator(new DefaultUniqueTicketIdGenerator());
        this.centralAuthenticationService.setServiceTicketUniqueTicketIdGenerator(new DefaultUniqueTicketIdGenerator());

        AuthenticationManagerImpl manager = new AuthenticationManagerImpl();

        CredentialsToPrincipalResolver[] resolvers = new CredentialsToPrincipalResolver[] {new UsernamePasswordCredentialsToPrincipalResolver(), new HttpBasedServiceCredentialsToPrincipalResolver()};
        AuthenticationHandler[] handlers = new AuthenticationHandler[] {new SimpleTestUsernamePasswordAuthenticationHandler(), new HttpBasedServiceCredentialsAuthenticationHandler()};

        manager.setAuthenticationHandlers(handlers);
        manager.setCredentialsToPrincipalResolvers(resolvers);
        manager.afterPropertiesSet();

        this.centralAuthenticationService.setAuthenticationManager(manager);

        this.remoteCentralAuthenticationService = new RemoteCentralAuthenticationService();

        this.remoteCentralAuthenticationService
            .setCentralAuthenticationService(this.centralAuthenticationService);

        Validator[] validators = new Validator[1];
        validators[0] = new UsernamePasswordCredentialsValidator();

        this.centralAuthenticationService.afterPropertiesSet();
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
            this.remoteCentralAuthenticationService.createTicketGrantingTicket(c);
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
