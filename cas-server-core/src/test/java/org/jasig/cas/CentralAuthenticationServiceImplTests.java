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

import java.util.Map;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.MixedPrincipalException;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.UnauthorizedServiceException;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.TicketState;
import org.jasig.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicy;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.jasig.cas.validation.Assertion;
import org.jasig.cas.validation.Cas20WithoutProxyingValidationSpecification;
import org.jasig.cas.validation.ValidationSpecification;
import org.junit.Test;

import static org.junit.Assert.*;

import static org.mockito.Mockito.mock;

/**
 * @author Scott Battaglia
 * @since 3.0
 */
public class CentralAuthenticationServiceImplTests extends AbstractCentralAuthenticationServiceTest {

    @Test(expected=AuthenticationException.class)
    public void testBadCredentialsOnTicketGrantingTicketCreation() throws Exception {
        getCentralAuthenticationService().createTicketGrantingTicket(
                TestUtils.getCredentialsWithDifferentUsernameAndPassword());
    }

    @Test
    public void testGoodCredentialsOnTicketGrantingTicketCreation() throws Exception {
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
    public void testDestroyTicketGrantingTicketWithValidTicket() throws Exception {
        final String ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        getCentralAuthenticationService().destroyTicketGrantingTicket(ticketId);
    }

    @Test(expected=ClassCastException.class)
    public void testDestroyTicketGrantingTicketWithInvalidTicket() throws Exception {
        final String ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String serviceTicketId = getCentralAuthenticationService()
            .grantServiceTicket(ticketId, TestUtils.getService());

        getCentralAuthenticationService().destroyTicketGrantingTicket(
                serviceTicketId);

    }

    @Test
    public void testGrantServiceTicketWithValidTicketGrantingTicket() throws Exception {
        final String ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        getCentralAuthenticationService().grantServiceTicket(ticketId,
            TestUtils.getService());
    }

    @Test(expected=TicketException.class)
    public void testGrantServiceTicketWithInvalidTicketGrantingTicket() throws Exception {
        final String ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        getCentralAuthenticationService().destroyTicketGrantingTicket(ticketId);
            getCentralAuthenticationService().grantServiceTicket(ticketId,
                TestUtils.getService());
    }

    @Test(expected=TicketException.class)
    public void testGrantServiceTicketWithExpiredTicketGrantingTicket() throws Exception {
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
    public void testDelegateTicketGrantingTicketWithProperParams() throws Exception {
        final String ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String serviceTicketId = getCentralAuthenticationService()
            .grantServiceTicket(ticketId, TestUtils.getService());
        getCentralAuthenticationService().delegateTicketGrantingTicket(
            serviceTicketId, TestUtils.getHttpBasedServiceCredentials());
    }

    @Test(expected=AuthenticationException.class)
    public void testDelegateTicketGrantingTicketWithBadCredentials() throws Exception {
        final String ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String serviceTicketId = getCentralAuthenticationService()
            .grantServiceTicket(ticketId, TestUtils.getService());

        getCentralAuthenticationService().delegateTicketGrantingTicket(
            serviceTicketId, TestUtils.getBadHttpBasedServiceCredentials());
    }

    @Test(expected=TicketException.class)
    public void testDelegateTicketGrantingTicketWithBadServiceTicket() throws Exception {
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
    public void testGrantServiceTicketWithValidCredentials() throws Exception {
        final String ticketGrantingTicket = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        getCentralAuthenticationService().grantServiceTicket(
            ticketGrantingTicket, TestUtils.getService(),
            TestUtils.getCredentialsWithSameUsernameAndPassword());
    }

    @Test(expected=AuthenticationException.class)
    public void testGrantServiceTicketWithInvalidCredentials() throws Exception {
        final String ticketGrantingTicket = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        getCentralAuthenticationService().grantServiceTicket(
            ticketGrantingTicket, TestUtils.getService(),
            TestUtils.getBadHttpBasedServiceCredentials());
    }

    @Test(expected=MixedPrincipalException.class)
    public void testGrantServiceTicketWithDifferentCredentials() throws Exception {
        final String ticketGrantingTicket = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword("testA"));
        getCentralAuthenticationService().grantServiceTicket(
            ticketGrantingTicket, TestUtils.getService(),
            TestUtils.getCredentialsWithSameUsernameAndPassword("testB"));
    }

    @Test
    public void testValidateServiceTicketWithExpires() throws Exception {
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
    public void testValidateServiceTicketWithValidService() throws Exception {
        final String ticketGrantingTicket = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String serviceTicket = getCentralAuthenticationService()
            .grantServiceTicket(ticketGrantingTicket, TestUtils.getService());

        getCentralAuthenticationService().validateServiceTicket(serviceTicket,
            TestUtils.getService());
    }

    @Test(expected=UnauthorizedServiceException.class)
    public void testValidateServiceTicketWithInvalidService() throws Exception {
        final String ticketGrantingTicket = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String serviceTicket = getCentralAuthenticationService()
            .grantServiceTicket(ticketGrantingTicket, TestUtils.getService());

        getCentralAuthenticationService().validateServiceTicket(
            serviceTicket, TestUtils.getService("test2"));
    }

    @Test(expected=TicketException.class)
    public void testValidateServiceTicketWithInvalidServiceTicket() throws Exception {
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
    public void testValidateServiceTicketNonExistantTicket() throws Exception {
        getCentralAuthenticationService().validateServiceTicket("test",
                TestUtils.getService());
    }

    @Test
    public void testValidateServiceTicketWithoutUsernameAttribute() throws Exception {
        UsernamePasswordCredential cred =  TestUtils.getCredentialsWithSameUsernameAndPassword();
        final String ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(cred);
        final String serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket,
                TestUtils.getService());

        final Assertion assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket,
                TestUtils.getService());
        final Authentication auth = assertion.getPrimaryAuthentication();
        assertEquals(auth.getPrincipal().getId(), cred.getUsername());
    }

    @Test
    public void testValidateServiceTicketWithDefaultUsernameAttribute() throws Exception {
        UsernamePasswordCredential cred =  TestUtils.getCredentialsWithSameUsernameAndPassword();
        final String ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(cred);

        Service svc = TestUtils.getService("testDefault");
        final String serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket, svc);

        final Assertion assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket, svc);
        final Authentication auth = assertion.getPrimaryAuthentication();
        assertEquals(auth.getPrincipal().getId(), cred.getUsername());
    }

