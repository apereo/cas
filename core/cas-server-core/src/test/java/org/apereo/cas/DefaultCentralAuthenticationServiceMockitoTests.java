package org.apereo.cas;

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
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;
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
    private static final String TGT_ID = "tgt-id";

    private static final String TGT2_ID = "tgt2-id";

    private static final String ST_ID = "st-id";

    private static final String ST2_ID = "st2-id";

    private static final String SVC1_ID = "test1";

    private static final String SVC2_ID = "test2";

    private static final String PRINCIPAL = "principal";

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
    public void prepareNewCAS() throws Throwable {
        authentication = RegisteredServiceTestUtils.getAuthentication(PRINCIPAL,
            new SimpleTestUsernamePasswordAuthenticationHandler(),
            RegisteredServiceTestUtils.getCredentialsWithSameUsernameAndPassword("principal"), Map.of());
        val tgtRootMock = createRootTicketGrantingTicket();
        val service1 = RegisteredServiceTestUtils.getService(SVC1_ID);
        val stMock = createMockServiceTicket(ST_ID, service1);
        val tgtMock = createMockTicketGrantingTicket(TGT_ID, stMock, false, tgtRootMock, new ArrayList<>());
        val proxiedBy = RegisteredServiceTestUtils.getService("proxiedBy");
        when(tgtMock.getProxiedBy()).thenReturn(proxiedBy);
        stMock.setTicketGrantingTicket(tgtMock);
        
        val authnListMock = List.of(authentication, authentication);
        when(tgtMock.getChainedAuthentications()).thenReturn(authnListMock);

        val service2 = RegisteredServiceTestUtils.getService(SVC2_ID);
        val stMock2 = createMockServiceTicket(ST2_ID, service2);
        val tgtMock2 = createMockTicketGrantingTicket(TGT2_ID, stMock2, false, tgtRootMock, authnListMock);
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
            .build();
        cas = new DefaultCentralAuthenticationService(context);
    }

    @Test
    void verifyNonExistentServiceWhenDelegatingTicketGrantingTicket() throws Throwable {
        assertThrows(InvalidTicketException.class, () -> cas.createProxyGrantingTicket("bad-st", getAuthenticationContext()));
    }

    @Test
    void verifyInvalidServiceWhenDelegatingTicketGrantingTicket() throws Throwable {
        assertThrows(UnauthorizedServiceException.class, () -> cas.createProxyGrantingTicket(ST_ID, getAuthenticationContext()));
    }

    @Test
    void disallowVendingServiceTicketsWhenServiceIsNotAllowedToProxyCAS1019() {
        assertThrows(UnauthorizedProxyingException.class,
            () -> cas.grantServiceTicket(TGT_ID, RegisteredServiceTestUtils.getService(SVC1_ID), getAuthenticationContext()));
    }

    @Test
    void verifyChainedAuthenticationsOnValidation() throws Throwable {
        val svc = RegisteredServiceTestUtils.getService(SVC2_ID);
        val st = cas.grantServiceTicket(TGT2_ID, svc, getAuthenticationContext());
        assertNotNull(st);

        val assertion = cas.validateServiceTicket(st.getId(), svc);
        assertNotNull(assertion);

        assertEquals(assertion.getService(), svc);
        assertEquals(PRINCIPAL, assertion.getPrimaryAuthentication().getPrincipal().getId());
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
