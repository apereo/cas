package org.apereo.cas;

import module java.base;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.policy.AtLeastOneCredentialValidatedAuthenticationPolicy;
import org.apereo.cas.authentication.principal.DefaultServiceMatchingStrategy;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.mock.MockServiceTicket;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RefuseRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegexMatchingRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedProxyingException;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.expiration.AlwaysExpiresExpirationPolicy;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.lock.LockRepository;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests with the help of Mockito framework.
 *
 * @author Dmitriy Kopylenko
 * @since 3.0.0
 */
@Tag("CAS")
class DefaultCentralAuthenticationServiceMockitoTests extends BaseCasCoreTests {
    private String ticketGrantingTicketId;

    private String ticketGrantingTicketId2;

    private String serviceTicketId;

    private String serviceTicketId2;

    private String serviceId1;

    private String serviceId2;

    private String principal;

    private CentralAuthenticationService cas;

    private Authentication authentication;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier(TicketFactory.BEAN_NAME)
    private TicketFactory ticketFactory;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
    private PrincipalResolver principalResolver;

    private void addServices(final Service service1, final Service service2) {
        val mockRegSvc1 = createMockRegisteredService(service1.getId(), true, getServiceProxyPolicy(false));
        val mockRegSvc2 = createMockRegisteredService("test", false, getServiceProxyPolicy(true));
        val mockRegSvc3 = createMockRegisteredService(service2.getId(), true, getServiceProxyPolicy(true));
        servicesManager.save(mockRegSvc1, mockRegSvc2, mockRegSvc3);
    }

    private static MockServiceTicket createMockServiceTicket(final String id, final Service svc) {
        return new MockServiceTicket(id, svc, null);
    }

    private static RegisteredServiceProxyPolicy getServiceProxyPolicy(final boolean canProxy) {
        if (!canProxy) {
            return new RefuseRegisteredServiceProxyPolicy();
        }
        return new RegexMatchingRegisteredServiceProxyPolicy().setPattern(".*");
    }

