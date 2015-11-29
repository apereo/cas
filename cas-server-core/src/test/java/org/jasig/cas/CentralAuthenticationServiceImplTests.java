package org.jasig.cas;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.MixedPrincipalException;
import org.jasig.cas.authentication.TestUtils;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.WebApplicationServiceFactory;
import org.jasig.cas.services.UnauthorizedServiceException;
import org.jasig.cas.services.UnauthorizedServiceForPrincipalException;
import org.jasig.cas.services.UnauthorizedSsoServiceException;
import org.jasig.cas.ticket.AbstractTicketException;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.ProxyGrantingTicket;
import org.jasig.cas.ticket.ProxyTicket;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketCreationException;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicy;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.jasig.cas.validation.Assertion;
import org.jasig.cas.validation.Cas20WithoutProxyingValidationSpecification;
import org.jasig.cas.validation.ValidationSpecification;
import org.junit.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class CentralAuthenticationServiceImplTests extends AbstractCentralAuthenticationServiceTest {

    @Test(expected = AuthenticationException.class)
    public void verifyBadCredentialsOnTicketGrantingTicketCreation() throws Exception {
        getCentralAuthenticationService().createTicketGrantingTicket(
            TestUtils.getCredentialsWithDifferentUsernameAndPassword());
    }

    @Test
    public void verifyGoodCredentialsOnTicketGrantingTicketCreation() throws Exception {
        try {
            assertNotNull(getCentralAuthenticationService()
                .createTicketGrantingTicket(
                    TestUtils.getCredentialsWithSameUsernameAndPassword()));
        } catch (final AbstractTicketException e) {
            fail("Exception expected");
        }
    }

    @Test
    public void verifyDestroyTicketGrantingTicketWithNonExistantTicket() {
        getCentralAuthenticationService().destroyTicketGrantingTicket("test");
    }

    @Test
    public void verifyDestroyTicketGrantingTicketWithValidTicket() throws Exception {
        final TicketGrantingTicket ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        getCentralAuthenticationService().destroyTicketGrantingTicket(ticketId.getId());
    }

    @Test(expected = TicketCreationException.class)
    public void disallowNullCredentionalsWhenCreatingTicketGrantingTicket() throws Exception {
        final TicketGrantingTicket ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(new Credential[] {null});

    }

    @Test(expected = TicketCreationException.class)
    public void disallowNullCredentionalsArrayWhenCreatingTicketGrantingTicket() throws Exception {
        final TicketGrantingTicket ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(new Credential[]{null, null});
    }

    @Test(expected = ClassCastException.class)
    public void verifyDestroyTicketGrantingTicketWithInvalidTicket() throws Exception {
        final TicketGrantingTicket ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final ServiceTicket serviceTicketId = getCentralAuthenticationService()
            .grantServiceTicket(ticketId.getId(), getService());

        getCentralAuthenticationService().destroyTicketGrantingTicket(
            serviceTicketId.getId());

    }

    @Test
    public void checkGrantingOfServiceTicketUsingDefaultTicketIdGen() throws Exception {
        final Service mockService = mock(Service.class);
        when(mockService.getId()).thenReturn("testDefault");
        final TicketGrantingTicket ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final ServiceTicket serviceTicketId = getCentralAuthenticationService()
            .grantServiceTicket(ticketId.getId(), mockService);
        assertNotNull(serviceTicketId);
    }

    @Test
    public void verifyGrantServiceTicketWithValidTicketGrantingTicket() throws Exception {
        final TicketGrantingTicket ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        getCentralAuthenticationService().grantServiceTicket(ticketId.getId(),
            getService());
    }

    @Test(expected = UnauthorizedServiceForPrincipalException.class)
    public void verifyGrantServiceTicketFailsAuthzRule() throws Exception {
        final TicketGrantingTicket ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        getCentralAuthenticationService().grantServiceTicket(ticketId.getId(),
            getService("TestServiceAttributeForAuthzFails"));
    }

    @Test
    public void verifyGrantServiceTicketPassesAuthzRule() throws Exception {
        final TicketGrantingTicket ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        getCentralAuthenticationService().grantServiceTicket(ticketId.getId(),
            getService("TestServiceAttributeForAuthzPasses"));
    }

    @Test
    public void verifyGrantProxyTicketWithValidTicketGrantingTicket() throws Exception {
        final TicketGrantingTicket ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final ServiceTicket serviceTicketId = getCentralAuthenticationService()
            .grantServiceTicket(ticketId.getId(), getService());
        final TicketGrantingTicket pgt = getCentralAuthenticationService().createProxyGrantingTicket(
            serviceTicketId.getId(), org.jasig.cas.services.TestUtils.getHttpBasedServiceCredentials());

        final ProxyTicket pt = getCentralAuthenticationService().grantProxyTicket(pgt.getId(),
                getService());
        assertTrue(pt.getId().startsWith(ProxyTicket.PROXY_TICKET_PREFIX));
    }

    @Test(expected = AbstractTicketException.class)
    public void verifyGrantServiceTicketWithInvalidTicketGrantingTicket() throws Exception {
        final TicketGrantingTicket ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        getCentralAuthenticationService().destroyTicketGrantingTicket(ticketId.getId());
        getCentralAuthenticationService().grantServiceTicket(ticketId.getId(),
            getService());
    }

    @Test
    public void verifyDelegateTicketGrantingTicketWithProperParams() throws Exception {
        final TicketGrantingTicket ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final ServiceTicket serviceTicketId = getCentralAuthenticationService()
            .grantServiceTicket(ticketId.getId(), getService());
        final TicketGrantingTicket pgt = getCentralAuthenticationService().createProxyGrantingTicket(
            serviceTicketId.getId(), org.jasig.cas.services.TestUtils.getHttpBasedServiceCredentials());
        assertTrue(pgt.getId().startsWith(ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX));
    }

    @Test(expected = AbstractTicketException.class)
    public void verifyDelegateTicketGrantingTicketWithBadServiceTicket() throws Exception {
        final TicketGrantingTicket ticketId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final ServiceTicket serviceTicketId = getCentralAuthenticationService()
            .grantServiceTicket(ticketId.getId(), getService());
        getCentralAuthenticationService().destroyTicketGrantingTicket(ticketId.getId());
        getCentralAuthenticationService().createProxyGrantingTicket(
            serviceTicketId.getId(), org.jasig.cas.services.TestUtils.getHttpBasedServiceCredentials());
    }

    @Test
    public void verifyGrantServiceTicketWithValidCredentials() throws Exception {
        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        getCentralAuthenticationService().grantServiceTicket(
            ticketGrantingTicket.getId(), getService(),
            TestUtils.getCredentialsWithSameUsernameAndPassword());
    }

    @Test(expected = MixedPrincipalException.class)
    public void verifyGrantServiceTicketWithDifferentCredentials() throws Exception {
        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword("testA"));
        getCentralAuthenticationService().grantServiceTicket(
            ticketGrantingTicket.getId(), getService(),
            TestUtils.getCredentialsWithSameUsernameAndPassword("testB"));
    }

    @Test
    public void verifyValidateServiceTicketWithExpires() throws Exception {
        ((CentralAuthenticationServiceImpl) getCentralAuthenticationService())
            .setServiceTicketExpirationPolicy(new MultiTimeUseOrTimeoutExpirationPolicy(
                1, 1100));
        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final ServiceTicket serviceTicket = getCentralAuthenticationService()
            .grantServiceTicket(ticketGrantingTicket.getId(), getService());

        getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(),
            getService());

        assertFalse(getTicketRegistry().deleteTicket(serviceTicket.getId()));
        ((CentralAuthenticationServiceImpl) getCentralAuthenticationService())
            .setServiceTicketExpirationPolicy(new NeverExpiresExpirationPolicy());
    }

    @Test
    public void verifyValidateServiceTicketWithValidService() throws Exception {
        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final ServiceTicket serviceTicket = getCentralAuthenticationService()
            .grantServiceTicket(ticketGrantingTicket.getId(), getService());

        getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(),
            getService());
    }

    @Test(expected = UnauthorizedServiceException.class)
    public void verifyValidateServiceTicketWithInvalidService() throws Exception {
        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final ServiceTicket serviceTicket = getCentralAuthenticationService()
            .grantServiceTicket(ticketGrantingTicket.getId(), getService());

        getCentralAuthenticationService().validateServiceTicket(
            serviceTicket.getId(), getService("test2"));
    }

    @Test(expected = AbstractTicketException.class)
    public void verifyValidateServiceTicketWithInvalidServiceTicket() throws Exception {
        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final ServiceTicket serviceTicket = getCentralAuthenticationService()
            .grantServiceTicket(ticketGrantingTicket.getId(), getService());
        getCentralAuthenticationService().destroyTicketGrantingTicket(
            ticketGrantingTicket.getId());

        getCentralAuthenticationService().validateServiceTicket(
            serviceTicket.getId(), getService());
    }

    @Test(expected = AbstractTicketException.class)
    public void verifyValidateServiceTicketNonExistantTicket() throws Exception {
        getCentralAuthenticationService().validateServiceTicket("google",
            getService());
    }

    @Test
    public void verifyValidateServiceTicketWithoutUsernameAttribute() throws Exception {
        final UsernamePasswordCredential cred = TestUtils.getCredentialsWithSameUsernameAndPassword();
        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(cred);
        final ServiceTicket serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(),
            getService());

        final Assertion assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(),
            getService());
        final Authentication auth = assertion.getPrimaryAuthentication();
        assertEquals(auth.getPrincipal().getId(), cred.getUsername());
    }

    @Test
    public void verifyValidateServiceTicketWithDefaultUsernameAttribute() throws Exception {
        final UsernamePasswordCredential cred = TestUtils.getCredentialsWithSameUsernameAndPassword();
        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(cred);

        final Service svc = getService("testDefault");
        final ServiceTicket serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), svc);

        final Assertion assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), svc);
        final Authentication auth = assertion.getPrimaryAuthentication();
        assertEquals(auth.getPrincipal().getId(), cred.getUsername());
    }

    @Test
    public void verifyValidateServiceTicketWithUsernameAttribute() throws Exception {
        final UsernamePasswordCredential cred = TestUtils.getCredentialsWithSameUsernameAndPassword();
        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(cred);

        final Service svc = getService("eduPersonTest");
        final ServiceTicket serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), svc);

        final Assertion assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), svc);
        assertEquals("developer", assertion.getPrimaryAuthentication().getPrincipal().getId());
    }

    @Test
    public void verifyGrantServiceTicketWithCredsAndSsoFalse() throws Exception {
        final UsernamePasswordCredential cred = TestUtils.getCredentialsWithSameUsernameAndPassword();
        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(cred);

        final Service svc = getService("TestSsoFalse");
        final ServiceTicket serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), svc, cred);
        assertNotNull(serviceTicket);
    }

    @Test(expected = UnauthorizedSsoServiceException.class)
    public void verifyGrantServiceTicketWithNoCredsAndSsoFalse() throws Exception {
        final UsernamePasswordCredential cred = TestUtils.getCredentialsWithSameUsernameAndPassword();
        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(cred);

        final Service svc = getService("TestSsoFalse");
        getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), svc,
                new Credential[] {null});
    }

    @Test
    public void verifyValidateServiceTicketNoAttributesReturned() throws Exception {
        final Service service = getService();
        final UsernamePasswordCredential cred = TestUtils.getCredentialsWithSameUsernameAndPassword();
        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(cred);
        final ServiceTicket serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(),
            service);

        final Assertion assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(),
            service);
        final Authentication auth = assertion.getPrimaryAuthentication();
        assertEquals(0, auth.getPrincipal().getAttributes().size());
    }

    @Test
    public void verifyValidateServiceTicketReturnAllAttributes() throws Exception {
        final Service service = getService("eduPersonTest");
        final UsernamePasswordCredential cred = TestUtils.getCredentialsWithSameUsernameAndPassword();
        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(cred);
        final ServiceTicket serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(),
            service);

        final Assertion assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(),
            service);
        final Authentication auth = assertion.getPrimaryAuthentication();
        assertEquals(3, auth.getPrincipal().getAttributes().size());
    }

    @Test
    public void verifyValidateServiceTicketReturnOnlyAllowedAttribute() throws Exception {
        final Service service = getService("eduPersonTestInvalid");
        final UsernamePasswordCredential cred = TestUtils.getCredentialsWithSameUsernameAndPassword();
        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(cred);
        final ServiceTicket serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(),
            service);

        final Assertion assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(),
            service);
        final Authentication auth = assertion.getPrimaryAuthentication();
        final Map<String, Object> attributes = auth.getPrincipal().getAttributes();
        assertEquals(1, attributes.size());
        assertEquals("adopters", attributes.get("groupMembership"));
    }

    @Test
    public void verifyValidateServiceTicketAnonymous() throws Exception {
        final Service service = getService("testAnonymous");
        final UsernamePasswordCredential cred = TestUtils.getCredentialsWithSameUsernameAndPassword();
        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(cred);
        final ServiceTicket serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(),
            service);

        final Assertion assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(),
            service);
        final Authentication auth = assertion.getPrimaryAuthentication();
        assertNotEquals(cred.getUsername(), auth.getPrincipal().getId());
    }

    @Test
    public void verifyValidateServiceTicketWithInvalidUsernameAttribute() throws Exception {
        final UsernamePasswordCredential cred = TestUtils.getCredentialsWithSameUsernameAndPassword();
        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(cred);

        final Service svc = getService("eduPersonTestInvalid");
        final ServiceTicket serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), svc);

        final Assertion assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), svc);
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
     * When supplemental authentications were returned with the chained authentications, the validation specification
     * failed as it only expects one authentication. Thus supplemental authentications should not be returned in the
     * chained authentications. Both concepts are orthogonal.
     *
     * @throws AbstractTicketException
     * @throws AuthenticationException
     */
    @Test
    public void authenticateTwiceWithRenew() throws AbstractTicketException, AuthenticationException {
        final CentralAuthenticationService cas = getCentralAuthenticationService();
        final Service svc = getService("testDefault");
        final UsernamePasswordCredential goodCredential = TestUtils.getCredentialsWithSameUsernameAndPassword();
        final TicketGrantingTicket tgtId = cas.createTicketGrantingTicket(goodCredential);
        cas.grantServiceTicket(tgtId.getId(), svc);
        // simulate renew with new good same credentials
        final ServiceTicket st2Id = cas.grantServiceTicket(tgtId.getId(), svc, goodCredential);
        final Assertion assertion = cas.validateServiceTicket(st2Id.getId(), svc);
        final ValidationSpecification validationSpecification = new Cas20WithoutProxyingValidationSpecification();
        assertTrue(validationSpecification.isSatisfiedBy(assertion));
    }

    /**
     * This test checks that the TGT destruction happens properly for a remote registry.
     * It previously failed when the deletion happens before the ticket was marked expired because an update was necessary for that.
     *
     * @throws AuthenticationException
     * @throws AbstractTicketException
     */
    @Test
    public void verifyDestroyRemoteRegistry() throws AbstractTicketException, AuthenticationException {
        final MockOnlyOneTicketRegistry registry = new MockOnlyOneTicketRegistry();
        final TicketGrantingTicketImpl tgt = new TicketGrantingTicketImpl("TGT-1", mock(Authentication.class),
            mock(ExpirationPolicy.class));
        final MockExpireUpdateTicketLogoutManager logoutManager = new MockExpireUpdateTicketLogoutManager(registry);
        // consider authentication has happened and the TGT is in the registry
        registry.addTicket(tgt);
        // create a new CASimpl
        final CentralAuthenticationServiceImpl cas = new CentralAuthenticationServiceImpl(registry, null, null, null, null,
                null, logoutManager);
        cas.setApplicationEventPublisher(mock(ApplicationEventPublisher.class));
        // destroy to mark expired and then delete : the opposite would fail with a "No ticket to update" error from the registry
        cas.destroyTicketGrantingTicket(tgt.getId());
    }

    private Service getService(final String name) {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", name);
        return new WebApplicationServiceFactory().createService(request);
    }

    private Service getService() {
        return getService(TestUtils.CONST_TEST_URL);
    }
}
