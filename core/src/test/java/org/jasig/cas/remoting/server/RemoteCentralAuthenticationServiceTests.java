/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.remoting.server;

import org.jasig.cas.AbstractCentralAuthenticationServiceTest;
import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.principal.SimpleService;
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
                .createTicketGrantingTicket(TestUtils
                    .getCredentialsWithDifferentUsernameAndPassword(null, null));
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            return;
        }
    }

    public void testValidCredentials() throws TicketException {
        this.remoteCentralAuthenticationService
            .createTicketGrantingTicket(TestUtils
                .getCredentialsWithSameUsernameAndPassword());
    }

    public void testDontUseValidatorsToCheckValidCredentials() {
        try {
            this.remoteCentralAuthenticationService.setValidators(null);
            this.remoteCentralAuthenticationService
                .createTicketGrantingTicket(TestUtils
                    .getCredentialsWithDifferentUsernameAndPassword());
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
        final String ticketId = this.remoteCentralAuthenticationService
            .createTicketGrantingTicket(TestUtils
                .getCredentialsWithSameUsernameAndPassword());
        this.remoteCentralAuthenticationService.grantServiceTicket(ticketId,
            new SimpleService("test"));
    }

    public void testGrantServiceTicketWithValidCredentials()
        throws TicketException {
        final String ticketGrantingTicketId = this.remoteCentralAuthenticationService
            .createTicketGrantingTicket(TestUtils
                .getCredentialsWithSameUsernameAndPassword());
        this.remoteCentralAuthenticationService.grantServiceTicket(
            ticketGrantingTicketId, new SimpleService("Test"), TestUtils
                .getCredentialsWithSameUsernameAndPassword());
    }

    public void testGrantServiceTicketWithNullCredentials()
        throws TicketException {
        final String ticketGrantingTicketId = this.remoteCentralAuthenticationService
            .createTicketGrantingTicket(TestUtils
                .getCredentialsWithSameUsernameAndPassword());
        this.remoteCentralAuthenticationService.grantServiceTicket(
            ticketGrantingTicketId, new SimpleService("Test"), null);
    }

    public void testGrantServiceTicketWithEmptyCredentials()
        throws TicketException {
        final String ticketGrantingTicketId = this.remoteCentralAuthenticationService
            .createTicketGrantingTicket(TestUtils
                .getCredentialsWithSameUsernameAndPassword());
        try {
            this.remoteCentralAuthenticationService.grantServiceTicket(
                ticketGrantingTicketId, new SimpleService("Test"), TestUtils
                    .getCredentialsWithDifferentUsernameAndPassword("", ""));
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            return;
        }
    }

    public void testValidateServiceTicketWithValidService()
        throws TicketException {
        final String ticketGrantingTicket = this.remoteCentralAuthenticationService
            .createTicketGrantingTicket(TestUtils
                .getCredentialsWithSameUsernameAndPassword());
        final String serviceTicket = this.remoteCentralAuthenticationService
            .grantServiceTicket(ticketGrantingTicket, new SimpleService("test"));

        this.remoteCentralAuthenticationService.validateServiceTicket(
            serviceTicket, new SimpleService("test"));
    }

    public void testDelegateTicketGrantingTicketWithValidCredentials()
        throws TicketException {
        final String ticketGrantingTicket = this.remoteCentralAuthenticationService
            .createTicketGrantingTicket(TestUtils
                .getCredentialsWithSameUsernameAndPassword());
        final String serviceTicket = this.remoteCentralAuthenticationService
            .grantServiceTicket(ticketGrantingTicket, new SimpleService("test"));
        this.remoteCentralAuthenticationService.delegateTicketGrantingTicket(
            serviceTicket, TestUtils.getHttpBasedServiceCredentials());
    }

    public void testDelegateTicketGrantingTicketWithInvalidCredentials()
        throws TicketException {
        final String ticketGrantingTicket = this.remoteCentralAuthenticationService
            .createTicketGrantingTicket(TestUtils
                .getCredentialsWithSameUsernameAndPassword());
        final String serviceTicket = this.remoteCentralAuthenticationService
            .grantServiceTicket(ticketGrantingTicket, new SimpleService("test"));
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
