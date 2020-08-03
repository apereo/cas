package org.apereo.cas;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.exceptions.MixedPrincipalException;
import org.apereo.cas.authentication.principal.AbstractWebApplicationService;
import org.apereo.cas.authentication.principal.DefaultServiceMatchingStrategy;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.services.UnauthorizedSsoServiceException;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.util.MockOnlyOneTicketRegistry;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.validation.Cas20WithoutProxyingValidationSpecification;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("Simple")
public class DefaultCentralAuthenticationServiceTests extends AbstractCentralAuthenticationServiceTests {

    @Test
    public void verifyBadCredentialsOnTicketGrantingTicketCreation() {
        assertThrows(AuthenticationException.class, () -> CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(),
            CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword()));
    }

    @Test
    public void verifyGoodCredentialsOnTicketGrantingTicketCreation() {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport());
        assertNotNull(getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx));
    }

    @Test
    public void verifyDestroyTicketGrantingTicketWithNonExistingTicket() {
        getCentralAuthenticationService().getObject().destroyTicketGrantingTicket("test");
    }

    @Test
    public void verifyDestroyTicketGrantingTicketWithValidTicket() {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport());
        val ticketId = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);
        getCentralAuthenticationService().getObject().destroyTicketGrantingTicket(ticketId.getId());
    }

    @Test
    public void verifyDisallowNullCredentialsWhenCreatingTicketGrantingTicket() {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), new Credential[]{null});
        assertThrows(RuntimeException.class, () -> getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx));
    }

    @Test
    public void verifyDisallowNullCredentialsArrayWhenCreatingTicketGrantingTicket() {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), new Credential[]{null, null});
        assertThrows(RuntimeException.class, () -> getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx));
    }

    @Test
    public void verifyDestroyTicketGrantingTicketWithInvalidTicket() {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport());
        val ticketId = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);
        val serviceTicketId = getCentralAuthenticationService().getObject().grantServiceTicket(ticketId.getId(), getService(), ctx);

        assertThrows(ClassCastException.class, () -> getCentralAuthenticationService().getObject().destroyTicketGrantingTicket(serviceTicketId.getId()));
    }

    @Test
    public void verifyGrantingOfServiceTicketUsingDefaultTicketIdGen() {
        val mockService = RegisteredServiceTestUtils.getService("testDefault");
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), mockService);
        val ticketId = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);
        val serviceTicketId = getCentralAuthenticationService().getObject().grantServiceTicket(ticketId.getId(), mockService, ctx);
        assertNotNull(serviceTicketId);
    }

    @Test
    public void verifyGrantServiceTicketWithValidTicketGrantingTicket() {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport());
        val ticketId = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);
        getCentralAuthenticationService().getObject().grantServiceTicket(ticketId.getId(), getService(), ctx);
    }

    @Test
    public void verifyGrantServiceTicketFailsAuthzRule() {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(),
            getService("TestServiceAttributeForAuthzFails"));

        assertThrows(PrincipalException.class, () -> getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx));
    }

    @Test
    public void verifyGrantServiceTicketPassesAuthzRule() {
        val service = getService("TestServiceAttributeForAuthzPasses");
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), service);
        val ticketId = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);
        getCentralAuthenticationService().getObject().grantServiceTicket(ticketId.getId(), service, ctx);
    }

    @Test
    public void verifyGrantProxyTicketWithValidTicketGrantingTicket() {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport());
        val ticketId = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);
        val serviceTicketId = getCentralAuthenticationService().getObject().grantServiceTicket(ticketId.getId(), getService(), ctx);

        val ctx2 = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(),
            RegisteredServiceTestUtils.getHttpBasedServiceCredentials());
        val pgt = getCentralAuthenticationService().getObject().createProxyGrantingTicket(serviceTicketId.getId(), ctx2);

        val pt = getCentralAuthenticationService().getObject().grantProxyTicket(pgt.getId(), getService());
        assertTrue(pt.getId().startsWith(ProxyTicket.PROXY_TICKET_PREFIX));
    }

    @Test
    public void verifyGrantServiceTicketWithInvalidTicketGrantingTicket() {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport());

        val ticketId = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);
        getCentralAuthenticationService().getObject().destroyTicketGrantingTicket(ticketId.getId());

        assertThrows(AbstractTicketException.class, () -> getCentralAuthenticationService().getObject().grantServiceTicket(ticketId.getId(), getService(), ctx));
    }

    @Test
    public void verifyDelegateTicketGrantingTicketWithProperParams() {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), getService());
        val ticketId = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);
        val serviceTicketId = getCentralAuthenticationService().getObject().grantServiceTicket(ticketId.getId(), getService(), ctx);
        val ctx2 = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(),
            RegisteredServiceTestUtils.getHttpBasedServiceCredentials());
        val pgt = getCentralAuthenticationService().getObject().createProxyGrantingTicket(serviceTicketId.getId(), ctx2);
        assertTrue(pgt.getId().startsWith(ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX));
    }

    @Test
    public void verifyProxyGrantingTicketHasRootAuthenticationAsPrincipal() {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), getService());
        val ticket = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);
        val serviceTicketId = getCentralAuthenticationService().getObject().grantServiceTicket(ticket.getId(), getService(), ctx);

        val service = (AbstractWebApplicationService) serviceTicketId.getService();
        assertEquals(service.getPrincipal(), ticket.getAuthentication().getPrincipal().getId());
    }

    @Test
    public void verifyDelegateTicketGrantingTicketWithBadServiceTicket() {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), getService());

        val ticketId = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);
        val serviceTicketId = getCentralAuthenticationService().getObject().grantServiceTicket(ticketId.getId(), getService(), ctx);
        getCentralAuthenticationService().getObject().destroyTicketGrantingTicket(ticketId.getId());

        val ctx2 = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(),
            RegisteredServiceTestUtils.getHttpBasedServiceCredentials());

        assertThrows(AbstractTicketException.class, () -> getCentralAuthenticationService().getObject().createProxyGrantingTicket(serviceTicketId.getId(), ctx2));
    }

    @Test
    public void verifyGrantServiceTicketWithValidCredentials() {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), getService());

        val ticketGrantingTicket = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);
        getCentralAuthenticationService().getObject().grantServiceTicket(ticketGrantingTicket.getId(), getService(), ctx);
    }

    @Test
    public void verifyGrantServiceTicketWithDifferentCredentials() {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(),
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("testA"));
        val ticketGrantingTicket = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);

        val ctx2 = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(),
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("testB"));

        assertThrows(MixedPrincipalException.class,
            () -> getCentralAuthenticationService().getObject().grantServiceTicket(ticketGrantingTicket.getId(), getService(), ctx2));
    }

    @Test
    public void verifyValidateServiceTicketWithValidService() {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport());
        val ticketGrantingTicket = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);
        val serviceTicket = getCentralAuthenticationService().getObject().grantServiceTicket(ticketGrantingTicket.getId(), getService(), ctx);

        getCentralAuthenticationService().getObject().validateServiceTicket(serviceTicket.getId(), getService());
    }

    @Test
    public void verifyValidateServiceTicketWithInvalidService() {
        val service = getService("badtestservice");
        assertThrows(UnauthorizedServiceException.class, () -> CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), service));
    }

    @Test
    public void verifyValidateServiceTicketWithInvalidServiceTicket() {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), getService());

        val ticketGrantingTicket = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);
        val serviceTicket = getCentralAuthenticationService().getObject().grantServiceTicket(ticketGrantingTicket.getId(), getService(), ctx);
        getCentralAuthenticationService().getObject().destroyTicketGrantingTicket(ticketGrantingTicket.getId());

        assertThrows(AbstractTicketException.class, () -> getCentralAuthenticationService().getObject().validateServiceTicket(serviceTicket.getId(), getService()));
    }

    @Test
    public void verifyValidateServiceTicketNonExistantTicket() {
        assertThrows(AbstractTicketException.class, () -> getCentralAuthenticationService().getObject().validateServiceTicket("google", getService()));
    }

    @Test
    public void verifyValidateServiceTicketWithoutUsernameAttribute() {
        val cred = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), getService());

        val ticketGrantingTicket = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);
        val serviceTicket = getCentralAuthenticationService().getObject().grantServiceTicket(ticketGrantingTicket.getId(), getService(), ctx);

        val assertion = getCentralAuthenticationService().getObject().validateServiceTicket(serviceTicket.getId(), getService());
        val auth = assertion.getPrimaryAuthentication();
        assertEquals(auth.getPrincipal().getId(), cred.getUsername());
    }

    @Test
    public void verifyValidateServiceTicketWithDefaultUsernameAttribute() {
        val svc = getService("testDefault");
        val cred = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);
        val ticketGrantingTicket = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);

        val serviceTicket = getCentralAuthenticationService().getObject().grantServiceTicket(ticketGrantingTicket.getId(), svc, ctx);

        val assertion = getCentralAuthenticationService().getObject().validateServiceTicket(serviceTicket.getId(), svc);
        val auth = assertion.getPrimaryAuthentication();
        assertEquals(auth.getPrincipal().getId(), cred.getUsername());
    }

    @Test
    public void verifyTicketState() {
        val svc = getService("testDefault");
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);
        val ticketGrantingTicket = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);
        var result = getCentralAuthenticationService().getObject().getTicket(ticketGrantingTicket.getId());
        assertEquals(result, ticketGrantingTicket);
        result.markTicketExpired();
        result = getCentralAuthenticationService().getObject().updateTicket(result);
        assertTrue(result.isExpired());
        assertThrows(InvalidTicketException.class, () -> getCentralAuthenticationService().getObject().getTicket(ticketGrantingTicket.getId()));
    }

    @Test
    public void verifyValidateServiceTicketWithUsernameAttribute() {
        val svc = getService("eduPersonTest");

        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);
        val ticketGrantingTicket = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);

        val serviceTicket = getCentralAuthenticationService().getObject().grantServiceTicket(ticketGrantingTicket.getId(), svc, ctx);

        val assertion = getCentralAuthenticationService().getObject().validateServiceTicket(serviceTicket.getId(), svc);
        assertEquals("developer", assertion.getPrimaryAuthentication().getPrincipal().getId());
    }

    @Test
    public void verifyGrantServiceTicketWithCredsAndSsoFalse() {
        val svc = getService("TestSsoFalse");
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);
        val ticketGrantingTicket = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);
        val serviceTicket = getCentralAuthenticationService().getObject().grantServiceTicket(ticketGrantingTicket.getId(), svc, ctx);
        assertNotNull(serviceTicket);
    }

    @Test
    public void verifyGrantServiceTicketWithNoCredsAndSsoFalse() {
        val svc = getService("TestSsoFalse");
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);
        val ticketGrantingTicket = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);
        assertNotNull(getCentralAuthenticationService().getObject().grantServiceTicket(ticketGrantingTicket.getId(), svc, ctx));
    }

    @Test
    public void verifyGrantServiceTicketWithNoCredsAndSsoFalseAndSsoFalse() {
        val svc = getService("TestSsoFalse");
        val ctx = mock(AuthenticationResult.class);
        when(ctx.getAuthentication()).thenReturn(CoreAuthenticationTestUtils.getAuthentication());
        when(ctx.isCredentialProvided()).thenReturn(true);
        val ticketGrantingTicket = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);
        val service = getService("eduPersonTest");
        getCentralAuthenticationService().getObject().grantServiceTicket(ticketGrantingTicket.getId(), service, ctx);
        when(ctx.isCredentialProvided()).thenReturn(false);

        assertThrows(UnauthorizedSsoServiceException.class, () -> getCentralAuthenticationService().getObject().grantServiceTicket(ticketGrantingTicket.getId(), svc, ctx));
    }

    @Test
    public void verifyValidateServiceTicketNoAttributesReturned() {
        val service = getService();
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), service);
        val ticketGrantingTicket = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);
        val serviceTicket = getCentralAuthenticationService().getObject().grantServiceTicket(ticketGrantingTicket.getId(), service, ctx);

        val assertion = getCentralAuthenticationService().getObject().validateServiceTicket(serviceTicket.getId(), service);
        val auth = assertion.getPrimaryAuthentication();
        assertEquals(0, auth.getPrincipal().getAttributes().size());
    }

    @Test
    public void verifyValidateServiceTicketReturnAllAttributes() {
        val service = getService("eduPersonTest");
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), service);
        val ticketGrantingTicket = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);
        val serviceTicket = getCentralAuthenticationService().getObject().grantServiceTicket(ticketGrantingTicket.getId(), service, ctx);

        val assertion = getCentralAuthenticationService().getObject().validateServiceTicket(serviceTicket.getId(), service);
        val auth = assertion.getPrimaryAuthentication();
        assertEquals(3, auth.getPrincipal().getAttributes().size());
    }

    @Test
    public void verifyValidateServiceTicketReturnOnlyAllowedAttribute() {
        val service = getService("eduPersonTestInvalid");
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), service);
        val ticketGrantingTicket = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);
        val serviceTicket = getCentralAuthenticationService().getObject().grantServiceTicket(ticketGrantingTicket.getId(), service, ctx);

        val assertion = getCentralAuthenticationService().getObject().validateServiceTicket(serviceTicket.getId(), service);
        val auth = assertion.getPrimaryAuthentication();
        val attributes = auth.getPrincipal().getAttributes();
        assertEquals(1, attributes.size());
        assertEquals("adopters", attributes.get("groupMembership").get(0));
    }

    @Test
    public void verifyValidateServiceTicketAnonymous() {
        val service = getService("testAnonymous");
        val cred = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), service);
        val ticketGrantingTicket = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);
        val serviceTicket = getCentralAuthenticationService().getObject().grantServiceTicket(ticketGrantingTicket.getId(), service, ctx);

        val assertion = getCentralAuthenticationService().getObject().validateServiceTicket(serviceTicket.getId(), service);
        val auth = assertion.getPrimaryAuthentication();
        assertNotEquals(cred.getUsername(), auth.getPrincipal().getId());
    }

    @Test
    public void verifyValidateServiceTicketWithInvalidUsernameAttribute() {
        val svc = getService("eduPersonTestInvalid");
        val cred = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);
        val ticketGrantingTicket = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);

        val serviceTicket = getCentralAuthenticationService().getObject().grantServiceTicket(ticketGrantingTicket.getId(), svc, ctx);

        val assertion = getCentralAuthenticationService().getObject().validateServiceTicket(serviceTicket.getId(), svc);
        val auth = assertion.getPrimaryAuthentication();

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
        val cas = getCentralAuthenticationService().getObject();
        val svc = getService("testDefault");
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);
        val tgtId = cas.createTicketGrantingTicket(ctx);
        cas.grantServiceTicket(tgtId.getId(), svc, ctx);
        val st2Id = cas.grantServiceTicket(tgtId.getId(), svc, ctx);
        val assertion = cas.validateServiceTicket(st2Id.getId(), svc);
        val validationSpecification = new Cas20WithoutProxyingValidationSpecification(mock(ServicesManager.class));
        assertTrue(validationSpecification.isSatisfiedBy(assertion, new MockHttpServletRequest()));
    }

    /**
     * This test checks that the TGT destruction happens properly for a remote registry.
     * It previously failed when the deletion happens before the ticket was marked expired because an update was necessary for that.
     */
    @Test
    public void verifyDestroyRemoteRegistry() throws AbstractTicketException, AuthenticationException {
        val registry = new MockOnlyOneTicketRegistry();
        val expirationPolicy = mock(ExpirationPolicy.class);
        when(expirationPolicy.getClock()).thenReturn(Clock.systemUTC());
        val tgt = new TicketGrantingTicketImpl("TGT-1", mock(Authentication.class), expirationPolicy);
        val logoutManager = mock(LogoutManager.class);
        when(logoutManager.performLogout(any(TicketGrantingTicket.class)))
            .thenAnswer(invocation -> {
                tgt.markTicketExpired();
                registry.updateTicket(tgt);
                return null;
            });
        registry.addTicket(tgt);
        val servicesManager = mock(ServicesManager.class);
        val cas = new DefaultCentralAuthenticationService(
            mock(ApplicationEventPublisher.class),
            registry,
            servicesManager,
            logoutManager,
            null,
            null,
            null,
            PrincipalFactoryUtils.newPrincipalFactory(),
            CipherExecutor.noOpOfStringToString(),
            mock(AuditableExecution.class),
            new DefaultServiceMatchingStrategy(servicesManager));
        cas.destroyTicketGrantingTicket(tgt.getId());
    }

    private static Service getService(final String name) {
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, name);
        return new WebApplicationServiceFactory().createService(request);
    }

    private static Service getService() {
        return getService(CoreAuthenticationTestUtils.CONST_TEST_URL);
    }
}
