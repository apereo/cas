/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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
package org.jasig.cas.remoting.server;

import org.jasig.cas.AbstractCentralAuthenticationServiceTest;
import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * @author Scott Battaglia
   @deprecated As of 4.1
 * @since 3.0.0
 */
@Deprecated
public class RemoteCentralAuthenticationServiceTests extends AbstractCentralAuthenticationServiceTest {

    private RemoteCentralAuthenticationService remoteCentralAuthenticationService;

    @Before
    public void onSetUp() throws Exception {
        this.remoteCentralAuthenticationService = new RemoteCentralAuthenticationService();
        this.remoteCentralAuthenticationService.setCentralAuthenticationService(getCentralAuthenticationService());
    }

    @Test
    public void verifyValidCredentials() throws Exception {
        this.remoteCentralAuthenticationService.createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
    }

    @Test
    public void verifyInvalidCredentials() throws Exception {
        try {
            this.remoteCentralAuthenticationService.createTicketGrantingTicket(
                    TestUtils.getCredentialsWithDifferentUsernameAndPassword(null, null));
            fail("IllegalArgumentException expected.");
        } catch (final IllegalArgumentException e) {
            return;
        }
    }

    @Test
    public void verifyDontUseValidatorsToCheckValidCredentials() throws Exception {
        try {
            this.remoteCentralAuthenticationService.createTicketGrantingTicket(
                    TestUtils.getCredentialsWithDifferentUsernameAndPassword());
            fail("AuthenticationException expected.");
        } catch (final AuthenticationException e) {
            return;
        }
    }

    @Test
    public void verifyDestroyTicketGrantingTicket() {
        this.remoteCentralAuthenticationService
            .destroyTicketGrantingTicket("test");
    }

    @Test
    public void verifyGrantServiceTicketWithValidTicketGrantingTicket() throws Exception {
        final TicketGrantingTicket ticketId = this.remoteCentralAuthenticationService
            .createTicketGrantingTicket(TestUtils
                .getCredentialsWithSameUsernameAndPassword());
        this.remoteCentralAuthenticationService.grantServiceTicket(ticketId.getId(),
            TestUtils.getService());
    }

    @Test
    public void verifyGrantServiceTicketWithValidCredentials() throws Exception {
        final TicketGrantingTicket ticketGrantingTicketId = this.remoteCentralAuthenticationService
            .createTicketGrantingTicket(TestUtils
                .getCredentialsWithSameUsernameAndPassword());
        this.remoteCentralAuthenticationService.grantServiceTicket(
            ticketGrantingTicketId.getId(), TestUtils.getService(), TestUtils
                .getCredentialsWithSameUsernameAndPassword());
    }

    @Test
    public void verifyGrantServiceTicketWithNullCredentials() throws Exception {
        final TicketGrantingTicket ticketGrantingTicketId = this.remoteCentralAuthenticationService
            .createTicketGrantingTicket(TestUtils
                .getCredentialsWithSameUsernameAndPassword());
        this.remoteCentralAuthenticationService.grantServiceTicket(
            ticketGrantingTicketId.getId(), TestUtils.getService(), (Credential[]) null);
    }

    @Test
    public void verifyGrantServiceTicketWithEmptyCredentials() throws Exception {
        final TicketGrantingTicket ticketGrantingTicketId = this.remoteCentralAuthenticationService
            .createTicketGrantingTicket(TestUtils
                .getCredentialsWithSameUsernameAndPassword());
        try {
            this.remoteCentralAuthenticationService.grantServiceTicket(
                ticketGrantingTicketId.getId(), TestUtils.getService(), TestUtils
                    .getCredentialsWithDifferentUsernameAndPassword("", ""));
            fail("IllegalArgumentException expected.");
        } catch (final IllegalArgumentException e) {
            return;
        }
    }

    @Test
    public void verifyValidateServiceTicketWithValidService() throws Exception {
        final TicketGrantingTicket ticketGrantingTicket = this.remoteCentralAuthenticationService
            .createTicketGrantingTicket(TestUtils
                .getCredentialsWithSameUsernameAndPassword());
        final ServiceTicket serviceTicket = this.remoteCentralAuthenticationService
            .grantServiceTicket(ticketGrantingTicket.getId(), TestUtils.getService());

        this.remoteCentralAuthenticationService.validateServiceTicket(
            serviceTicket.getId(), TestUtils.getService());
    }

    @Test
    public void verifyDelegateTicketGrantingTicketWithValidCredentials() throws Exception {
        final TicketGrantingTicket ticketGrantingTicket = this.remoteCentralAuthenticationService
            .createTicketGrantingTicket(TestUtils
                .getCredentialsWithSameUsernameAndPassword());
        final ServiceTicket serviceTicket = this.remoteCentralAuthenticationService
            .grantServiceTicket(ticketGrantingTicket.getId(), TestUtils.getService());
        this.remoteCentralAuthenticationService.delegateTicketGrantingTicket(
            serviceTicket.getId(), TestUtils.getHttpBasedServiceCredentials());
    }

    @Test(expected=IllegalArgumentException.class)
    public void verifyDelegateTicketGrantingTicketWithInvalidCredentials() throws Exception {
        final TicketGrantingTicket ticketGrantingTicket = this.remoteCentralAuthenticationService
            .createTicketGrantingTicket(TestUtils
                .getCredentialsWithSameUsernameAndPassword());
        final ServiceTicket serviceTicket = this.remoteCentralAuthenticationService
            .grantServiceTicket(ticketGrantingTicket.getId(), TestUtils.getService());

        this.remoteCentralAuthenticationService
            .delegateTicketGrantingTicket(serviceTicket.getId(), TestUtils
                .getCredentialsWithDifferentUsernameAndPassword("", ""));
        fail("IllegalArgumentException expected.");
    }
}
