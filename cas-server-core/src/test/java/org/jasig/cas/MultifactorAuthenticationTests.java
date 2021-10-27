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

import org.jasig.cas.authentication.MixedPrincipalException;
import org.jasig.cas.authentication.OneTimePasswordCredential;
import org.jasig.cas.authentication.SuccessfulHandlerMetaDataPopulator;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.ticket.UnsatisfiedAuthenticationPolicyException;
import org.jasig.cas.validation.Assertion;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * High-level MFA functionality tests that leverage registered service metadata
 * ala {@link org.jasig.cas.authentication.RequiredHandlerAuthenticationPolicyFactory} to drive
 * authentication policy.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/mfa-test-context.xml" })
public class MultifactorAuthenticationTests {

    @Autowired
    private CentralAuthenticationService cas;

    @Test
    public void testAllowsAccessToNormalSecurityServiceWithPassword() throws Exception {
        final String tgt = cas.createTicketGrantingTicket(newUserPassCredentials("alice", "alice"));
        assertNotNull(tgt);
        final String st = cas.grantServiceTicket(tgt, newService("https://example.com/normal/"));
        assertNotNull(st);
    }

    @Test
    public void testAllowsAccessToNormalSecurityServiceWithOTP() throws Exception {
        final String tgt = cas.createTicketGrantingTicket(new OneTimePasswordCredential("alice", "31415"));
        assertNotNull(tgt);
        final String st = cas.grantServiceTicket(tgt, newService("https://example.com/normal/"));
        assertNotNull(st);
    }

    @Test(expected = UnsatisfiedAuthenticationPolicyException.class)
    public void testDeniesAccessToHighSecurityServiceWithPassword() throws Exception {
        final String tgt = cas.createTicketGrantingTicket(newUserPassCredentials("alice", "alice"));
        assertNotNull(tgt);
        cas.grantServiceTicket(tgt, newService("https://example.com/high/"));
    }

    @Test(expected = UnsatisfiedAuthenticationPolicyException.class)
    public void testDeniesAccessToHighSecurityServiceWithOTP() throws Exception {
        final String tgt = cas.createTicketGrantingTicket(new OneTimePasswordCredential("alice", "31415"));
        assertNotNull(tgt);
        final String st = cas.grantServiceTicket(tgt, newService("https://example.com/high/"));
        assertNotNull(st);
    }

    @Test
    public void testAllowsAccessToHighSecurityServiceWithPasswordAndOTP() throws Exception {
        final String tgt = cas.createTicketGrantingTicket(
                newUserPassCredentials("alice", "alice"),
                new OneTimePasswordCredential("alice", "31415"));
        assertNotNull(tgt);
        final String st = cas.grantServiceTicket(tgt, newService("https://example.com/high/"));
        assertNotNull(st);
    }

    @Test
    public void testAllowsAccessToHighSecurityServiceWithPasswordAndOTPViaRenew() throws Exception {
        // Note the original credential used to start SSO session does not satisfy security policy
        final String tgt = cas.createTicketGrantingTicket(newUserPassCredentials("alice", "alice"));
        assertNotNull(tgt);
        final Service service = newService("https://example.com/high/");
        final String st = cas.grantServiceTicket(
                tgt,
                service,
                newUserPassCredentials("alice", "alice"),
                new OneTimePasswordCredential("alice", "31415"));
        assertNotNull(st);
        // Confirm the authentication in the assertion is the one that satisfies security policy
        final Assertion assertion = cas.validateServiceTicket(st, service);
        assertEquals(2, assertion.getPrimaryAuthentication().getSuccesses().size());
        assertTrue(assertion.getPrimaryAuthentication().getSuccesses().containsKey("passwordHandler"));
        assertTrue(assertion.getPrimaryAuthentication().getSuccesses().containsKey("oneTimePasswordHandler"));
        assertTrue(assertion.getPrimaryAuthentication().getAttributes().containsKey(
                SuccessfulHandlerMetaDataPopulator.SUCCESSFUL_AUTHENTICATION_HANDLERS));
    }


    @Test(expected = MixedPrincipalException.class)
    public void testThrowsMixedPrincipalExceptionOnRenewWithDifferentPrincipal() throws Exception {
        // Note the original credential used to start SSO session does not satisfy security policy
        final String tgt = cas.createTicketGrantingTicket(newUserPassCredentials("alice", "alice"));
        assertNotNull(tgt);
        final Service service = newService("https://example.com/high/");
        cas.grantServiceTicket(
                tgt,
                service,
                newUserPassCredentials("bob", "bob"),
                new OneTimePasswordCredential("bob", "62831"));
    }

    private static UsernamePasswordCredential newUserPassCredentials(final String user, final String pass) {
        final UsernamePasswordCredential userpass = new UsernamePasswordCredential();
        userpass.setUsername(user);
        userpass.setPassword(pass);
        return userpass;
    }

    private static Service newService(final String id) {
        return new SimpleWebApplicationServiceImpl(id);
    }
}
