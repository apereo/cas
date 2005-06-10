/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas;

import java.net.MalformedURLException;
import java.net.URL;

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

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class CentralAuthenticationServiceImplTests extends TestCase {

    private CentralAuthenticationServiceImpl centralAuthenticationService;

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

        AuthenticationManagerImpl manager = new AuthenticationManagerImpl();

        CredentialsToPrincipalResolver[] resolvers = new CredentialsToPrincipalResolver[] {
            new UsernamePasswordCredentialsToPrincipalResolver(),
            new HttpBasedServiceCredentialsToPrincipalResolver()};
        AuthenticationHandler[] handlers = new AuthenticationHandler[] {
            new SimpleTestUsernamePasswordAuthenticationHandler(),
            new HttpBasedServiceCredentialsAuthenticationHandler()};

        manager.setAuthenticationHandlers(handlers);
        manager.setCredentialsToPrincipalResolvers(resolvers);
        manager.afterPropertiesSet();

        this.centralAuthenticationService.setAuthenticationManager(manager);
        this.centralAuthenticationService.setServiceTicketUniqueTicketIdGenerator(new DefaultUniqueTicketIdGenerator());
        this.centralAuthenticationService.afterPropertiesSet();
    }

    public void testNullCredentialsOnTicketGrantingTicketCreation() {
        try {
            this.centralAuthenticationService.createTicketGrantingTicket(null);
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            return;
        } catch (TicketException e) {
            fail("IllegalArgumentException expected.");
        }
    }

    public void testBadCredentialsOnTicketGrantingTicketCreation() {
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setUsername("test");
        c.setPassword("test1");

        try {
            this.centralAuthenticationService.createTicketGrantingTicket(c);
            fail("TicketException expected.");
        } catch (TicketException e) {
            return;
        }
    }

    public void testGoodCredentialsOnTicketGrantingTicketCreation() {
        try {
            assertNotNull(this.centralAuthenticationService
                .createTicketGrantingTicket(getUsernamePasswordCredentials()));
        } catch (TicketException e) {
            fail("TicketException not expected.");
        }
    }

    public void testDestroyTicketGrantingTicketWithNull() {
        try {
            this.centralAuthenticationService.destroyTicketGrantingTicket(null);
            fail("IllegalArgumentException exepcted.");
        } catch (IllegalArgumentException e) {
            return;
        }
    }

    public void testDestroyTicketGrantingTicketWithNonExistantTicket() {
        this.centralAuthenticationService.destroyTicketGrantingTicket("test");
    }

    public void testDestroyTicketGrantingTicketWithValidTicket()
        throws TicketException {
        final String ticketId = this.centralAuthenticationService
            .createTicketGrantingTicket(getUsernamePasswordCredentials());
        this.centralAuthenticationService.destroyTicketGrantingTicket(ticketId);
    }

    public void testDestroyTicketGrantingTicketWithInvalidTicket()
        throws TicketException {
        final String ticketId = this.centralAuthenticationService
            .createTicketGrantingTicket(getUsernamePasswordCredentials());
        final String serviceTicketId = this.centralAuthenticationService
            .grantServiceTicket(ticketId, new SimpleService("test"));
        try {
            this.centralAuthenticationService
                .destroyTicketGrantingTicket(serviceTicketId);
            fail("ClassCastException expected.");
        } catch (ClassCastException e) {
            return;
        }
    }

    public void testGrantServiceTicketWithValidTicketGrantingTicket()
        throws TicketException {
        final String ticketId = this.centralAuthenticationService
            .createTicketGrantingTicket(getUsernamePasswordCredentials());
        this.centralAuthenticationService.grantServiceTicket(ticketId,
            new SimpleService("test"));
    }

    public void testGrantServiceTicketWithInvalidTicketGrantingTicket()
        throws TicketException {
        final String ticketId = this.centralAuthenticationService
            .createTicketGrantingTicket(getUsernamePasswordCredentials());
        this.centralAuthenticationService.destroyTicketGrantingTicket(ticketId);
        try {
            this.centralAuthenticationService.grantServiceTicket(ticketId,
                new SimpleService("test"));
            fail("Expected exception to be thrown.");
        } catch (TicketException e) {
            return;
        }
    }

    public void testDelegateTicketGrantingTicketWithProperParams()
        throws TicketException {
        final String ticketId = this.centralAuthenticationService
            .createTicketGrantingTicket(getUsernamePasswordCredentials());
        final String serviceTicketId = this.centralAuthenticationService
            .grantServiceTicket(ticketId, new SimpleService("test"));
        this.centralAuthenticationService.delegateTicketGrantingTicket(
            serviceTicketId, getHttpBasedServiceCredentials());
    }

    public void testDelegateTicketGrantingTicketWithBadCredentials()
        throws TicketException {
        final String ticketId = this.centralAuthenticationService
            .createTicketGrantingTicket(getUsernamePasswordCredentials());
        final String serviceTicketId = this.centralAuthenticationService
            .grantServiceTicket(ticketId, new SimpleService("test"));
        try {
            this.centralAuthenticationService.delegateTicketGrantingTicket(
                serviceTicketId, getBadHttpBasedServiceCredentials());
            fail("TicketException expected.");
        } catch (TicketException e) {
            return;
        }
    }

    public void testDelegateTicketGrantingTicketWithBadServiceTicket()
        throws TicketException {
        final String ticketId = this.centralAuthenticationService
            .createTicketGrantingTicket(getUsernamePasswordCredentials());
        final String serviceTicketId = this.centralAuthenticationService
            .grantServiceTicket(ticketId, new SimpleService("test"));
        this.centralAuthenticationService.destroyTicketGrantingTicket(ticketId);
        try {
            this.centralAuthenticationService.delegateTicketGrantingTicket(
                serviceTicketId, getHttpBasedServiceCredentials());
            fail("TicketException expected.");
        } catch (TicketException e) {
            return;
        }
    }

    public void testDelegateTicketGrantingTicketWithNullParams()
        throws TicketException {
        try {
            this.centralAuthenticationService.delegateTicketGrantingTicket(
                null, null);
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            return;
        }
    }

    public void testGrantServiceTicketWithNullParams() throws TicketException {
        try {
            this.centralAuthenticationService.grantServiceTicket(null, null);
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            return;
        }
    }

    public void testGrantServiceTicketWithValidCredentials()
        throws TicketException {
        final String ticketGrantingTicket = this.centralAuthenticationService
            .createTicketGrantingTicket(getUsernamePasswordCredentials());
        this.centralAuthenticationService.grantServiceTicket(
            ticketGrantingTicket, new SimpleService("test"),
            getUsernamePasswordCredentials());
    }

    public void testGrantServiceTicketWithInvalidCredentials()
        throws TicketException {
        final String ticketGrantingTicket = this.centralAuthenticationService
            .createTicketGrantingTicket(getUsernamePasswordCredentials());
        try {
            this.centralAuthenticationService.grantServiceTicket(
                ticketGrantingTicket, new SimpleService("test"),
                getBadHttpBasedServiceCredentials());
            fail("Exception expected.");
        } catch (TicketException e) {
            return;
        }
    }

    public void testGrantServiceTicketWithDifferentCredentials()
        throws TicketException {
        final String ticketGrantingTicket = this.centralAuthenticationService
            .createTicketGrantingTicket(getUsernamePasswordCredentials());
        try {
            this.centralAuthenticationService.grantServiceTicket(
                ticketGrantingTicket, new SimpleService("test"),
                getUsernamePasswordCredentials2());
            fail("Exception expected.");
        } catch (TicketException e) {
            return;
        }
    }

    public void testValidateServiceTicketWithValidService()
        throws TicketException {
        final String ticketGrantingTicket = this.centralAuthenticationService
            .createTicketGrantingTicket(getUsernamePasswordCredentials());
        final String serviceTicket = this.centralAuthenticationService
            .grantServiceTicket(ticketGrantingTicket, new SimpleService("test"));

        this.centralAuthenticationService.validateServiceTicket(serviceTicket,
            new SimpleService("test"));
    }

    public void testValidateServiceTicketWithInvalidService()
        throws TicketException {
        final String ticketGrantingTicket = this.centralAuthenticationService
            .createTicketGrantingTicket(getUsernamePasswordCredentials());
        final String serviceTicket = this.centralAuthenticationService
            .grantServiceTicket(ticketGrantingTicket, new SimpleService("test"));

        try {
            this.centralAuthenticationService.validateServiceTicket(
                serviceTicket, new SimpleService("test2"));
            fail("Exception expected.");
        } catch (TicketException e) {
            return;
        }
    }

    public void testValidateServiceTicketWithInvalidServiceTicket()
        throws TicketException {
        final String ticketGrantingTicket = this.centralAuthenticationService
            .createTicketGrantingTicket(getUsernamePasswordCredentials());
        final String serviceTicket = this.centralAuthenticationService
            .grantServiceTicket(ticketGrantingTicket, new SimpleService("test"));
        this.centralAuthenticationService
            .destroyTicketGrantingTicket(ticketGrantingTicket);

        try {
            this.centralAuthenticationService.validateServiceTicket(
                serviceTicket, new SimpleService("test"));
            fail("Exception expected.");
        } catch (TicketException e) {
            return;
        }
    }

    public void testValidateServiceTicketNonExistantTicket() {
        try {
            this.centralAuthenticationService.validateServiceTicket("test",
                new SimpleService("test"));
            fail("Exception expected.");
        } catch (TicketException e) {
            return;
        }

    }

    public void testValidateServiceTicketWithNullCredentials()
        throws TicketException {

        try {
            this.centralAuthenticationService.validateServiceTicket(null, null);
            fail("Exception expected.");
        } catch (IllegalArgumentException e) {
            return;
        }
    }

    private UsernamePasswordCredentials getUsernamePasswordCredentials() {
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setUsername("test");
        c.setPassword("test");

        return c;
    }

    private UsernamePasswordCredentials getUsernamePasswordCredentials2() {
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setUsername("test1");
        c.setPassword("test1");

        return c;
    }

    private HttpBasedServiceCredentials getHttpBasedServiceCredentials() {
        try {
            HttpBasedServiceCredentials c = new HttpBasedServiceCredentials(
                new URL("https://www.acs.rutgers.edu"));
            return c;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException();
        }
    }

    private HttpBasedServiceCredentials getBadHttpBasedServiceCredentials() {
        try {
            HttpBasedServiceCredentials c = new HttpBasedServiceCredentials(
                new URL("http://www.acs.rutgers.edu"));
            return c;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException();
        }
    }
}
