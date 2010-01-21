/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.remoting.server;

import org.jasig.cas.AbstractCentralAuthenticationServiceTest;
import org.jasig.cas.TestUtils;
import org.jasig.cas.ticket.TicketException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class RemoteCentralAuthenticationServiceTests extends AbstractCentralAuthenticationServiceTest {

    private RemoteCentralAuthenticationService remoteCentralAuthenticationService;

    @Before
    public void onSetUp() throws Exception {
        this.remoteCentralAuthenticationService = new RemoteCentralAuthenticationService();
        this.remoteCentralAuthenticationService.setCentralAuthenticationService(getCentralAuthenticationService());
    }

    @Test
    public void testValidCredentials() throws TicketException {
        this.remoteCentralAuthenticationService.createTicketGrantingTicket(TestUtils.getCredentialsWithSameUsernameAndPassword());
    }

    @Test
    public void testInvalidCredentials() throws TicketException {
        try {
            this.remoteCentralAuthenticationService.createTicketGrantingTicket(TestUtils.getCredentialsWithDifferentUsernameAndPassword(null, null));
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            return;
        }
    }

    @Test
    public void testDontUseValidatorsToCheckValidCredentials() {
        try {
            this.remoteCentralAuthenticationService.createTicketGrantingTicket(TestUtils.getCredentialsWithDifferentUsernameAndPassword());
            fail("TicketException expected.");
        } catch (TicketException e) {
            return;
        }
    }

    @Test
    public void testDestroyTicketGrantingTicket() {
        this.remoteCentralAuthenticationService
            .destroyTicketGrantingTicket("test");
    }

    @Test
    public void testGrantServiceTicketWithValidTicketGrantingTicket()
        throws TicketException {
        final String ticketId = this.remoteCentralAuthenticationService
            .createTicketGrantingTicket(TestUtils
                .getCredentialsWithSameUsernameAndPassword());
        this.remoteCentralAuthenticationService.grantServiceTicket(ticketId,
            TestUtils.getService());
    }

    @Test
    public void testGrantServiceTicketWithValidCredentials()
        throws TicketException {
        final String ticketGrantingTicketId = this.remoteCentralAuthenticationService
            .createTicketGrantingTicket(TestUtils
                .getCredentialsWithSameUsernameAndPassword());
        this.remoteCentralAuthenticationService.grantServiceTicket(
            ticketGrantingTicketId, TestUtils.getService(), TestUtils
                .getCredentialsWithSameUsernameAndPassword());
    }

    @Test
    public void testGrantServiceTicketWithNullCredentials()
        throws TicketException {
        final String ticketGrantingTicketId = this.remoteCentralAuthenticationService
            .createTicketGrantingTicket(TestUtils
                .getCredentialsWithSameUsernameAndPassword());
        this.remoteCentralAuthenticationService.grantServiceTicket(
            ticketGrantingTicketId, TestUtils.getService(), null);
    }

    @Test
    public void testGrantServiceTicketWithEmptyCredentials()
        throws TicketException {
        final String ticketGrantingTicketId = this.remoteCentralAuthenticationService
            .createTicketGrantingTicket(TestUtils
                .getCredentialsWithSameUsernameAndPassword());
        try {
            this.remoteCentralAuthenticationService.grantServiceTicket(
                ticketGrantingTicketId, TestUtils.getService(), TestUtils
                    .getCredentialsWithDifferentUsernameAndPassword("", ""));
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            return;
        }
    }

    @Test
    public void testValidateServiceTicketWithValidService()
        throws TicketException {
        final String ticketGrantingTicket = this.remoteCentralAuthenticationService
            .createTicketGrantingTicket(TestUtils
                .getCredentialsWithSameUsernameAndPassword());
        final String serviceTicket = this.remoteCentralAuthenticationService
            .grantServiceTicket(ticketGrantingTicket, TestUtils.getService());

        this.remoteCentralAuthenticationService.validateServiceTicket(
            serviceTicket, TestUtils.getService());
    }

    @Test
    public void testDelegateTicketGrantingTicketWithValidCredentials()
        throws TicketException {
        final String ticketGrantingTicket = this.remoteCentralAuthenticationService
            .createTicketGrantingTicket(TestUtils
                .getCredentialsWithSameUsernameAndPassword());
        final String serviceTicket = this.remoteCentralAuthenticationService
            .grantServiceTicket(ticketGrantingTicket, TestUtils.getService());
        this.remoteCentralAuthenticationService.delegateTicketGrantingTicket(
            serviceTicket, TestUtils.getHttpBasedServiceCredentials());
    }

    @Test
    public void testDelegateTicketGrantingTicketWithInvalidCredentials()
        throws TicketException {
        final String ticketGrantingTicket = this.remoteCentralAuthenticationService
            .createTicketGrantingTicket(TestUtils
                .getCredentialsWithSameUsernameAndPassword());
        final String serviceTicket = this.remoteCentralAuthenticationService
            .grantServiceTicket(ticketGrantingTicket, TestUtils.getService());
        try {
            this.remoteCentralAuthenticationService
                .delegateTicketGrantingTicket(serviceTicket, TestUtils
                    .getCredentialsWithDifferentUsernameAndPassword("", ""));
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            return;
        }

    }
}