    private static RegisteredService createMockRegisteredService(final String svcId,
                                                                 final boolean enabled,
                                                                 final RegisteredServiceProxyPolicy proxy) {
        val service = new CasRegisteredService();
        service.setId(RandomUtils.nextInt());
        service.setName(UUID.randomUUID().toString());
        service.setServiceId(svcId);
        service.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(enabled, true));
        service.setProxyPolicy(proxy);
        service.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        return service;
    }

    @BeforeEach
    void prepareNewCAS() throws Throwable {
        this.ticketGrantingTicketId = UUID.randomUUID().toString();
        this.ticketGrantingTicketId2 = UUID.randomUUID().toString();
        this.serviceTicketId = UUID.randomUUID().toString();
        this.serviceTicketId2 = UUID.randomUUID().toString();
        this.serviceId1 = UUID.randomUUID().toString();
        this.serviceId2 = UUID.randomUUID().toString();
        this.principal = UUID.randomUUID().toString();

        authentication = RegisteredServiceTestUtils.getAuthentication(principal,
            new SimpleTestUsernamePasswordAuthenticationHandler(),
            RegisteredServiceTestUtils.getCredentialsWithSameUsernameAndPassword("principal"), Map.of());
        val tgtRootMock = createRootTicketGrantingTicket();
        val service1 = RegisteredServiceTestUtils.getService(serviceId1);
        val stMock = createMockServiceTicket(serviceTicketId, service1);
        val tgtMock = createMockTicketGrantingTicket(ticketGrantingTicketId, stMock, false, tgtRootMock, new ArrayList<>());
        val proxiedBy = RegisteredServiceTestUtils.getService("proxiedBy");
        when(tgtMock.getProxiedBy()).thenReturn(proxiedBy);
        stMock.setTicketGrantingTicket(tgtMock);

        val authnListMock = List.of(authentication, authentication);
        when(tgtMock.getChainedAuthentications()).thenReturn(authnListMock);

        val service2 = RegisteredServiceTestUtils.getService(serviceId2);
        val stMock2 = createMockServiceTicket(serviceTicketId2, service2);
        val tgtMock2 = createMockTicketGrantingTicket(ticketGrantingTicketId2, stMock2, false, tgtRootMock, authnListMock);
        stMock2.setTicketGrantingTicket(tgtMock2);
        ticketRegistry.addTicket(Stream.of(stMock, tgtMock, stMock2, tgtMock2));

        addServices(service1, service2);

        val authenticationRequestServiceSelectionStrategies =
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy());
        val enforcer = mock(AuditableExecution.class);
        when(enforcer.execute(any())).thenReturn(new AuditableExecutionResult());

        val context = CentralAuthenticationServiceContext.builder()
            .applicationContext(applicationContext)
            .ticketRegistry(ticketRegistry)
            .servicesManager(servicesManager)
            .ticketFactory(ticketFactory)
            .lockRepository(LockRepository.asDefault())
            .authenticationServiceSelectionPlan(authenticationRequestServiceSelectionStrategies)
            .authenticationPolicy(new AtLeastOneCredentialValidatedAuthenticationPolicy(false))
            .principalFactory(PrincipalFactoryUtils.newPrincipalFactory())
            .cipherExecutor(CipherExecutor.noOpOfStringToString())
            .registeredServiceAccessStrategyEnforcer(enforcer)
            .serviceMatchingStrategy(new DefaultServiceMatchingStrategy(servicesManager))
            .principalResolver(principalResolver)
            .build();
        cas = new DefaultCentralAuthenticationService(context);
    }

    @Test
    void verifyNonExistentServiceWhenDelegatingTicketGrantingTicket() {
        assertThrows(InvalidTicketException.class, () -> cas.createProxyGrantingTicket("bad-st", getAuthenticationContext()));
    }

    @Test
    void verifyInvalidServiceWhenDelegatingTicketGrantingTicket() {
        assertThrows(UnauthorizedServiceException.class, () -> cas.createProxyGrantingTicket(serviceTicketId, getAuthenticationContext()));
    }

    @Test
    void disallowVendingServiceTicketsWhenServiceIsNotAllowedToProxyCAS1019() {
        assertThrows(UnauthorizedProxyingException.class,
            () -> cas.grantServiceTicket(ticketGrantingTicketId, RegisteredServiceTestUtils.getService(serviceId1), getAuthenticationContext()));
    }

    @Test
    void verifyChainedAuthenticationsOnValidation() throws Throwable {
        val svc = RegisteredServiceTestUtils.getService(serviceId2);
        val st = cas.grantServiceTicket(ticketGrantingTicketId2, svc, getAuthenticationContext());
        assertNotNull(st);

        val assertion = cas.validateServiceTicket(st.getId(), svc);
        assertNotNull(assertion);

        assertEquals(assertion.getService(), svc);
        assertEquals(principal, assertion.getPrimaryAuthentication().getPrincipal().getId());
        assertSame(2, assertion.getChainedAuthentications().size());
        IntStream.range(0, assertion.getChainedAuthentications().size())
            .forEach(i -> assertEquals(assertion.getChainedAuthentications().get(i), authentication));
    }

    private AuthenticationResult getAuthenticationContext() {
        return CoreAuthenticationTestUtils.getAuthenticationResult(authentication);
    }


    private TicketGrantingTicket createRootTicketGrantingTicket() {
        return new MockTicketGrantingTicket(authentication);
    }

    private TicketGrantingTicket createMockTicketGrantingTicket(
        final String id, final ServiceTicket svcTicket,
        final boolean isExpired,
        final TicketGrantingTicket root,
        final List<Authentication> chainedAuthnList) {
        val tgtMock = mock(TicketGrantingTicket.class);
        when(tgtMock.isExpired()).thenReturn(isExpired);
        when(tgtMock.getId()).thenReturn(id);

        if (isExpired) {
            when(tgtMock.getExpirationPolicy()).thenReturn(AlwaysExpiresExpirationPolicy.INSTANCE);
        } else {
            when(tgtMock.getExpirationPolicy()).thenReturn(NeverExpiresExpirationPolicy.INSTANCE);
        }

        val svcId = svcTicket.getService().getId();
        when(tgtMock.grantServiceTicket(anyString(), argThat(new VerifyServiceByIdMatcher(svcId)),
            any(ExpirationPolicy.class), anyBoolean(), any())).thenReturn(svcTicket);
        when(tgtMock.getRoot()).thenReturn(root);
        when(tgtMock.getChainedAuthentications()).thenReturn(chainedAuthnList);
        when(tgtMock.getAuthentication()).thenReturn(authentication);
        when(tgtMock.getCreationTime()).thenReturn(ZonedDateTime.now(Clock.systemUTC()));

        return tgtMock;
    }

    @SuppressWarnings("UnusedVariable")
    private record VerifyServiceByIdMatcher(String id) implements ArgumentMatcher<Service> {
        @Override
        public boolean matches(final Service service) {
            return service != null && service.getId().equals(id());
        }
    }
}
