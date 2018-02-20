package org.apereo.cas;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.exceptions.MixedPrincipalException;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.AbstractWebApplicationService;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.services.UnauthorizedSsoServiceException;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.util.MockOnlyOneTicketRegistry;
import org.apereo.cas.validation.Assertion;
import org.apereo.cas.validation.Cas20WithoutProxyingValidationSpecification;
import org.apereo.cas.validation.CasProtocolValidationSpecification;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@DirtiesContext
public class CentralAuthenticationServiceImplTests extends AbstractCentralAuthenticationServiceTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void verifyBadCredentialsOnTicketGrantingTicketCreation() {
        this.thrown.expect(AuthenticationException.class);
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(),
                CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword());
        getCentralAuthenticationService().createTicketGrantingTicket(ctx);
    }

    @Test
    public void verifyGoodCredentialsOnTicketGrantingTicketCreation() {
        try {
            final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport());
            assertNotNull(getCentralAuthenticationService().createTicketGrantingTicket(ctx));
        } catch (final AbstractTicketException e) {
            fail("Exception expected");
        }
    }

    @Test
    public void verifyDestroyTicketGrantingTicketWithNonExistingTicket() {
        getCentralAuthenticationService().destroyTicketGrantingTicket("test");
    }

    @Test
    public void verifyDestroyTicketGrantingTicketWithValidTicket() {
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport());
        final TicketGrantingTicket ticketId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        getCentralAuthenticationService().destroyTicketGrantingTicket(ticketId.getId());
    }

    @Test
    public void verifyDisallowNullCredentialsWhenCreatingTicketGrantingTicket() {
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), new Credential[]{null});
        this.thrown.expect(RuntimeException.class);
        getCentralAuthenticationService().createTicketGrantingTicket(ctx);
    }

    @Test
    public void verifyDisallowNullCredentialsArrayWhenCreatingTicketGrantingTicket() {
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), new Credential[]{null, null});
        this.thrown.expect(RuntimeException.class);
        getCentralAuthenticationService().createTicketGrantingTicket(ctx);
    }

    @Test
    public void verifyDestroyTicketGrantingTicketWithInvalidTicket() {
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport());
        final TicketGrantingTicket ticketId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final ServiceTicket serviceTicketId = getCentralAuthenticationService().grantServiceTicket(ticketId.getId(), getService(), ctx);

        this.thrown.expect(ClassCastException.class);
        getCentralAuthenticationService().destroyTicketGrantingTicket(serviceTicketId.getId());
    }

    @Test
    public void verifyGrantingOfServiceTicketUsingDefaultTicketIdGen() {
        final Service mockService = CoreAuthenticationTestUtils.getService("testDefault");
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), mockService);
        final TicketGrantingTicket ticketId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final ServiceTicket serviceTicketId = getCentralAuthenticationService().grantServiceTicket(ticketId.getId(), mockService, ctx);
        assertNotNull(serviceTicketId);
    }

    @Test
    public void verifyGrantServiceTicketWithValidTicketGrantingTicket() {
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport());
        final TicketGrantingTicket ticketId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        getCentralAuthenticationService().grantServiceTicket(ticketId.getId(), getService(), ctx);
    }

    @Test
    public void verifyGrantServiceTicketFailsAuthzRule() {
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(),
                getService("TestServiceAttributeForAuthzFails"));

        this.thrown.expect(PrincipalException.class);
        this.thrown.expectMessage("screen.service.error.message");

        final TicketGrantingTicket ticketId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        getCentralAuthenticationService().grantServiceTicket(ticketId.getId(), getService("TestServiceAttributeForAuthzFails"), ctx);
    }

    @Test
    public void verifyGrantServiceTicketPassesAuthzRule() {
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(),
                getService("TestServiceAttributeForAuthzPasses"));
        final TicketGrantingTicket ticketId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        getCentralAuthenticationService().grantServiceTicket(ticketId.getId(),
                getService("TestServiceAttributeForAuthzPasses"), ctx);
    }

    @Test
    public void verifyGrantProxyTicketWithValidTicketGrantingTicket() {
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport());
        final TicketGrantingTicket ticketId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final ServiceTicket serviceTicketId = getCentralAuthenticationService().grantServiceTicket(ticketId.getId(), getService(), ctx);

        final AuthenticationResult ctx2 = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(),
                RegisteredServiceTestUtils.getHttpBasedServiceCredentials());
        final TicketGrantingTicket pgt = getCentralAuthenticationService().createProxyGrantingTicket(serviceTicketId.getId(), ctx2);

        final ProxyTicket pt = getCentralAuthenticationService().grantProxyTicket(pgt.getId(), getService());
        assertTrue(pt.getId().startsWith(ProxyTicket.PROXY_TICKET_PREFIX));
    }

    @Test
    public void verifyGrantServiceTicketWithInvalidTicketGrantingTicket() {
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport());

        final TicketGrantingTicket ticketId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        getCentralAuthenticationService().destroyTicketGrantingTicket(ticketId.getId());

        this.thrown.expect(AbstractTicketException.class);

        getCentralAuthenticationService().grantServiceTicket(ticketId.getId(), getService(), ctx);
    }

    @Test
    public void verifyDelegateTicketGrantingTicketWithProperParams() {
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), getService());
        final TicketGrantingTicket ticketId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final ServiceTicket serviceTicketId = getCentralAuthenticationService().grantServiceTicket(ticketId.getId(), getService(), ctx);
        final AuthenticationResult ctx2 = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(),
                RegisteredServiceTestUtils.getHttpBasedServiceCredentials());
        final TicketGrantingTicket pgt = getCentralAuthenticationService().createProxyGrantingTicket(serviceTicketId.getId(), ctx2);
        assertTrue(pgt.getId().startsWith(ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX));
    }

    @Test
    public void verifyProxyGrantingTicketHasRootAuthenticationAsPrincipal() {
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), getService());
        final TicketGrantingTicket ticket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final ServiceTicket serviceTicketId = getCentralAuthenticationService().grantServiceTicket(ticket.getId(), getService(), ctx);

        final AbstractWebApplicationService service = AbstractWebApplicationService.class.cast(serviceTicketId.getService());
        assertEquals(service.getPrincipal(), ticket.getAuthentication().getPrincipal().getId());
    }

    @Test
    public void verifyDelegateTicketGrantingTicketWithBadServiceTicket() {
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), getService());

        final TicketGrantingTicket ticketId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final ServiceTicket serviceTicketId = getCentralAuthenticationService().grantServiceTicket(ticketId.getId(), getService(), ctx);
        getCentralAuthenticationService().destroyTicketGrantingTicket(ticketId.getId());

        final AuthenticationResult ctx2 = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(),
                RegisteredServiceTestUtils.getHttpBasedServiceCredentials());

        this.thrown.expect(AbstractTicketException.class);

        getCentralAuthenticationService().createProxyGrantingTicket(serviceTicketId.getId(), ctx2);
    }

    @Test
    public void verifyGrantServiceTicketWithValidCredentials() {
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), getService());

        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), getService(), ctx);
    }

    @Test
    public void verifyGrantServiceTicketWithDifferentCredentials() {
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(),
                CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("testA"));
        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);

        final AuthenticationResult ctx2 = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(),
                CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("testB"));

        this.thrown.expect(MixedPrincipalException.class);
        this.thrown.expectMessage("testB != testA");

        getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), getService(), ctx2);
    }

    @Test
    public void verifyValidateServiceTicketWithValidService() {
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport());
        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final ServiceTicket serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), getService(), ctx);

        getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), getService());
    }

    @Test
    public void verifyValidateServiceTicketWithInvalidService() {
        this.thrown.expect(UnauthorizedServiceException.class);
        final Service service = getService("badtestservice");
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), service);

        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);

        final ServiceTicket serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), service, ctx);
        getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), service);
    }

    @Test
    public void verifyValidateServiceTicketWithInvalidServiceTicket() {
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), getService());

        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final ServiceTicket serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), getService(), ctx);
        getCentralAuthenticationService().destroyTicketGrantingTicket(ticketGrantingTicket.getId());

        this.thrown.expect(AbstractTicketException.class);

        getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), getService());
    }

    @Test
    public void verifyValidateServiceTicketNonExistantTicket() {
        this.thrown.expect(AbstractTicketException.class);
        this.thrown.expectMessage("google");

        getCentralAuthenticationService().validateServiceTicket("google", getService());
    }

    @Test
    public void verifyValidateServiceTicketWithoutUsernameAttribute() {
        final UsernamePasswordCredential cred = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), getService());

        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final ServiceTicket serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), getService(), ctx);

        final Assertion assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), getService());
        final Authentication auth = assertion.getPrimaryAuthentication();
        assertEquals(auth.getPrincipal().getId(), cred.getUsername());
    }

    @Test
    public void verifyValidateServiceTicketWithDefaultUsernameAttribute() {
        final Service svc = getService("testDefault");
        final UsernamePasswordCredential cred = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);
        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);

        final ServiceTicket serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), svc, ctx);

        final Assertion assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), svc);
        final Authentication auth = assertion.getPrimaryAuthentication();
        assertEquals(auth.getPrincipal().getId(), cred.getUsername());
    }

    @Test
    public void verifyValidateServiceTicketWithUsernameAttribute() {
        final Service svc = getService("eduPersonTest");

        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);
        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);

        final ServiceTicket serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), svc, ctx);

        final Assertion assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), svc);
        assertEquals("developer", assertion.getPrimaryAuthentication().getPrincipal().getId());
    }

    @Test
    public void verifyGrantServiceTicketWithCredsAndSsoFalse() {
        final Service svc = getService("TestSsoFalse");
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);
        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final ServiceTicket serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), svc, ctx);
        assertNotNull(serviceTicket);
    }

    @Test
    public void verifyGrantServiceTicketWithNoCredsAndSsoFalse() {
        final Service svc = getService("TestSsoFalse");
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);
        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        assertNotNull(getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), svc, ctx));
    }

    @Test
    public void verifyGrantServiceTicketWithNoCredsAndSsoFalseAndSsoFalse() {
        final Service svc = getService("TestSsoFalse");
        final AuthenticationResult ctx = mock(AuthenticationResult.class);
        when(ctx.getAuthentication()).thenReturn(CoreAuthenticationTestUtils.getAuthentication());
        when(ctx.isCredentialProvided()).thenReturn(true);
        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final Service service = getService("eduPersonTest");
        getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), service, ctx);

        this.thrown.expect(UnauthorizedSsoServiceException.class);
        when(ctx.isCredentialProvided()).thenReturn(false);
        getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), svc, ctx);
    }

    @Test
    public void verifyValidateServiceTicketNoAttributesReturned() {
        final Service service = getService();
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), service);
        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final ServiceTicket serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), service, ctx);

        final Assertion assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), service);
        final Authentication auth = assertion.getPrimaryAuthentication();
        assertEquals(0, auth.getPrincipal().getAttributes().size());
    }

    @Test
    public void verifyValidateServiceTicketReturnAllAttributes() {
        final Service service = getService("eduPersonTest");
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), service);
        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final ServiceTicket serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), service, ctx);

        final Assertion assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), service);
        final Authentication auth = assertion.getPrimaryAuthentication();
        assertEquals(3, auth.getPrincipal().getAttributes().size());
    }

    @Test
    public void verifyValidateServiceTicketReturnOnlyAllowedAttribute() {
        final Service service = getService("eduPersonTestInvalid");
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), service);
        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final ServiceTicket serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), service, ctx);

        final Assertion assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), service);
        final Authentication auth = assertion.getPrimaryAuthentication();
        final Map<String, Object> attributes = auth.getPrincipal().getAttributes();
        assertEquals(1, attributes.size());
        assertEquals("adopters", attributes.get("groupMembership"));
    }

    @Test
    public void verifyValidateServiceTicketAnonymous() {
        final Service service = getService("testAnonymous");
        final UsernamePasswordCredential cred = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), service);
        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final ServiceTicket serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), service, ctx);

        final Assertion assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), service);
        final Authentication auth = assertion.getPrimaryAuthentication();
        assertNotEquals(cred.getUsername(), auth.getPrincipal().getId());
    }

    @Test
    public void verifyValidateServiceTicketWithInvalidUsernameAttribute() {
        final Service svc = getService("eduPersonTestInvalid");
        final UsernamePasswordCredential cred = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);
        final TicketGrantingTicket ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);

        final ServiceTicket serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), svc, ctx);

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
     */
    @Test
    public void verifyAuthenticateTwiceWithRenew() throws AbstractTicketException, AuthenticationException {
        final CentralAuthenticationService cas = getCentralAuthenticationService();
        final Service svc = getService("testDefault");
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);
        final TicketGrantingTicket tgtId = cas.createTicketGrantingTicket(ctx);
        cas.grantServiceTicket(tgtId.getId(), svc, ctx);
        // simulate renew with new good same credentials
        final ServiceTicket st2Id = cas.grantServiceTicket(tgtId.getId(), svc, ctx);
        final Assertion assertion = cas.validateServiceTicket(st2Id.getId(), svc);
        final CasProtocolValidationSpecification validationSpecification = new Cas20WithoutProxyingValidationSpecification();
        assertTrue(validationSpecification.isSatisfiedBy(assertion, new MockHttpServletRequest()));
    }

    /**
     * This test checks that the TGT destruction happens properly for a remote registry.
     * It previously failed when the deletion happens before the ticket was marked expired because an update was necessary for that.
     */
    @Test
    public void verifyDestroyRemoteRegistry() throws AbstractTicketException, AuthenticationException {
        final MockOnlyOneTicketRegistry registry = new MockOnlyOneTicketRegistry();
        final TicketGrantingTicketImpl tgt = new TicketGrantingTicketImpl("TGT-1", mock(Authentication.class), mock(ExpirationPolicy.class));
        final MockExpireUpdateTicketLogoutManager logoutManager = new MockExpireUpdateTicketLogoutManager(registry);
        registry.addTicket(tgt);
        final DefaultCentralAuthenticationService cas = new DefaultCentralAuthenticationService(registry, null, null, logoutManager, null, null, null, null);
        cas.setApplicationEventPublisher(mock(ApplicationEventPublisher.class));
        cas.destroyTicketGrantingTicket(tgt.getId());
    }

    private static Service getService(final String name) {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, name);
        return new WebApplicationServiceFactory().createService(request);
    }

    private static Service getService() {
        return getService(CoreAuthenticationTestUtils.CONST_TEST_URL);
    }
}
