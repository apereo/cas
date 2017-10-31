package org.apereo.cas;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.BasicCredentialMetaData;
import org.apereo.cas.authentication.CredentialMetaData;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.DefaultHandlerResult;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.policy.AcceptAnyAuthenticationPolicyFactory;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
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
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.factory.DefaultProxyGrantingTicketFactory;
import org.apereo.cas.ticket.factory.DefaultProxyTicketFactory;
import org.apereo.cas.ticket.factory.DefaultServiceTicketFactory;
import org.apereo.cas.ticket.factory.DefaultTicketFactory;
import org.apereo.cas.ticket.factory.DefaultTicketGrantingTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.apereo.cas.validation.Assertion;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests with the help of Mockito framework.
 *
 * @author Dmitriy Kopylenko
 * @since 3.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        RefreshAutoConfiguration.class,
        CasCoreConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasCoreTicketIdGeneratorsConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasCoreWebConfiguration.class,
        CasPersonDirectoryTestConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasRegisteredServicesTestConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        CasAuthenticationEventExecutionPlanTestConfiguration.class,
        CasDefaultServiceTicketIdGeneratorsConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class
})
public class CentralAuthenticationServiceImplWithMockitoTests {

    private static final String TGT_ID = "tgt-id";
    private static final String TGT2_ID = "tgt2-id";

    private static final String ST_ID = "st-id";
    private static final String ST2_ID = "st2-id";

    private static final String SVC1_ID = "test1";
    private static final String SVC2_ID = "test2";

    private static final String PRINCIPAL = "principal";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private DefaultCentralAuthenticationService cas;
    private Authentication authentication;
    private TicketRegistry ticketRegMock;

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

