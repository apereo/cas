/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
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
            fail(TestUtils.CONST_EXCEPTION_EXPECTED);
        } catch (IllegalArgumentException e) {
            // nothing to do here, exception is expected.
        }
    }

    public void testBadCredentialsOnTicketGrantingTicketCreation() {
        try {
            getCentralAuthenticationService().createTicketGrantingTicket(
                TestUtils.getCredentialsWithDifferentUsernameAndPassword());
            fail(TestUtils.CONST_EXCEPTION_EXPECTED);
        } catch (TicketException e) {
            // nothing to do here, exception is expected.
        }
    }

    public void testGoodCredentialsOnTicketGrantingTicketCreation() {
        try {
            assertNotNull(getCentralAuthenticationService()
                .createTicketGrantingTicket(
                    TestUtils.getCredentialsWithSameUsernameAndPassword()));
        } catch (TicketException e) {
            fail(TestUtils.CONST_EXCEPTION_NON_EXPECTED);
        }
    }

    public void testDestroyTicketGrantingTicketWithNull() {
        try {
            getCentralAuthenticationService().destroyTicketGrantingTicket(null);
            fail(TestUtils.CONST_EXCEPTION_EXPECTED);
        } catch (IllegalArgumentException e) {
            // nothing to do here, exception is expected.
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
            .grantServiceTicket(ticketId, TestUtils.getService());
        try {
            getCentralAuthenticationService().destroyTicketGrantingTicket(
                serviceTicketId);
            fail(TestUtils.CONST_EXCEPTION_EXPECTED);
        } catch (ClassCastException e) {
            // nothing to do here, exception is expected.
        }
    }

    public void testGrantServiceTicketWithValidTicketGrantingTicket()
        throws TicketException {
        final String ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        getCentralAuthenticationService().grantServiceTicket(ticketId,
            TestUtils.getService());
    }

    public void testGrantServiceTicketWithInvalidTicketGrantingTicket()
        throws TicketException {
        final String ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        getCentralAuthenticationService().destroyTicketGrantingTicket(ticketId);
        try {
            getCentralAuthenticationService().grantServiceTicket(ticketId,
                TestUtils.getService());
            fail(TestUtils.CONST_EXCEPTION_EXPECTED);
        } catch (TicketException e) {
            // nothing to do here, exception is expected.
        }
    }

    public void testDelegateTicketGrantingTicketWithProperParams()
        throws TicketException {
        final String ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String serviceTicketId = getCentralAuthenticationService()
            .grantServiceTicket(ticketId, TestUtils.getService());
        getCentralAuthenticationService().delegateTicketGrantingTicket(
            serviceTicketId, TestUtils.getHttpBasedServiceCredentials());
    }

    public void testDelegateTicketGrantingTicketWithBadCredentials()
        throws TicketException {
        final String ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String serviceTicketId = getCentralAuthenticationService()
            .grantServiceTicket(ticketId, TestUtils.getService());
        try {
            getCentralAuthenticationService().delegateTicketGrantingTicket(
                serviceTicketId, TestUtils.getBadHttpBasedServiceCredentials());
            fail(TestUtils.CONST_EXCEPTION_EXPECTED);
        } catch (TicketException e) {
            // nothing to do here, exception is expected.
        }
    }

    public void testDelegateTicketGrantingTicketWithBadServiceTicket()
        throws TicketException {
        final String ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String serviceTicketId = getCentralAuthenticationService()
            .grantServiceTicket(ticketId, TestUtils.getService());
        getCentralAuthenticationService().destroyTicketGrantingTicket(ticketId);
        try {
            getCentralAuthenticationService().delegateTicketGrantingTicket(
                serviceTicketId, TestUtils.getHttpBasedServiceCredentials());
            fail(TestUtils.CONST_EXCEPTION_EXPECTED);
        } catch (TicketException e) {
            // nothing to do here, exception is expected.
        }
    }

    public void testDelegateTicketGrantingTicketWithNullParams()
        throws TicketException {
        try {
            getCentralAuthenticationService().delegateTicketGrantingTicket(
                null, null);
            fail(TestUtils.CONST_EXCEPTION_EXPECTED);
        } catch (IllegalArgumentException e) {
            // nothing to do here, exception is expected.
        }
    }

    public void testGrantServiceTicketWithNullParams() throws TicketException {
        try {
            getCentralAuthenticationService().grantServiceTicket(null, null);
            fail(TestUtils.CONST_EXCEPTION_EXPECTED);
        } catch (IllegalArgumentException e) {
            // nothing to do here, exception is expected.
        }
    }

    public void testGrantServiceTicketWithValidCredentials()
        throws TicketException {
        final String ticketGrantingTicket = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        getCentralAuthenticationService().grantServiceTicket(
            ticketGrantingTicket, TestUtils.getService(),
            TestUtils.getCredentialsWithSameUsernameAndPassword());
    }

    public void testGrantServiceTicketWithInvalidCredentials()
        throws TicketException {
        final String ticketGrantingTicket = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        try {
            getCentralAuthenticationService().grantServiceTicket(
                ticketGrantingTicket, TestUtils.getService(),
                TestUtils.getBadHttpBasedServiceCredentials());
            fail(TestUtils.CONST_EXCEPTION_EXPECTED);
        } catch (TicketException e) {
            // nothing to do here, exception is expected.
        }
    }

    public void testGrantServiceTicketWithDifferentCredentials()
        throws TicketException {
        final String ticketGrantingTicket = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        try {
            getCentralAuthenticationService().grantServiceTicket(
                ticketGrantingTicket, TestUtils.getService(),
                TestUtils.getCredentialsWithSameUsernameAndPassword("test1"));
            fail(TestUtils.CONST_EXCEPTION_EXPECTED);
        } catch (TicketException e) {
            // nothing to do here, exception is expected.
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
            .grantServiceTicket(ticketGrantingTicket, TestUtils.getService());

        getCentralAuthenticationService().validateServiceTicket(serviceTicket,
            TestUtils.getService());

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
            .grantServiceTicket(ticketGrantingTicket, TestUtils.getService());

        getCentralAuthenticationService().validateServiceTicket(serviceTicket,
            TestUtils.getService());
    }

    public void testValidateServiceTicketWithInvalidService()
        throws TicketException {
        final String ticketGrantingTicket = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String serviceTicket = getCentralAuthenticationService()
            .grantServiceTicket(ticketGrantingTicket, TestUtils.getService());

        try {
            getCentralAuthenticationService().validateServiceTicket(
                serviceTicket, new SimpleService("test2"));
            fail(TestUtils.CONST_EXCEPTION_EXPECTED);
        } catch (TicketException e) {
            // nothing to do here, exception is expected.
        }
    }

    public void testValidateServiceTicketWithInvalidServiceTicket()
        throws TicketException {
        final String ticketGrantingTicket = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String serviceTicket = getCentralAuthenticationService()
            .grantServiceTicket(ticketGrantingTicket, TestUtils.getService());
        getCentralAuthenticationService().destroyTicketGrantingTicket(
            ticketGrantingTicket);

        try {
            getCentralAuthenticationService().validateServiceTicket(
                serviceTicket, TestUtils.getService());
            fail(TestUtils.CONST_EXCEPTION_EXPECTED);
        } catch (TicketException e) {
            // nothing to do here, exception is expected.
        }
    }

    public void testValidateServiceTicketNonExistantTicket() {
        try {
            getCentralAuthenticationService().validateServiceTicket("test",
                TestUtils.getService());
            fail(TestUtils.CONST_EXCEPTION_EXPECTED);
        } catch (TicketException e) {
            // nothing to do here, exception is expected.
        }
    }

    public void testValidateServiceTicketWithNullCredentials()
        throws TicketException {

        try {
            getCentralAuthenticationService().validateServiceTicket(null, null);
            fail(TestUtils.CONST_EXCEPTION_EXPECTED);
        } catch (IllegalArgumentException e) {
            // nothing to do here, exception is expected.
        }
    }

}
