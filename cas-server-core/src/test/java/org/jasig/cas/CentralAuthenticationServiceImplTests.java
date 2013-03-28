/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.services.UnauthorizedServiceException;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.ticket.TicketState;
import org.jasig.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicy;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.jasig.cas.validation.Assertion;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0
 */
public class CentralAuthenticationServiceImplTests extends AbstractCentralAuthenticationServiceTest {

    @Test(expected=TicketException.class)
    public void testBadCredentialsOnTicketGrantingTicketCreation() throws TicketException {
        getCentralAuthenticationService().createTicketGrantingTicket(
                TestUtils.getCredentialsWithDifferentUsernameAndPassword());
    }

    @Test
    public void testGoodCredentialsOnTicketGrantingTicketCreation() {
        try {
            assertNotNull(getCentralAuthenticationService()
                .createTicketGrantingTicket(
                    TestUtils.getCredentialsWithSameUsernameAndPassword()));
        } catch (final TicketException e) {
            fail(TestUtils.CONST_EXCEPTION_NON_EXPECTED);
        }
    }

    @Test
    public void testDestroyTicketGrantingTicketWithNonExistantTicket() {
        getCentralAuthenticationService().destroyTicketGrantingTicket("test");
    }

    @Test
    public void testDestroyTicketGrantingTicketWithValidTicket()
        throws TicketException {
        final String ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        getCentralAuthenticationService().destroyTicketGrantingTicket(ticketId);
    }

    @Test(expected=ClassCastException.class)
    public void testDestroyTicketGrantingTicketWithInvalidTicket()
        throws TicketException {
        final String ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String serviceTicketId = getCentralAuthenticationService()
            .grantServiceTicket(ticketId, TestUtils.getService());

        getCentralAuthenticationService().destroyTicketGrantingTicket(
                serviceTicketId);

    }

    @Test
    public void testGrantServiceTicketWithValidTicketGrantingTicket()
        throws TicketException {
        final String ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        getCentralAuthenticationService().grantServiceTicket(ticketId,
            TestUtils.getService());
    }

    @Test(expected=TicketException.class)
    public void testGrantServiceTicketWithInvalidTicketGrantingTicket()
        throws TicketException {
        final String ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        getCentralAuthenticationService().destroyTicketGrantingTicket(ticketId);
            getCentralAuthenticationService().grantServiceTicket(ticketId,
                TestUtils.getService());
    }

    @Test(expected=TicketException.class)
    public void testGrantServiceTicketWithExpiredTicketGrantingTicket() throws TicketException {
        ((CentralAuthenticationServiceImpl) getCentralAuthenticationService()).setTicketGrantingTicketExpirationPolicy(
                new ExpirationPolicy() {
            private static final long serialVersionUID = 1L;

            public boolean isExpired(final TicketState ticket) {
                return true;
            }});
    final String ticketId = getCentralAuthenticationService()
        .createTicketGrantingTicket(
            TestUtils.getCredentialsWithSameUsernameAndPassword());
    try {
        getCentralAuthenticationService().grantServiceTicket(ticketId,
            TestUtils.getService());
    } finally {
        ((CentralAuthenticationServiceImpl) getCentralAuthenticationService()).setTicketGrantingTicketExpirationPolicy(
                new NeverExpiresExpirationPolicy());
    }
}

    @Test
    public void testDelegateTicketGrantingTicketWithProperParams() throws TicketException {
        final String ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String serviceTicketId = getCentralAuthenticationService()
            .grantServiceTicket(ticketId, TestUtils.getService());
        getCentralAuthenticationService().delegateTicketGrantingTicket(
            serviceTicketId, TestUtils.getHttpBasedServiceCredentials());
    }

    @Test(expected=TicketException.class)
    public void testDelegateTicketGrantingTicketWithBadCredentials()
        throws TicketException {
        final String ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String serviceTicketId = getCentralAuthenticationService()
            .grantServiceTicket(ticketId, TestUtils.getService());

        getCentralAuthenticationService().delegateTicketGrantingTicket(
            serviceTicketId, TestUtils.getBadHttpBasedServiceCredentials());
    }

    @Test(expected=TicketException.class)
    public void testDelegateTicketGrantingTicketWithBadServiceTicket()
        throws TicketException {
        final String ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String serviceTicketId = getCentralAuthenticationService()
            .grantServiceTicket(ticketId, TestUtils.getService());
        getCentralAuthenticationService().destroyTicketGrantingTicket(ticketId);
        getCentralAuthenticationService().delegateTicketGrantingTicket(
            serviceTicketId, TestUtils.getHttpBasedServiceCredentials());
    }

