package org.apereo.cas;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.exceptions.MixedPrincipalException;
import org.apereo.cas.authentication.principal.AbstractWebApplicationService;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.services.UnauthorizedSsoServiceException;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.util.MockOnlyOneTicketRegistry;
import org.apereo.cas.validation.Cas20WithoutProxyingValidationSpecification;
import org.apereo.cas.validation.CasProtocolValidationSpecification;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@DirtiesContext
@Slf4j
public class DefaultCentralAuthenticationServiceTests extends AbstractCentralAuthenticationServiceTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void verifyBadCredentialsOnTicketGrantingTicketCreation() {
        this.thrown.expect(AuthenticationException.class);
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(),
            CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword());
        getCentralAuthenticationService().createTicketGrantingTicket(ctx);
    }

    @Test
    public void verifyGoodCredentialsOnTicketGrantingTicketCreation() {
        try {
            final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport());
            assertNotNull(getCentralAuthenticationService().createTicketGrantingTicket(ctx));
        } catch (final AbstractTicketException e) {
            throw new AssertionError("Exception expected", e);
        }
    }

    @Test
    public void verifyDestroyTicketGrantingTicketWithNonExistingTicket() {
        getCentralAuthenticationService().destroyTicketGrantingTicket("test");
    }

    @Test
    public void verifyDestroyTicketGrantingTicketWithValidTicket() {
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport());
        final var ticketId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        getCentralAuthenticationService().destroyTicketGrantingTicket(ticketId.getId());
    }

    @Test
    public void verifyDisallowNullCredentialsWhenCreatingTicketGrantingTicket() {
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), new Credential[]{null});
        this.thrown.expect(RuntimeException.class);
        getCentralAuthenticationService().createTicketGrantingTicket(ctx);
    }

    @Test
    public void verifyDisallowNullCredentialsArrayWhenCreatingTicketGrantingTicket() {
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), new Credential[]{null, null});
        this.thrown.expect(RuntimeException.class);
        getCentralAuthenticationService().createTicketGrantingTicket(ctx);
    }

    @Test
    public void verifyDestroyTicketGrantingTicketWithInvalidTicket() {
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport());
        final var ticketId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final var serviceTicketId = getCentralAuthenticationService().grantServiceTicket(ticketId.getId(), getService(), ctx);

        this.thrown.expect(ClassCastException.class);
        getCentralAuthenticationService().destroyTicketGrantingTicket(serviceTicketId.getId());
    }

    @Test
    public void verifyGrantingOfServiceTicketUsingDefaultTicketIdGen() {
        final Service mockService = RegisteredServiceTestUtils.getService("testDefault");
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), mockService);
        final var ticketId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final var serviceTicketId = getCentralAuthenticationService().grantServiceTicket(ticketId.getId(), mockService, ctx);
        assertNotNull(serviceTicketId);
    }

    @Test
    public void verifyGrantServiceTicketWithValidTicketGrantingTicket() {
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport());
        final var ticketId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        getCentralAuthenticationService().grantServiceTicket(ticketId.getId(), getService(), ctx);
    }

    @Test
    public void verifyGrantServiceTicketFailsAuthzRule() {
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(),
            getService("TestServiceAttributeForAuthzFails"));

        this.thrown.expect(PrincipalException.class);


        final var ticketId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        getCentralAuthenticationService().grantServiceTicket(ticketId.getId(), getService("TestServiceAttributeForAuthzFails"), ctx);
    }

    @Test
    public void verifyGrantServiceTicketPassesAuthzRule() {
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(),
            getService("TestServiceAttributeForAuthzPasses"));
        final var ticketId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        getCentralAuthenticationService().grantServiceTicket(ticketId.getId(),
            getService("TestServiceAttributeForAuthzPasses"), ctx);
    }

    @Test
    public void verifyGrantProxyTicketWithValidTicketGrantingTicket() {
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport());
        final var ticketId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final var serviceTicketId = getCentralAuthenticationService().grantServiceTicket(ticketId.getId(), getService(), ctx);

        final var ctx2 = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(),
            RegisteredServiceTestUtils.getHttpBasedServiceCredentials());
        final TicketGrantingTicket pgt = getCentralAuthenticationService().createProxyGrantingTicket(serviceTicketId.getId(), ctx2);

        final var pt = getCentralAuthenticationService().grantProxyTicket(pgt.getId(), getService());
        assertTrue(pt.getId().startsWith(ProxyTicket.PROXY_TICKET_PREFIX));
    }

    @Test
    public void verifyGrantServiceTicketWithInvalidTicketGrantingTicket() {
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport());

        final var ticketId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        getCentralAuthenticationService().destroyTicketGrantingTicket(ticketId.getId());

        this.thrown.expect(AbstractTicketException.class);

        getCentralAuthenticationService().grantServiceTicket(ticketId.getId(), getService(), ctx);
    }

    @Test
    public void verifyDelegateTicketGrantingTicketWithProperParams() {
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), getService());
        final var ticketId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final var serviceTicketId = getCentralAuthenticationService().grantServiceTicket(ticketId.getId(), getService(), ctx);
        final var ctx2 = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(),
            RegisteredServiceTestUtils.getHttpBasedServiceCredentials());
        final TicketGrantingTicket pgt = getCentralAuthenticationService().createProxyGrantingTicket(serviceTicketId.getId(), ctx2);
        assertTrue(pgt.getId().startsWith(ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX));
    }

    @Test
    public void verifyProxyGrantingTicketHasRootAuthenticationAsPrincipal() {
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), getService());
        final var ticket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final var serviceTicketId = getCentralAuthenticationService().grantServiceTicket(ticket.getId(), getService(), ctx);

        final var service = AbstractWebApplicationService.class.cast(serviceTicketId.getService());
        assertEquals(service.getPrincipal(), ticket.getAuthentication().getPrincipal().getId());
    }

    @Test
    public void verifyDelegateTicketGrantingTicketWithBadServiceTicket() {
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), getService());

        final var ticketId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final var serviceTicketId = getCentralAuthenticationService().grantServiceTicket(ticketId.getId(), getService(), ctx);
        getCentralAuthenticationService().destroyTicketGrantingTicket(ticketId.getId());

        final var ctx2 = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(),
            RegisteredServiceTestUtils.getHttpBasedServiceCredentials());

        this.thrown.expect(AbstractTicketException.class);

        getCentralAuthenticationService().createProxyGrantingTicket(serviceTicketId.getId(), ctx2);
    }

    @Test
    public void verifyGrantServiceTicketWithValidCredentials() {
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), getService());

        final var ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), getService(), ctx);
    }

    @Test
    public void verifyGrantServiceTicketWithDifferentCredentials() {
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(),
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("testA"));
        final var ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);

        final var ctx2 = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(),
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("testB"));

        this.thrown.expect(MixedPrincipalException.class);
        getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), getService(), ctx2);
    }

    @Test
    public void verifyValidateServiceTicketWithValidService() {
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport());
        final var ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final var serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), getService(), ctx);

        getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), getService());
    }

    @Test
    public void verifyValidateServiceTicketWithInvalidService() {
        this.thrown.expect(UnauthorizedServiceException.class);
        final var service = getService("badtestservice");
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), service);

        final var ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);

        final var serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), service, ctx);
        getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), service);
    }

    @Test
    public void verifyValidateServiceTicketWithInvalidServiceTicket() {
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), getService());

        final var ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final var serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), getService(), ctx);
        getCentralAuthenticationService().destroyTicketGrantingTicket(ticketGrantingTicket.getId());

        this.thrown.expect(AbstractTicketException.class);

        getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), getService());
    }

    @Test
    public void verifyValidateServiceTicketNonExistantTicket() {
        this.thrown.expect(AbstractTicketException.class);
        getCentralAuthenticationService().validateServiceTicket("google", getService());
    }

    @Test
    public void verifyValidateServiceTicketWithoutUsernameAttribute() {
        final var cred = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), getService());

        final var ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final var serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), getService(), ctx);

        final var assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), getService());
        final var auth = assertion.getPrimaryAuthentication();
        assertEquals(auth.getPrincipal().getId(), cred.getUsername());
    }

    @Test
    public void verifyValidateServiceTicketWithDefaultUsernameAttribute() {
        final var svc = getService("testDefault");
        final var cred = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);
        final var ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);

        final var serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), svc, ctx);

        final var assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), svc);
        final var auth = assertion.getPrimaryAuthentication();
        assertEquals(auth.getPrincipal().getId(), cred.getUsername());
    }

    @Test
    public void verifyValidateServiceTicketWithUsernameAttribute() {
        final var svc = getService("eduPersonTest");

        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);
        final var ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);

        final var serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), svc, ctx);

        final var assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), svc);
        assertEquals("developer", assertion.getPrimaryAuthentication().getPrincipal().getId());
    }

    @Test
    public void verifyGrantServiceTicketWithCredsAndSsoFalse() {
        final var svc = getService("TestSsoFalse");
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);
        final var ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final var serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), svc, ctx);
        assertNotNull(serviceTicket);
    }

    @Test
    public void verifyGrantServiceTicketWithNoCredsAndSsoFalse() {
        final var svc = getService("TestSsoFalse");
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);
        final var ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        assertNotNull(getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), svc, ctx));
    }

    @Test
    public void verifyGrantServiceTicketWithNoCredsAndSsoFalseAndSsoFalse() {
        final var svc = getService("TestSsoFalse");
        final var ctx = mock(AuthenticationResult.class);
        when(ctx.getAuthentication()).thenReturn(CoreAuthenticationTestUtils.getAuthentication());
        when(ctx.isCredentialProvided()).thenReturn(true);
        final var ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final var service = getService("eduPersonTest");
        getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), service, ctx);

        this.thrown.expect(UnauthorizedSsoServiceException.class);
        when(ctx.isCredentialProvided()).thenReturn(false);
        getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), svc, ctx);
    }

    @Test
    public void verifyValidateServiceTicketNoAttributesReturned() {
        final var service = getService();
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), service);
        final var ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final var serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), service, ctx);

        final var assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), service);
        final var auth = assertion.getPrimaryAuthentication();
        assertEquals(0, auth.getPrincipal().getAttributes().size());
    }

    @Test
    public void verifyValidateServiceTicketReturnAllAttributes() {
        final var service = getService("eduPersonTest");
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), service);
        final var ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final var serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), service, ctx);

        final var assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), service);
        final var auth = assertion.getPrimaryAuthentication();
        assertEquals(3, auth.getPrincipal().getAttributes().size());
    }

    @Test
    public void verifyValidateServiceTicketReturnOnlyAllowedAttribute() {
        final var service = getService("eduPersonTestInvalid");
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), service);
        final var ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final var serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), service, ctx);

        final var assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), service);
        final var auth = assertion.getPrimaryAuthentication();
        final var attributes = auth.getPrincipal().getAttributes();
        assertEquals(1, attributes.size());
        assertEquals("adopters", attributes.get("groupMembership"));
    }

    @Test
    public void verifyValidateServiceTicketAnonymous() {
        final var service = getService("testAnonymous");
        final var cred = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), service);
        final var ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final var serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), service, ctx);

        final var assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), service);
        final var auth = assertion.getPrimaryAuthentication();
        assertNotEquals(cred.getUsername(), auth.getPrincipal().getId());
    }

    @Test
    public void verifyValidateServiceTicketWithInvalidUsernameAttribute() {
        final var svc = getService("eduPersonTestInvalid");
        final var cred = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);
        final var ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);

        final var serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), svc, ctx);

        final var assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), svc);
        final var auth = assertion.getPrimaryAuthentication();

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
        final var cas = getCentralAuthenticationService();
        final var svc = getService("testDefault");
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);
        final var tgtId = cas.createTicketGrantingTicket(ctx);
        cas.grantServiceTicket(tgtId.getId(), svc, ctx);
        // simulate renew with new good same credentials
        final var st2Id = cas.grantServiceTicket(tgtId.getId(), svc, ctx);
        final var assertion = cas.validateServiceTicket(st2Id.getId(), svc);
        final CasProtocolValidationSpecification validationSpecification = new Cas20WithoutProxyingValidationSpecification();
        assertTrue(validationSpecification.isSatisfiedBy(assertion, new MockHttpServletRequest()));
    }

    /**
     * This test checks that the TGT destruction happens properly for a remote registry.
     * It previously failed when the deletion happens before the ticket was marked expired because an update was necessary for that.
     */
    @Test
    public void verifyDestroyRemoteRegistry() throws AbstractTicketException, AuthenticationException {
        final var registry = new MockOnlyOneTicketRegistry();
        final var tgt = new TicketGrantingTicketImpl("TGT-1", mock(Authentication.class), mock(ExpirationPolicy.class));
        final var logoutManager = mock(LogoutManager.class);
        when(logoutManager.performLogout(any(TicketGrantingTicket.class)))
            .thenAnswer(invocation -> {
                tgt.markTicketExpired();
                registry.updateTicket(tgt);
                return null;
            });
        registry.addTicket(tgt);
        final var cas = new DefaultCentralAuthenticationService(
            mock(ApplicationEventPublisher.class), registry, null, logoutManager,
            null, null,
            null, null, null,
            mock(AuditableExecution.class));
        cas.destroyTicketGrantingTicket(tgt.getId());
    }

    private static Service getService(final String name) {
        final var request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, name);
        return new WebApplicationServiceFactory().createService(request);
    }

    private static Service getService() {
        return getService(CoreAuthenticationTestUtils.CONST_TEST_URL);
    }
}
