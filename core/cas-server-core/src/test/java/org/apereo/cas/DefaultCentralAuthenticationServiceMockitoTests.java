package org.apereo.cas;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.metadata.BasicCredentialMetaData;
import org.apereo.cas.authentication.policy.AcceptAnyAuthenticationPolicyFactory;
import org.apereo.cas.authentication.principal.DefaultServiceMatchingStrategy;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.mock.MockServiceTicket;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceUsernameProvider;
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
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.factory.DefaultProxyGrantingTicketFactory;
import org.apereo.cas.ticket.factory.DefaultProxyTicketFactory;
import org.apereo.cas.ticket.factory.DefaultServiceTicketFactory;
import org.apereo.cas.ticket.factory.DefaultTicketFactory;
import org.apereo.cas.ticket.factory.DefaultTicketGrantingTicketFactory;
import org.apereo.cas.ticket.factory.DefaultTransientSessionTicketFactory;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests with the help of Mockito framework.
 *
 * @author Dmitriy Kopylenko
 * @since 3.0.0
 */
@Tag("CAS")
public class DefaultCentralAuthenticationServiceMockitoTests extends BaseCasCoreTests {
    private static final String TGT_ID = "tgt-id";
    private static final String TGT2_ID = "tgt2-id";

    private static final String ST_ID = "st-id";
    private static final String ST2_ID = "st2-id";

    private static final String SVC1_ID = "test1";
    private static final String SVC2_ID = "test2";

    private static final String PRINCIPAL = "principal";

    private DefaultCentralAuthenticationService cas;
    private Authentication authentication;
    private TicketRegistry ticketRegMock;

    private static ServicesManager getServicesManager(final Service service1, final Service service2) {
        val mockRegSvc1 = createMockRegisteredService(service1.getId(), true, getServiceProxyPolicy(false));
        val mockRegSvc2 = createMockRegisteredService("test", false, getServiceProxyPolicy(true));
        val mockRegSvc3 = createMockRegisteredService(service2.getId(), true, getServiceProxyPolicy(true));

        val smMock = mock(ServicesManager.class);
        when(smMock.findServiceBy(argThat(new VerifyServiceByIdMatcher(service1.getId())))).thenReturn(mockRegSvc1);
        when(smMock.findServiceBy(argThat(new VerifyServiceByIdMatcher("test")))).thenReturn(mockRegSvc2);
        when(smMock.findServiceBy(argThat(new VerifyServiceByIdMatcher(service2.getId())))).thenReturn(mockRegSvc3);
        return smMock;
    }

    private static MockServiceTicket createMockServiceTicket(final String id, final Service svc) {
        return new MockServiceTicket(id, svc, null);
    }

    private static RegisteredServiceProxyPolicy getServiceProxyPolicy(final boolean canProxy) {
        if (!canProxy) {
            return new RefuseRegisteredServiceProxyPolicy();
        }

        return new RegexMatchingRegisteredServiceProxyPolicy(".*");
    }

    private static RegisteredService createMockRegisteredService(final String svcId,
                                                                 final boolean enabled, final RegisteredServiceProxyPolicy proxy) {
        val mockRegSvc = mock(RegisteredService.class);
        when(mockRegSvc.getServiceId()).thenReturn(svcId);
        when(mockRegSvc.getProxyPolicy()).thenReturn(proxy);
        when(mockRegSvc.getName()).thenReturn(svcId);
        when(mockRegSvc.matches(argThat(new VerifyServiceByIdMatcher(svcId)))).thenReturn(true);
        when(mockRegSvc.getAttributeReleasePolicy()).thenReturn(new ReturnAllAttributeReleasePolicy());
        when(mockRegSvc.getUsernameAttributeProvider()).thenReturn(new DefaultRegisteredServiceUsernameProvider());
        when(mockRegSvc.getAccessStrategy()).thenReturn(new DefaultRegisteredServiceAccessStrategy(enabled, true));
        return mockRegSvc;
    }