    @Test
    public void testGrantServiceTicketWithValidCredentials()
        throws TicketException {
        final String ticketGrantingTicket = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        getCentralAuthenticationService().grantServiceTicket(
            ticketGrantingTicket, TestUtils.getService(),
            TestUtils.getCredentialsWithSameUsernameAndPassword());
    }

    @Test(expected=TicketException.class)
    public void testGrantServiceTicketWithInvalidCredentials()
        throws TicketException {
        final String ticketGrantingTicket = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        getCentralAuthenticationService().grantServiceTicket(
            ticketGrantingTicket, TestUtils.getService(),
            TestUtils.getBadHttpBasedServiceCredentials());
    }

    @Test
    public void testGrantServiceTicketWithDifferentCredentials()
        throws TicketException {
        final String ticketGrantingTicket = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        getCentralAuthenticationService().grantServiceTicket(
            ticketGrantingTicket, TestUtils.getService(),
            TestUtils.getCredentialsWithSameUsernameAndPassword("test"));
    }

    @Test
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

    @Test
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

    @Test(expected=UnauthorizedServiceException.class)
    public void testValidateServiceTicketWithInvalidService()
        throws TicketException {
        final String ticketGrantingTicket = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String serviceTicket = getCentralAuthenticationService()
            .grantServiceTicket(ticketGrantingTicket, TestUtils.getService());

        getCentralAuthenticationService().validateServiceTicket(
            serviceTicket, TestUtils.getService("test2"));
    }

    @Test(expected=TicketException.class)
    public void testValidateServiceTicketWithInvalidServiceTicket()
        throws TicketException {
        final String ticketGrantingTicket = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String serviceTicket = getCentralAuthenticationService()
            .grantServiceTicket(ticketGrantingTicket, TestUtils.getService());
        getCentralAuthenticationService().destroyTicketGrantingTicket(
            ticketGrantingTicket);

        getCentralAuthenticationService().validateServiceTicket(
                serviceTicket, TestUtils.getService());
    }

    @Test(expected=TicketException.class)
    public void testValidateServiceTicketNonExistantTicket() throws TicketException {
        getCentralAuthenticationService().validateServiceTicket("test",
                TestUtils.getService());
    }

    @Test
    public void testValidateServiceTicketWithoutUsernameAttribute() throws TicketException {
        UsernamePasswordCredentials cred =  TestUtils.getCredentialsWithSameUsernameAndPassword();
        final String ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(cred);
        final String serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket,
                TestUtils.getService());

        final Assertion assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket,
                TestUtils.getService());
        final Authentication auth = assertion.getChainedAuthentications().get(0);
        assertEquals(auth.getPrincipal().getId(), cred.getUsername());
    }

    @Test
    public void testValidateServiceTicketWithDefaultUsernameAttribute() throws TicketException {
        UsernamePasswordCredentials cred =  TestUtils.getCredentialsWithSameUsernameAndPassword();
        final String ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(cred);

        Service svc = TestUtils.getService("testDefault");
        final String serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket, svc);

        final Assertion assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket, svc);
        final Authentication auth = assertion.getChainedAuthentications().get(0);
        assertEquals(auth.getPrincipal().getId(), cred.getUsername());
    }

    @Test
    public void testValidateServiceTicketWithUsernameAttribute() throws TicketException {
        UsernamePasswordCredentials cred =  TestUtils.getCredentialsWithSameUsernameAndPassword();
        final String ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(cred);

        Service svc = TestUtils.getService("eduPersonTest");
        final String serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket, svc);

        final Assertion assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket, svc);
        final Authentication auth = assertion.getChainedAuthentications().get(0);
        assertEquals(auth.getPrincipal().getId(), "developer");
    }

    @Test
    public void testValidateServiceTicketWithInvalidUsernameAttribute() throws TicketException {
        UsernamePasswordCredentials cred =  TestUtils.getCredentialsWithSameUsernameAndPassword();
        final String ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(cred);

        Service svc = TestUtils.getService("eduPersonTestInvalid");
        final String serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket, svc);

        final Assertion assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket, svc);
        final Authentication auth = assertion.getChainedAuthentications().get(0);

        /*
         * The attribute specified for this service is not allows in the list of returned attributes.
         * Therefore, we expect the default to be returned.
         */
        assertEquals(auth.getPrincipal().getId(), cred.getUsername());
    }
}
