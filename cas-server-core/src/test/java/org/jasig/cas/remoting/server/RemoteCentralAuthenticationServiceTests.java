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
package org.jasig.cas.remoting.server;

import org.jasig.cas.AbstractCentralAuthenticationServiceTest;
import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.AuthenticationException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * @author Scott Battaglia

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
    public void testValidCredentials() throws Exception {
        this.remoteCentralAuthenticationService.createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
    }

    @Test
    public void testInvalidCredentials() throws Exception {
        try {
            this.remoteCentralAuthenticationService.createTicketGrantingTicket(
                    TestUtils.getCredentialsWithDifferentUsernameAndPassword(null, null));
            fail("IllegalArgumentException expected.");
        } catch (final IllegalArgumentException e) {
            return;
        }
    }

    @Test
    public void testDontUseValidatorsToCheckValidCredentials() throws Exception {
        try {
            this.remoteCentralAuthenticationService.createTicketGrantingTicket(
                    TestUtils.getCredentialsWithDifferentUsernameAndPassword());
            fail("AuthenticationException expected.");
        } catch (final AuthenticationException e) {
            return;
        }
    }

    @Test
    public void testDestroyTicketGrantingTicket() {
        this.remoteCentralAuthenticationService
            .destroyTicketGrantingTicket("test");
    }

    @Test
    public void testGrantServiceTicketWithValidTicketGrantingTicket() throws Exception {
        final String ticketId = this.remoteCentralAuthenticationService
            .createTicketGrantingTicket(TestUtils
                .getCredentialsWithSameUsernameAndPassword());
        this.remoteCentralAuthenticationService.grantServiceTicket(ticketId,
            TestUtils.getService());
    }

    @Test
    public void testGrantServiceTicketWithValidCredentials() throws Exception {
        final String ticketGrantingTicketId = this.remoteCentralAuthenticationService
            .createTicketGrantingTicket(TestUtils
                .getCredentialsWithSameUsernameAndPassword());
        this.remoteCentralAuthenticationService.grantServiceTicket(
            ticketGrantingTicketId, TestUtils.getService(), TestUtils
                .getCredentialsWithSameUsernameAndPassword());
    }

    @Test
    public void testGrantServiceTicketWithNullCredentials() throws Exception {
        final String ticketGrantingTicketId = this.remoteCentralAuthenticationService
            .createTicketGrantingTicket(TestUtils
                .getCredentialsWithSameUsernameAndPassword());
        this.remoteCentralAuthenticationService.grantServiceTicket(
            ticketGrantingTicketId, TestUtils.getService(), null);
    }

    @Test
    public void testGrantServiceTicketWithEmptyCredentials() throws Exception {
        final String ticketGrantingTicketId = this.remoteCentralAuthenticationService
            .createTicketGrantingTicket(TestUtils
                .getCredentialsWithSameUsernameAndPassword());
        try {
            this.remoteCentralAuthenticationService.grantServiceTicket(
                ticketGrantingTicketId, TestUtils.getService(), TestUtils
                    .getCredentialsWithDifferentUsernameAndPassword("", ""));
            fail("IllegalArgumentException expected.");
        } catch (final IllegalArgumentException e) {
            return;
        }
    }

    @Test
    public void testValidateServiceTicketWithValidService() throws Exception {
        final String ticketGrantingTicket = this.remoteCentralAuthenticationService
            .createTicketGrantingTicket(TestUtils
                .getCredentialsWithSameUsernameAndPassword());
        final String serviceTicket = this.remoteCentralAuthenticationService
            .grantServiceTicket(ticketGrantingTicket, TestUtils.getService());

        this.remoteCentralAuthenticationService.validateServiceTicket(
            serviceTicket, TestUtils.getService());
    }

    @Test
    public void testDelegateTicketGrantingTicketWithValidCredentials() throws Exception {
        final String ticketGrantingTicket = this.remoteCentralAuthenticationService
            .createTicketGrantingTicket(TestUtils
                .getCredentialsWithSameUsernameAndPassword());
        final String serviceTicket = this.remoteCentralAuthenticationService
            .grantServiceTicket(ticketGrantingTicket, TestUtils.getService());
        this.remoteCentralAuthenticationService.delegateTicketGrantingTicket(
            serviceTicket, TestUtils.getHttpBasedServiceCredentials());
    }

    @Test
    public void testDelegateTicketGrantingTicketWithInvalidCredentials() throws Exception {
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
        } catch (final IllegalArgumentException e) {
            return;
        }

    }
}