    @Test
    public void testValidateServiceTicketWithUsernameAttribute() throws Exception {
        UsernamePasswordCredential cred =  TestUtils.getCredentialsWithSameUsernameAndPassword();
        final String ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(cred);

        Service svc = TestUtils.getService("eduPersonTest");
        final String serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket, svc);

        final Assertion assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket, svc);
        assertEquals("developer", assertion.getPrimaryAuthentication().getPrincipal().getId());
    }

    @Test
    public void testValidateServiceTicketNoAttributesReturned() throws Exception {
        final Service service = TestUtils.getService();
        final UsernamePasswordCredential cred =  TestUtils.getCredentialsWithSameUsernameAndPassword();
        final String ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(cred);
        final String serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket,
                service);

        final Assertion assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket,
                service);
        final Authentication auth = assertion.getPrimaryAuthentication();
        assertEquals(0, auth.getPrincipal().getAttributes().size());
    }

    @Test
    public void testValidateServiceTicketReturnAllAttributes() throws Exception {
        final Service service = TestUtils.getService("eduPersonTest");
        final UsernamePasswordCredential cred =  TestUtils.getCredentialsWithSameUsernameAndPassword();
        final String ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(cred);
        final String serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket,
                service);

        final Assertion assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket,
                service);
        final Authentication auth = assertion.getPrimaryAuthentication();
        assertEquals(3, auth.getPrincipal().getAttributes().size());
    }

    @Test
    public void testValidateServiceTicketReturnOnlyAllowedAttribute() throws Exception {
        final Service service = TestUtils.getService("eduPersonTestInvalid");
        final UsernamePasswordCredential cred =  TestUtils.getCredentialsWithSameUsernameAndPassword();
        final String ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(cred);
        final String serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket,
                service);

        final Assertion assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket,
                service);
        final Authentication auth = assertion.getPrimaryAuthentication();
        Map<String, Object> attributes = auth.getPrincipal().getAttributes();
        assertEquals(1, attributes.size());
        assertEquals("adopters", attributes.get("groupMembership"));
    }

    @Test
    public void testValidateServiceTicketAnonymous() throws Exception {
        final Service service = TestUtils.getService("testAnonymous");
        final UsernamePasswordCredential cred =  TestUtils.getCredentialsWithSameUsernameAndPassword();
        final String ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(cred);
        final String serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket,
                service);

        final Assertion assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket,
                service);
        final Authentication auth = assertion.getPrimaryAuthentication();
        assertNotEquals(cred.getUsername(), auth.getPrincipal().getId());
    }

    @Test
    public void testValidateServiceTicketWithInvalidUsernameAttribute() throws Exception {
        final UsernamePasswordCredential cred =  TestUtils.getCredentialsWithSameUsernameAndPassword();
        final String ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(cred);

        final Service svc = TestUtils.getService("eduPersonTestInvalid");
        final String serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket, svc);

        final Assertion assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket, svc);
        final Authentication auth = assertion.getPrimaryAuthentication();

        /*
         * The attribute specified for this service does not resolve.
         * Therefore, we expect the default to be returned.
         */
        assertEquals(auth.getPrincipal().getId(), cred.getUsername());
    }

    /**
     * This test simulates :
     * - a first authentication for a default service
     * - a second authentication with the renew parameter and the same service (and same credentials)
     * - a validation of the second ticket.
     * 
     * When supplemental authentications were returned with the chained authentications, the validation specification
     * failed as it only expects one authentication. Thus supplemental authentications should not be returned in the
     * chained authentications. Both concepts are orthogonal.
     *  
     * @throws TicketException
     * @throws AuthenticationException
     */
    @Test
    public void authenticateTwiceWithRenew() throws TicketException, AuthenticationException {
        final CentralAuthenticationService cas = getCentralAuthenticationService();
        final Service svc = TestUtils.getService("testDefault");
        final UsernamePasswordCredential goodCredential = TestUtils.getCredentialsWithSameUsernameAndPassword();
        final String tgtId = cas.createTicketGrantingTicket(goodCredential);
        cas.grantServiceTicket(tgtId, svc);
        // simulate renew with new good same credentials
        final String st2Id = cas.grantServiceTicket(tgtId, svc, goodCredential);
        final Assertion assertion = cas.validateServiceTicket(st2Id, svc);
        final ValidationSpecification validationSpecification = new Cas20WithoutProxyingValidationSpecification();
        assertTrue(validationSpecification.isSatisfiedBy(assertion));
    }
    
    /**
     * This test checks that the TGT destruction happens properly for a remote registry.
     * It previously failed when the deletion happens before the ticket was marked expired because an update was necessary for that.
     *
     * @throws AuthenticationException
     * @throws TicketException
     */
    @Test
    public void testDestroyRemoteRegistry() throws TicketException, AuthenticationException {
        final MockOnlyOneTicketRegistry registry = new MockOnlyOneTicketRegistry();
        final TicketGrantingTicketImpl tgt = new TicketGrantingTicketImpl("TGT-1", mock(Authentication.class),
                mock(ExpirationPolicy.class));
        final MockExpireUpdateTicketLogoutManager logoutManager = new MockExpireUpdateTicketLogoutManager(registry);
        // consider authentication has happened and the TGT is in the registry
        registry.addTicket(tgt);
        // create a new CASimpl
        final CentralAuthenticationServiceImpl cas = new CentralAuthenticationServiceImpl(registry,  null,  null, null, null, null, null,
                null, logoutManager);
        // destroy to mark expired and then delete : the opposite would fail with a "No ticket to update" error from the registry
        cas.destroyTicketGrantingTicket(tgt.getId());
    }
}