    private static Service getService(final String name) {
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, name);
        return new WebApplicationServiceFactory().createService(request);
    }

    @BeforeEach
    public void prepareNewCAS() {
        this.authentication = mock(Authentication.class);
        when(this.authentication.getAuthenticationDate()).thenReturn(ZonedDateTime.now(ZoneOffset.UTC));
        val metadata = new BasicCredentialMetaData(RegisteredServiceTestUtils.getCredentialsWithSameUsernameAndPassword("principal"));
        val successes = new HashMap<String, AuthenticationHandlerExecutionResult>();
        successes.put("handler1", new DefaultAuthenticationHandlerExecutionResult(mock(AuthenticationHandler.class), metadata));
        when(this.authentication.getCredentials()).thenReturn(List.of(metadata));
        when(this.authentication.getSuccesses()).thenReturn(successes);
        when(this.authentication.getPrincipal()).thenReturn(PrincipalFactoryUtils.newPrincipalFactory().createPrincipal(PRINCIPAL));

        val tgtRootMock = createRootTicketGrantingTicket();
        val service1 = getService(SVC1_ID);
        val stMock = createMockServiceTicket(ST_ID, service1);
        val tgtMock = createMockTicketGrantingTicket(TGT_ID, stMock, false,
            tgtRootMock, new ArrayList<>());
        when(tgtMock.getProxiedBy()).thenReturn(getService("proxiedBy"));
        stMock.setTicketGrantingTicket(tgtMock);


        val authnListMock = mock(List.class);
        /*
         * Size is required to be 2, so that
         * we can simulate proxying capabilities
         */
        when(authnListMock.size()).thenReturn(2);
        when(authnListMock.toArray()).thenReturn(new Object[]{this.authentication, this.authentication});
        when(authnListMock.get(anyInt())).thenReturn(this.authentication);
        when(tgtMock.getChainedAuthentications()).thenReturn(authnListMock);

        val service2 = getService(SVC2_ID);
        val stMock2 = createMockServiceTicket(ST2_ID, service2);
        val tgtMock2 = createMockTicketGrantingTicket(TGT2_ID, stMock2, false, tgtRootMock, authnListMock);
        stMock2.setTicketGrantingTicket(tgtMock2);


        mockTicketRegistry(stMock, tgtMock, stMock2, tgtMock2);
        val smMock = getServicesManager(service1, service2);
        val factory = getTicketFactory();

        val authenticationRequestServiceSelectionStrategies =
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy());
        val enforcer = mock(AuditableExecution.class);
        when(enforcer.execute(any())).thenReturn(new AuditableExecutionResult());
        this.cas = new DefaultCentralAuthenticationService(
            mock(ApplicationEventPublisher.class),
            ticketRegMock,
            smMock,
            factory,
            authenticationRequestServiceSelectionStrategies,
            new AcceptAnyAuthenticationPolicyFactory(),
            PrincipalFactoryUtils.newPrincipalFactory(),
            CipherExecutor.noOpOfStringToString(),
            enforcer,
            new DefaultServiceMatchingStrategy(smMock));
    }

    private static TicketFactory getTicketFactory() {
        val factory = new DefaultTicketFactory();
        factory.addTicketFactory(ProxyGrantingTicket.class,
            new DefaultProxyGrantingTicketFactory(null,
                null, CipherExecutor.noOpOfStringToString(), mock(ServicesManager.class)));
        factory.addTicketFactory(TicketGrantingTicket.class,
            new DefaultTicketGrantingTicketFactory(null,
                null, CipherExecutor.noOpOfSerializableToString(), mock(ServicesManager.class)));
        factory.addTicketFactory(ServiceTicket.class,
            new DefaultServiceTicketFactory(neverExpiresExpirationPolicyBuilder(),
                new HashMap<>(0), false,
                CipherExecutor.noOpOfStringToString(), mock(ServicesManager.class)));
        factory.addTicketFactory(ProxyTicket.class,
            new DefaultProxyTicketFactory(null, new HashMap<>(0),
                CipherExecutor.noOpOfStringToString(), true, mock(ServicesManager.class)));
        factory.addTicketFactory(TransientSessionTicket.class,
            new DefaultTransientSessionTicketFactory(neverExpiresExpirationPolicyBuilder()));
        assertEquals(Ticket.class, factory.getTicketType());
        return factory;
    }

    private AuthenticationResult getAuthenticationContext() {
        val ctx = mock(AuthenticationResult.class);
        when(ctx.getAuthentication()).thenReturn(this.authentication);
        return ctx;
    }

    private void mockTicketRegistry(final ServiceTicket stMock, final TicketGrantingTicket tgtMock,
                                    final ServiceTicket stMock2, final TicketGrantingTicket tgtMock2) {
        this.ticketRegMock = mock(TicketRegistry.class);
        when(ticketRegMock.getTicket(eq(tgtMock.getId()), eq(TicketGrantingTicket.class))).thenReturn(tgtMock);
        when(ticketRegMock.getTicket(eq(tgtMock2.getId()), eq(TicketGrantingTicket.class))).thenReturn(tgtMock2);
        when(ticketRegMock.getTicket(eq(stMock.getId()), eq(ServiceTicket.class))).thenReturn(stMock);
        when(ticketRegMock.getTicket(eq(stMock2.getId()), eq(ServiceTicket.class))).thenReturn(stMock2);
        when(ticketRegMock.getTickets()).thenReturn((Collection) Arrays.asList(tgtMock, tgtMock2, stMock, stMock2));
        when(ticketRegMock.getTicketsStream()).thenCallRealMethod();
    }

    @Test
    public void verifyNonExistentServiceWhenDelegatingTicketGrantingTicket() {
        assertThrows(InvalidTicketException.class, () -> cas.createProxyGrantingTicket("bad-st", getAuthenticationContext()));
    }

    @Test
    public void verifyInvalidServiceWhenDelegatingTicketGrantingTicket() {
        assertThrows(UnauthorizedServiceException.class, () -> this.cas.createProxyGrantingTicket(ST_ID, getAuthenticationContext()));
    }

    @Test
    public void disallowVendingServiceTicketsWhenServiceIsNotAllowedToProxyCAS1019() {
        assertThrows(UnauthorizedProxyingException.class,
            () -> this.cas.grantServiceTicket(TGT_ID, RegisteredServiceTestUtils.getService(SVC1_ID), getAuthenticationContext()));
    }

    @Test
    public void getTicketGrantingTicketIfTicketIdIsNull() {
        assertThrows(NullPointerException.class, () -> this.cas.getTicket(null, TicketGrantingTicket.class));
    }

    @Test
    public void getTicketGrantingTicketIfTicketIdIsMissing() {
        assertThrows(InvalidTicketException.class, () -> this.cas.getTicket("TGT-9000", TicketGrantingTicket.class));
    }

    @Test
    public void getTicketsWithNoPredicate() {
        val c = this.cas.getTickets(ticket -> true);
        assertEquals(c.size(), this.ticketRegMock.getTickets().size());
    }

    @Test
    public void verifyChainedAuthenticationsOnValidation() {
        val svc = RegisteredServiceTestUtils.getService(SVC2_ID);
        val st = this.cas.grantServiceTicket(TGT2_ID, svc, getAuthenticationContext());
        assertNotNull(st);

        val assertion = this.cas.validateServiceTicket(st.getId(), svc);
        assertNotNull(assertion);

        assertEquals(assertion.getService(), svc);
        assertEquals(PRINCIPAL, assertion.getPrimaryAuthentication().getPrincipal().getId());
        assertSame(2, assertion.getChainedAuthentications().size());
        IntStream.range(0, assertion.getChainedAuthentications().size())
            .forEach(i -> assertEquals(assertion.getChainedAuthentications().get(i), authentication));
    }

    private TicketGrantingTicket createRootTicketGrantingTicket() {
        val tgtRootMock = mock(TicketGrantingTicket.class);
        when(tgtRootMock.isExpired()).thenReturn(false);
        when(tgtRootMock.getAuthentication()).thenReturn(this.authentication);
        return tgtRootMock;
    }

    private TicketGrantingTicket createMockTicketGrantingTicket(final String id, final ServiceTicket svcTicket, final boolean isExpired,
                                                                final TicketGrantingTicket root, final List<Authentication> chainedAuthnList) {
        val tgtMock = mock(TicketGrantingTicket.class);
        when(tgtMock.isExpired()).thenReturn(isExpired);
        when(tgtMock.getId()).thenReturn(id);

        val svcId = svcTicket.getService().getId();
        when(tgtMock.grantServiceTicket(anyString(), argThat(new VerifyServiceByIdMatcher(svcId)),
            any(ExpirationPolicy.class), anyBoolean(), anyBoolean())).thenReturn(svcTicket);
        when(tgtMock.getRoot()).thenReturn(root);
        when(tgtMock.getChainedAuthentications()).thenReturn(chainedAuthnList);
        when(tgtMock.getAuthentication()).thenReturn(this.authentication);

        return tgtMock;
    }

    private static class VerifyServiceByIdMatcher implements ArgumentMatcher<Service> {
        private final String id;

        VerifyServiceByIdMatcher(final String id) {
            this.id = id;
        }

        @Override
        public boolean matches(final Service s) {
            return s != null && s.getId().equals(this.id);
        }
    }
}