    @Before
    public void prepareNewCAS() {
        this.authentication = mock(Authentication.class);
        when(this.authentication.getAuthenticationDate()).thenReturn(ZonedDateTime.now(ZoneOffset.UTC));
        final CredentialMetaData metadata = new BasicCredentialMetaData(
                RegisteredServiceTestUtils.getCredentialsWithSameUsernameAndPassword("principal"));
        final Map<String, HandlerResult> successes = new HashMap<>();
        successes.put("handler1", new DefaultHandlerResult(mock(AuthenticationHandler.class), metadata));
        when(this.authentication.getCredentials()).thenReturn(Arrays.asList(metadata));
        when(this.authentication.getSuccesses()).thenReturn(successes);
        when(this.authentication.getPrincipal()).thenReturn(new DefaultPrincipalFactory().createPrincipal(PRINCIPAL));

        final Service service1 = getService(SVC1_ID);
        final ServiceTicket stMock = createMockServiceTicket(ST_ID, service1);

        final TicketGrantingTicket tgtRootMock = createRootTicketGrantingTicket();

        final TicketGrantingTicket tgtMock = createMockTicketGrantingTicket(TGT_ID, stMock, false,
                tgtRootMock, new ArrayList<>());
        when(tgtMock.getProxiedBy()).thenReturn(getService("proxiedBy"));

        final List<Authentication> authnListMock = mock(List.class);
        // Size is required to be 2, so that we can simulate proxying capabilities
        when(authnListMock.size()).thenReturn(2);
        when(authnListMock.toArray()).thenReturn(new Object[]{this.authentication, this.authentication});
        when(authnListMock.get(anyInt())).thenReturn(this.authentication);
        when(tgtMock.getChainedAuthentications()).thenReturn(authnListMock);
        when(stMock.getGrantingTicket()).thenReturn(tgtMock);

        final Service service2 = getService(SVC2_ID);
        final ServiceTicket stMock2 = createMockServiceTicket(ST2_ID, service2);

        final TicketGrantingTicket tgtMock2 = createMockTicketGrantingTicket(TGT2_ID, stMock2, false, tgtRootMock, authnListMock);

        //Mock TicketRegistry
        mockTicketRegistry(stMock, tgtMock, stMock2, tgtMock2);

        //Mock ServicesManager
        final ServicesManager smMock = getServicesManager(service1, service2);

        final DefaultTicketFactory factory = new DefaultTicketFactory(
                new DefaultProxyGrantingTicketFactory(null, null, null),
                new DefaultTicketGrantingTicketFactory(null, null, null),
                new DefaultServiceTicketFactory(new NeverExpiresExpirationPolicy(), new HashMap<>(0), false, null),
                new DefaultProxyTicketFactory(null, new HashMap<>(0), null, true));
        final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies =
                new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy());
        this.cas = new DefaultCentralAuthenticationService(ticketRegMock, factory, smMock, mock(LogoutManager.class),
                authenticationRequestServiceSelectionStrategies, new AcceptAnyAuthenticationPolicyFactory(),
                new DefaultPrincipalFactory(), null);
        this.cas.setApplicationEventPublisher(mock(ApplicationEventPublisher.class));
    }

    private AuthenticationResult getAuthenticationContext() {
        final AuthenticationResult ctx = mock(AuthenticationResult.class);
        when(ctx.getAuthentication()).thenReturn(this.authentication);
        return ctx;
    }

    private static ServicesManager getServicesManager(final Service service1, final Service service2) {
        final RegisteredService mockRegSvc1 = createMockRegisteredService(service1.getId(), true, getServiceProxyPolicy(false));
        final RegisteredService mockRegSvc2 = createMockRegisteredService("test", false, getServiceProxyPolicy(true));
        final RegisteredService mockRegSvc3 = createMockRegisteredService(service2.getId(), true, getServiceProxyPolicy(true));

        final ServicesManager smMock = mock(ServicesManager.class);
        when(smMock.findServiceBy(argThat(new VerifyServiceByIdMatcher(service1.getId())))).thenReturn(mockRegSvc1);
        when(smMock.findServiceBy(argThat(new VerifyServiceByIdMatcher("test")))).thenReturn(mockRegSvc2);
        when(smMock.findServiceBy(argThat(new VerifyServiceByIdMatcher(service2.getId())))).thenReturn(mockRegSvc3);
        return smMock;
    }

    private void mockTicketRegistry(final ServiceTicket stMock, final TicketGrantingTicket tgtMock,
                                    final ServiceTicket stMock2, final TicketGrantingTicket tgtMock2) {
        this.ticketRegMock = mock(TicketRegistry.class);
        when(ticketRegMock.getTicket(eq(tgtMock.getId()), eq(TicketGrantingTicket.class))).thenReturn(tgtMock);
        when(ticketRegMock.getTicket(eq(tgtMock2.getId()), eq(TicketGrantingTicket.class))).thenReturn(tgtMock2);
        when(ticketRegMock.getTicket(eq(stMock.getId()), eq(ServiceTicket.class))).thenReturn(stMock);
        when(ticketRegMock.getTicket(eq(stMock2.getId()), eq(ServiceTicket.class))).thenReturn(stMock2);
        when(ticketRegMock.getTickets()).thenReturn(Arrays.asList(tgtMock, tgtMock2, stMock, stMock2));
    }

    @Test
    public void verifyNonExistentServiceWhenDelegatingTicketGrantingTicket() {
        this.thrown.expect(InvalidTicketException.class);
        this.thrown.expectMessage("bad-st");

        this.cas.createProxyGrantingTicket("bad-st", getAuthenticationContext());
    }

    @Test
    public void verifyInvalidServiceWhenDelegatingTicketGrantingTicket() {
        this.thrown.expect(UnauthorizedServiceException.class);
        this.cas.createProxyGrantingTicket(ST_ID, getAuthenticationContext());
    }

    @Test
    public void disallowVendingServiceTicketsWhenServiceIsNotAllowedToProxyCAS1019() {
        this.thrown.expect(UnauthorizedProxyingException.class);
        this.cas.grantServiceTicket(TGT_ID, RegisteredServiceTestUtils.getService(SVC1_ID), getAuthenticationContext());
    }

    @Test
    public void getTicketGrantingTicketIfTicketIdIsNull() throws InvalidTicketException {
        this.thrown.expect(IllegalArgumentException.class);
        this.cas.getTicket(null, TicketGrantingTicket.class);
    }

    @Test
    public void getTicketGrantingTicketIfTicketIdIsMissing() throws InvalidTicketException {
        this.thrown.expect(InvalidTicketException.class);
        this.cas.getTicket("TGT-9000", TicketGrantingTicket.class);
    }

    @Test
    public void getTicketsWithNoPredicate() {
        final Collection<Ticket> c = this.cas.getTickets(ticket -> true);
        assertEquals(c.size(), this.ticketRegMock.getTickets().size());
    }

    @Test
    public void verifyChainedAuthenticationsOnValidation() {
        final Service svc = RegisteredServiceTestUtils.getService(SVC2_ID);
        final ServiceTicket st = this.cas.grantServiceTicket(TGT2_ID, svc, getAuthenticationContext());
        assertNotNull(st);

        final Assertion assertion = this.cas.validateServiceTicket(st.getId(), svc);
        assertNotNull(assertion);

        assertEquals(assertion.getService(), svc);
        assertEquals(assertion.getPrimaryAuthentication().getPrincipal().getId(), PRINCIPAL);
        assertSame(2, assertion.getChainedAuthentications().size());
        IntStream.range(0, assertion.getChainedAuthentications().size())
                .forEach(i -> assertEquals(assertion.getChainedAuthentications().get(i), authentication));
    }

    private TicketGrantingTicket createRootTicketGrantingTicket() {
        final TicketGrantingTicket tgtRootMock = mock(TicketGrantingTicket.class);
        when(tgtRootMock.isExpired()).thenReturn(false);
        when(tgtRootMock.getAuthentication()).thenReturn(this.authentication);
        return tgtRootMock;
    }

    private TicketGrantingTicket createMockTicketGrantingTicket(final String id, final ServiceTicket svcTicket, final boolean isExpired,
                                                                final TicketGrantingTicket root, final List<Authentication> chainedAuthnList) {
        final TicketGrantingTicket tgtMock = mock(TicketGrantingTicket.class);
        when(tgtMock.isExpired()).thenReturn(isExpired);
        when(tgtMock.getId()).thenReturn(id);

        final String svcId = svcTicket.getService().getId();
        when(tgtMock.grantServiceTicket(anyString(), argThat(new VerifyServiceByIdMatcher(svcId)),
                any(ExpirationPolicy.class), anyBoolean(), anyBoolean())).thenReturn(svcTicket);
        when(tgtMock.getRoot()).thenReturn(root);
        when(tgtMock.getChainedAuthentications()).thenReturn(chainedAuthnList);
        when(tgtMock.getAuthentication()).thenReturn(this.authentication);
        when(svcTicket.getGrantingTicket()).thenReturn(tgtMock);

        return tgtMock;
    }

    private static ServiceTicket createMockServiceTicket(final String id, final Service svc) {
        final ServiceTicket stMock = mock(ServiceTicket.class);
        when(stMock.getService()).thenReturn(svc);
        when(stMock.getId()).thenReturn(id);
        when(stMock.isValidFor(svc)).thenReturn(true);
        return stMock;
    }

    private static RegisteredServiceProxyPolicy getServiceProxyPolicy(final boolean canProxy) {
        if (!canProxy) {
            return new RefuseRegisteredServiceProxyPolicy();
        }

        return new RegexMatchingRegisteredServiceProxyPolicy(".*");
    }

    private static RegisteredService createMockRegisteredService(final String svcId,
                                                                 final boolean enabled, final RegisteredServiceProxyPolicy proxy) {
        final RegisteredService mockRegSvc = mock(RegisteredService.class);
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
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, name);
        return new WebApplicationServiceFactory().createService(request);
    }
}
