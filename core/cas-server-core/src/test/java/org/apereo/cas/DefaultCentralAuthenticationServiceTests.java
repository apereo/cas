package org.apereo.cas;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.exceptions.MixedPrincipalException;
import org.apereo.cas.authentication.principal.AbstractWebApplicationService;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedProxyingException;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.services.UnauthorizedSsoServiceException;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.ServiceAwareTicket;
import org.apereo.cas.ticket.ServiceTicketImpl;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.UnrecognizableServiceForServiceTicketValidationException;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.validation.DefaultCasProtocolValidationSpecification;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("CAS")
@TestPropertySource(properties = "cas.ticket.crypto.enabled=true")
class DefaultCentralAuthenticationServiceTests extends AbstractCentralAuthenticationServiceTests {

    private Service getService(final String name) {
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, name);
        return getWebApplicationServiceFactory().createService(request);
    }

    private Service getService() {
        return getService(CoreAuthenticationTestUtils.CONST_TEST_URL);
    }

    @Test
    void verifyBadCredentialsOnTicketGrantingTicketCreation() {
        assertThrows(AuthenticationException.class, () -> CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(),
            CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword()));
    }

    @Test
    void verifyGoodCredentialsOnTicketGrantingTicketCreation() throws Throwable {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport());
        assertNotNull(getCentralAuthenticationService().createTicketGrantingTicket(ctx));
    }

    @Test
    void verifyDestroyTicketGrantingTicketWithNonExistingTicket() throws Throwable {
        getTicketRegistry().deleteTicket("test");
    }

    @Test
    void verifyDestroyTicketGrantingTicketWithValidTicket() throws Throwable {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport());
        val ticketId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        getTicketRegistry().deleteTicket(ticketId.getId());
    }

    @Test
    void verifyDisallowNullCredentialsWhenCreatingTicketGrantingTicket() {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), new Credential[]{null});
        assertThrows(RuntimeException.class, () -> getCentralAuthenticationService().createTicketGrantingTicket(ctx));
    }

    @Test
    void verifyDisallowNullCredentialsArrayWhenCreatingTicketGrantingTicket() {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), new Credential[]{null, null});
        assertThrows(RuntimeException.class, () -> getCentralAuthenticationService().createTicketGrantingTicket(ctx));
    }

    @Test
    void verifyDestroyTicketGrantingTicketWithInvalidTicket() throws Throwable {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport());
        val ticketId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val serviceTicketId = getCentralAuthenticationService().grantServiceTicket(ticketId.getId(), getService(), ctx);
        assertDoesNotThrow(() -> {
            getTicketRegistry().deleteTicket(serviceTicketId.getId());
        });
    }

    @Test
    void verifyGrantingOfServiceTicketUsingDefaultTicketIdGen() throws Throwable {
        val mockService = RegisteredServiceTestUtils.getService("testDefault");
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), mockService);
        val ticketId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val serviceTicketId = getCentralAuthenticationService().grantServiceTicket(ticketId.getId(), mockService, ctx);
        assertNotNull(serviceTicketId);
    }

    @Test
    void verifyGrantServiceTicketWithValidTicketGrantingTicket() throws Throwable {
        assertNotNull(getCentralAuthenticationService().getTicketFactory());
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport());
        val ticketId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        getCentralAuthenticationService().grantServiceTicket(ticketId.getId(), getService(), ctx);
    }

    @Test
    void verifyGrantServiceTicketFailsAuthzRule() {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(),
            getService("TestServiceAttributeForAuthzFails"));

        assertThrows(PrincipalException.class, () -> getCentralAuthenticationService().createTicketGrantingTicket(ctx));
    }

    @Test
    void verifyGrantServiceTicketPassesAuthzRule() throws Throwable {
        val service = getService("TestServiceAttributeForAuthzPasses");
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), service);
        val ticketId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        getCentralAuthenticationService().grantServiceTicket(ticketId.getId(), service, ctx);
    }

    @Test
    void verifyGrantProxyTicketWithValidTicketGrantingTicket() throws Throwable {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport());
        val ticketId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val serviceTicketId = getCentralAuthenticationService().grantServiceTicket(ticketId.getId(), getService(), ctx);

        val ctx2 = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(),
            RegisteredServiceTestUtils.getHttpBasedServiceCredentials());
        val pgt = getCentralAuthenticationService().createProxyGrantingTicket(serviceTicketId.getId(), ctx2);

        val pt = (ProxyTicket) getCentralAuthenticationService().grantProxyTicket(pgt.getId(), getService());
        assertNotNull(pt);
        assertNotNull(pt.getAuthentication());
    }

    @Test
    void verifyGrantProxyTicketUnauthzProxy() throws Throwable {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport());
        val ticketId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val serviceTicketId = getCentralAuthenticationService().grantServiceTicket(ticketId.getId(),
            RegisteredServiceTestUtils.getService("eduPersonTest"), ctx);

        val ctx2 = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(),
            RegisteredServiceTestUtils.getHttpBasedServiceCredentials());
        assertThrows(UnauthorizedProxyingException.class,
            () -> getCentralAuthenticationService().createProxyGrantingTicket(serviceTicketId.getId(), ctx2));
    }

    @Test
    void verifyGrantProxyTicketFailsServiceAccess() throws Throwable {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport());
        val ticketId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val serviceTicketId = getCentralAuthenticationService().grantServiceTicket(ticketId.getId(), getService(), ctx);

        val ctx2 = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(),
            RegisteredServiceTestUtils.getHttpBasedServiceCredentials());
        val pgt = getCentralAuthenticationService().createProxyGrantingTicket(serviceTicketId.getId(), ctx2);

        assertThrows(UnauthorizedSsoServiceException.class,
            () -> getCentralAuthenticationService().grantProxyTicket(pgt.getId(), RegisteredServiceTestUtils.getService("unknown-service")));
    }

    @Test
    void verifyGrantServiceTicketWithInvalidTicketGrantingTicket() throws Throwable {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport());

        val ticketId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        getTicketRegistry().deleteTicket(ticketId.getId());

        assertThrows(AbstractTicketException.class,
            () -> getCentralAuthenticationService().grantServiceTicket(ticketId.getId(), getService(), ctx));
    }

    @Test
    void verifyDelegateTicketGrantingTicketWithProperParams() throws Throwable {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), getService());
        val ticketId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val serviceTicketId = getCentralAuthenticationService().grantServiceTicket(ticketId.getId(), getService(), ctx);
        val ctx2 = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(),
            RegisteredServiceTestUtils.getHttpBasedServiceCredentials());
        val pgt = getCentralAuthenticationService().createProxyGrantingTicket(serviceTicketId.getId(), ctx2);
        assertNotNull(pgt);
    }

    @Test
    void verifyProxyGrantingTicketHasRootAuthenticationAsPrincipal() throws Throwable {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), getService());
        val ticket = (TicketGrantingTicket) getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val serviceTicketId = (ServiceAwareTicket) getCentralAuthenticationService().grantServiceTicket(ticket.getId(), getService(), ctx);
        val service = (AbstractWebApplicationService) serviceTicketId.getService();
        assertEquals(service.getPrincipal(), ticket.getAuthentication().getPrincipal().getId());
    }

    @Test
    void verifyDelegateTicketGrantingTicketWithBadServiceTicket() throws Throwable {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), getService());
        val ticketId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val serviceTicketId = getCentralAuthenticationService().grantServiceTicket(ticketId.getId(), getService(), ctx);
        getTicketRegistry().deleteTicket(ticketId.getId());
        val ctx2 = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(),
            RegisteredServiceTestUtils.getHttpBasedServiceCredentials());
        assertThrows(AbstractTicketException.class,
            () -> getCentralAuthenticationService().createProxyGrantingTicket(serviceTicketId.getId(), ctx2));
    }

    @Test
    void verifyGrantServiceTicketWithValidCredentials() throws Throwable {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), getService());
        val ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), getService(), ctx);
    }

    @Test
    void verifyGrantServiceTicketWithDifferentCredentials() throws Throwable {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(),
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("testA"));
        val ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val ctx2 = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(),
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("testB"));
        assertThrows(MixedPrincipalException.class,
            () -> getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), getService(), ctx2));
    }

    @Test
    void verifyValidateServiceTicketWithValidService() throws Throwable {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport());
        val ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), getService(), ctx);
        assertNotNull(getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), getService()));
    }

    @Test
    void verifyValidateServiceTicketWithMappedAttrPolicy() throws Throwable {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport());
        val ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val operativeService = getService("accessStrategyMapped");
        val serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), operativeService, ctx);
        assertNotNull(getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), operativeService));
    }

    @Test
    void verifyValidateServiceTicketFailsTicket() throws Throwable {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport());
        val ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), getService(), ctx);
        val id = UUID.randomUUID().toString();
        assertThrows(InvalidTicketException.class,
            () -> getCentralAuthenticationService().validateServiceTicket(id, getService()));

        assertThrows(UnrecognizableServiceForServiceTicketValidationException.class,
            () -> getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(),
                RegisteredServiceTestUtils.getService(id)));
    }

    @Test
    void verifyValidateServiceTicketWithoutTicketGrantingTicket() throws Throwable {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport());
        val ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), getService(), ctx);
        ((ServiceTicketImpl) serviceTicket).setTicketGrantingTicket(null);
        assertThrows(InvalidTicketException.class,
            () -> getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), getService()));
    }

    @Test
    void verifyValidateServiceTicketWithInvalidService() {
        val service = getService("badtestservice");
        assertThrows(UnauthorizedServiceException.class,
            () -> CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), service));
    }

    @Test
    void verifyValidateServiceTicketWithInvalidServiceTicket() throws Throwable {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), getService());

        val ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), getService(), ctx);
        getTicketRegistry().deleteTicket(ticketGrantingTicket.getId());

        assertThrows(AbstractTicketException.class,
            () -> getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), getService()));
    }

    @Test
    void verifyValidateServiceTicketWithInvalidProxy() throws Throwable {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), getService());

        val ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), getService(), ctx);
        getTicketRegistry().deleteTicket(ticketGrantingTicket.getId());

        assertThrows(AbstractTicketException.class,
            () -> getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), getService()));
    }

    @Test
    void verifyValidateServiceTicketNonExistingTicket() {
        assertThrows(AbstractTicketException.class, () -> getCentralAuthenticationService().validateServiceTicket("google", getService()));
    }

    @Test
    void verifyValidateServiceTicketWithoutUsernameAttribute() throws Throwable {
        val cred = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), getService());

        val ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), getService(), ctx);

        val assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), getService());
        val auth = assertion.getPrimaryAuthentication();
        assertEquals(auth.getPrincipal().getId(), cred.getUsername());
    }

    @Test
    void verifyValidateServiceTicketWithDefaultUsernameAttribute() throws Throwable {
        val svc = getService("testDefault");
        val cred = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);
        val ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);

        val serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), svc, ctx);

        val assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), svc);
        val auth = assertion.getPrimaryAuthentication();
        assertEquals(auth.getPrincipal().getId(), cred.getUsername());
    }

    @Test
    void verifyTicketState() throws Throwable {
        val svc = getService("testDefault");
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);
        val ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        var result = getTicketRegistry().getTicket(ticketGrantingTicket.getId());
        assertEquals(result, ticketGrantingTicket);
        result.markTicketExpired();
        result = getTicketRegistry().updateTicket(result);
        assertTrue(result.isExpired());
        assertThrows(InvalidTicketException.class,
            () -> getTicketRegistry().getTicket(ticketGrantingTicket.getId(), TicketGrantingTicket.class));
    }

    @Test
    void verifyValidateServiceTicketWithUsernameAttribute() throws Throwable {
        val svc = getService("eduPersonTest");

        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);
        val ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);

        val serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), svc, ctx);

        val assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), svc);
        assertEquals("developer", assertion.getPrimaryAuthentication().getPrincipal().getId());
    }

    @Test
    void verifyGrantServiceTicketWithCredsAndSsoFalse() throws Throwable {
        val svc = getService("TestSsoFalse");
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);
        val ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val ticket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), svc, ctx);
        assertNotNull(ticket);
        assertEquals(1, ticketGrantingTicket.getCountOfUses());
        assertDoesNotThrow(() -> getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), svc, ctx));
    }

    @Test
    void verifyGrantServiceTicketWithNoCredsAndSsoFalse() throws Throwable {
        val svc = getService("TestSsoFalse");
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(svc);
        when(ctx.isCredentialProvided()).thenReturn(true);
        val ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val ticket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), svc, ctx);
        assertNotNull(ticket);
        assertEquals(1, ticketGrantingTicket.getCountOfUses());
        when(ctx.isCredentialProvided()).thenReturn(false);
        assertThrows(UnauthorizedSsoServiceException.class,
            () -> getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), svc, ctx));
    }

    @Test
    void verifyGrantServiceTicketWithNoCredsAndSsoFalseAndSsoFalse() throws Throwable {
        val svc = getService("TestSsoFalse");
        val ctx = mock(AuthenticationResult.class);
        when(ctx.getAuthentication()).thenReturn(CoreAuthenticationTestUtils.getAuthentication());
        when(ctx.isCredentialProvided()).thenReturn(true);
        val ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val service = getService("eduPersonTest");
        getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), service, ctx);
        when(ctx.isCredentialProvided()).thenReturn(false);

        assertThrows(UnauthorizedSsoServiceException.class,
            () -> getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), svc, ctx));
    }

    @Test
    void verifyValidateServiceTicketNoAttributesReturned() throws Throwable {
        val service = getService();
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), service);
        val ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), service, ctx);

        val assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), service);
        val auth = assertion.getPrimaryAuthentication();
        val attributes = auth.getPrincipal().getAttributes();
        assertEquals(1, attributes.size());
        assertTrue(attributes.containsKey("binaryAttribute"));
    }

    @Test
    void verifyValidateServiceTicketReturnAllAttributes() throws Throwable {
        val service = getService("eduPersonTest");
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), service);
        val ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), service, ctx);

        val assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), service);
        val auth = assertion.getPrimaryAuthentication();
        val attributes = auth.getPrincipal().getAttributes();
        assertEquals(5, attributes.size());
        assertTrue(attributes.containsKey("uid"));
        assertTrue(attributes.containsKey("mail"));
        assertTrue(attributes.containsKey("binaryAttribute"));
        assertTrue(attributes.containsKey("groupMembership"));
        assertTrue(attributes.containsKey("eduPersonAffiliation"));
    }

    @Test
    void verifyValidateServiceTicketReturnOnlyAllowedAttribute() throws Throwable {
        val service = getService("eduPersonTestInvalid");
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), service);
        val ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), service, ctx);

        val assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), service);
        val auth = assertion.getPrimaryAuthentication();
        val attributes = auth.getPrincipal().getAttributes();
        assertEquals(1, attributes.size());
        assertEquals("adopters", attributes.get("groupMembership").getFirst());
    }

    @Test
    void verifyValidateServiceTicketAnonymous() throws Throwable {
        val service = getService("testAnonymous");
        val cred = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), service);
        val ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), service, ctx);

        val assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), service);
        val auth = assertion.getPrimaryAuthentication();
        assertNotEquals(cred.getUsername(), auth.getPrincipal().getId());
    }

    @Test
    void verifyValidateServiceTicketWithInvalidUsernameAttribute() throws Throwable {
        val svc = getService("eduPersonTestInvalid");
        val cred = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);
        val ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val serviceTicket = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), svc, ctx);
        val assertion = getCentralAuthenticationService().validateServiceTicket(serviceTicket.getId(), svc);
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
    void verifyAuthenticateTwiceWithRenew() throws Throwable {
        val cas = getCentralAuthenticationService();
        val svc = getService("testDefault");
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);
        val tgtId = cas.createTicketGrantingTicket(ctx);
        cas.grantServiceTicket(tgtId.getId(), svc, ctx);
        val st2Id = cas.grantServiceTicket(tgtId.getId(), svc, ctx);
        val assertion = cas.validateServiceTicket(st2Id.getId(), svc);
        val validationSpecification = new DefaultCasProtocolValidationSpecification(mock(ServicesManager.class), input -> true);
        assertTrue(validationSpecification.isSatisfiedBy(assertion, new MockHttpServletRequest()));
    }
}
