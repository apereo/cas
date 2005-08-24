/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas;

import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicy;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class CentralAuthenticationServiceImplTests extends
    AbstractCentralAuthenticationServiceTest {

    public void testNullCredentialsOnTicketGrantingTicketCreation()
        throws TicketException {
        try {
            getCentralAuthenticationService().createTicketGrantingTicket(null);
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            return;
        }
    }

    public void testBadCredentialsOnTicketGrantingTicketCreation() {
        try {
            getCentralAuthenticationService().createTicketGrantingTicket(
                TestUtils.getCredentialsWithDifferentUsernameAndPassword("test",
                    "test1"));
            fail("TicketException expected.");
        } catch (TicketException e) {
            return;
        }
    }

    public void testGoodCredentialsOnTicketGrantingTicketCreation() {
        try {
            assertNotNull(getCentralAuthenticationService()
                .createTicketGrantingTicket(
                    TestUtils.getCredentialsWithSameUsernameAndPassword()));
        } catch (TicketException e) {
            fail("TicketException not expected.");
        }
    }

    public void testDestroyTicketGrantingTicketWithNull() {
        try {
            getCentralAuthenticationService().destroyTicketGrantingTicket(null);
            fail("IllegalArgumentException exepcted.");
        } catch (IllegalArgumentException e) {
            return;
        }
    }

    public void testDestroyTicketGrantingTicketWithNonExistantTicket() {
        getCentralAuthenticationService().destroyTicketGrantingTicket("test");
    }

    public void testDestroyTicketGrantingTicketWithValidTicket()
        throws TicketException {
        final String ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        getCentralAuthenticationService().destroyTicketGrantingTicket(ticketId);
    }

    public void testDestroyTicketGrantingTicketWithInvalidTicket()
        throws TicketException {
        final String ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String serviceTicketId = getCentralAuthenticationService()
            .grantServiceTicket(ticketId, new SimpleService("test"));
        try {
            getCentralAuthenticationService().destroyTicketGrantingTicket(
                serviceTicketId);
            fail("ClassCastException expected.");
        } catch (ClassCastException e) {
            return;
        }
    }

    public void testGrantServiceTicketWithValidTicketGrantingTicket()
        throws TicketException {
        final String ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        getCentralAuthenticationService().grantServiceTicket(ticketId,
            new SimpleService("test"));
    }

    public void testGrantServiceTicketWithInvalidTicketGrantingTicket()
        throws TicketException {
        final String ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        getCentralAuthenticationService().destroyTicketGrantingTicket(ticketId);
        try {
            getCentralAuthenticationService().grantServiceTicket(ticketId,
                new SimpleService("test"));
            fail("Expected exception to be thrown.");
        } catch (TicketException e) {
            return;
        }
    }

    public void testDelegateTicketGrantingTicketWithProperParams()
        throws TicketException {
        final String ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String serviceTicketId = getCentralAuthenticationService()
            .grantServiceTicket(ticketId, new SimpleService("test"));
        getCentralAuthenticationService().delegateTicketGrantingTicket(
            serviceTicketId, TestUtils.getHttpBasedServiceCredentials());
    }

    public void testDelegateTicketGrantingTicketWithBadCredentials()
        throws TicketException {
        final String ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String serviceTicketId = getCentralAuthenticationService()
            .grantServiceTicket(ticketId, new SimpleService("test"));
        try {
            getCentralAuthenticationService().delegateTicketGrantingTicket(
                serviceTicketId, TestUtils.getBadHttpBasedServiceCredentials());
            fail("TicketException expected.");
        } catch (TicketException e) {
            return;
        }
    }

    public void testDelegateTicketGrantingTicketWithBadServiceTicket()
        throws TicketException {
        final String ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String serviceTicketId = getCentralAuthenticationService()
            .grantServiceTicket(ticketId, new SimpleService("test"));
        getCentralAuthenticationService().destroyTicketGrantingTicket(ticketId);
        try {
            getCentralAuthenticationService().delegateTicketGrantingTicket(
                serviceTicketId, TestUtils.getHttpBasedServiceCredentials());
            fail("TicketException expected.");
        } catch (TicketException e) {
            return;
        }
    }

    public void testDelegateTicketGrantingTicketWithNullParams()
        throws TicketException {
        try {
            getCentralAuthenticationService().delegateTicketGrantingTicket(
                null, null);
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            return;
        }
    }

    public void testGrantServiceTicketWithNullParams() throws TicketException {
        try {
            getCentralAuthenticationService().grantServiceTicket(null, null);
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            return;
        }
    }

    public void testGrantServiceTicketWithValidCredentials()
        throws TicketException {
        final String ticketGrantingTicket = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        getCentralAuthenticationService().grantServiceTicket(
            ticketGrantingTicket, new SimpleService("test"),
            TestUtils.getCredentialsWithSameUsernameAndPassword());
    }

    public void testGrantServiceTicketWithInvalidCredentials()
        throws TicketException {
        final String ticketGrantingTicket = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        try {
            getCentralAuthenticationService().grantServiceTicket(
                ticketGrantingTicket, new SimpleService("test"),
                TestUtils.getBadHttpBasedServiceCredentials());
            fail("Exception expected.");
        } catch (TicketException e) {
            return;
        }
    }

    public void testGrantServiceTicketWithDifferentCredentials()
        throws TicketException {
        final String ticketGrantingTicket = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        try {
            getCentralAuthenticationService().grantServiceTicket(
                ticketGrantingTicket,
                new SimpleService("test"),
                TestUtils.getCredentialsWithSameUsernameAndPassword("test1"));
            fail("Exception expected.");
        } catch (TicketException e) {
            return;
        }
    }

    public void testValidateServiceTicketWithExpires() throws TicketException {
        ((CentralAuthenticationServiceImpl) getCentralAuthenticationService())
            .setServiceTicketExpirationPolicy(new MultiTimeUseOrTimeoutExpirationPolicy(
                1, 1100));
        final String ticketGrantingTicket = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String serviceTicket = getCentralAuthenticationService()
            .grantServiceTicket(ticketGrantingTicket, new SimpleService("test"));

        getCentralAuthenticationService().validateServiceTicket(serviceTicket,
            new SimpleService("test"));

        assertFalse(getTicketRegistry().deleteTicket(serviceTicket));
        ((CentralAuthenticationServiceImpl) getCentralAuthenticationService())
            .setServiceTicketExpirationPolicy(new NeverExpiresExpirationPolicy());
    }

    public void testValidateServiceTicketWithValidService()
        throws TicketException {
        final String ticketGrantingTicket = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String serviceTicket = getCentralAuthenticationService()
            .grantServiceTicket(ticketGrantingTicket, new SimpleService("test"));

        getCentralAuthenticationService().validateServiceTicket(serviceTicket,
            new SimpleService("test"));
    }

    public void testValidateServiceTicketWithInvalidService()
        throws TicketException {
        final String ticketGrantingTicket = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String serviceTicket = getCentralAuthenticationService()
            .grantServiceTicket(ticketGrantingTicket, new SimpleService("test"));

        try {
            getCentralAuthenticationService().validateServiceTicket(
                serviceTicket, new SimpleService("test2"));
            fail("Exception expected.");
        } catch (TicketException e) {
            return;
        }
    }

    public void testValidateServiceTicketWithInvalidServiceTicket()
        throws TicketException {
        final String ticketGrantingTicket = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String serviceTicket = getCentralAuthenticationService()
            .grantServiceTicket(ticketGrantingTicket, new SimpleService("test"));
        getCentralAuthenticationService().destroyTicketGrantingTicket(
            ticketGrantingTicket);

        try {
            getCentralAuthenticationService().validateServiceTicket(
                serviceTicket, new SimpleService("test"));
            fail("Exception expected.");
        } catch (TicketException e) {
            return;
        }
    }

    public void testValidateServiceTicketNonExistantTicket() {
        try {
            getCentralAuthenticationService().validateServiceTicket("test",
                new SimpleService("test"));
            fail("Exception expected.");
        } catch (TicketException e) {
            return;
        }

    }

    public void testValidateServiceTicketWithNullCredentials()
        throws TicketException {

        try {
            getCentralAuthenticationService().validateServiceTicket(null, null);
            fail("Exception expected.");
        } catch (IllegalArgumentException e) {
            return;
        }
    }

}
