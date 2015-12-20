package org.jasig.cas;

import com.google.common.base.Predicates;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationContext;
import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.BasicCredentialMetaData;
import org.jasig.cas.authentication.CredentialMetaData;
import org.jasig.cas.authentication.DefaultHandlerResult;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.TestUtils;
import org.jasig.cas.authentication.principal.DefaultPrincipalFactory;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.WebApplicationServiceFactory;
import org.jasig.cas.logout.LogoutManager;
import org.jasig.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.jasig.cas.services.DefaultRegisteredServiceUsernameProvider;
import org.jasig.cas.services.RefuseRegisteredServiceProxyPolicy;
import org.jasig.cas.services.RegexMatchingRegisteredServiceProxyPolicy;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceProxyPolicy;
import org.jasig.cas.services.ReturnAllAttributeReleasePolicy;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.services.UnauthorizedProxyingException;
import org.jasig.cas.services.UnauthorizedServiceException;
import org.jasig.cas.ticket.DefaultProxyGrantingTicketFactory;
import org.jasig.cas.ticket.DefaultProxyTicketFactory;
import org.jasig.cas.ticket.DefaultServiceTicketFactory;
import org.jasig.cas.ticket.DefaultTicketFactory;
import org.jasig.cas.ticket.DefaultTicketGrantingTicketFactory;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.validation.Assertion;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests with the help of Mockito framework.
 *
 * @author Dmitriy Kopylenko
 * @since 3.0.0
 */
public class CentralAuthenticationServiceImplWithMockitoTests {
    private static final String TGT_ID = "tgt-id";
    private static final String TGT2_ID = "tgt2-id";
    
    private static final String ST_ID = "st-id";
    private static final String ST2_ID = "st2-id";
    
    private static final String SVC1_ID = "test1";
    private static final String SVC2_ID = "test2";
    
    private static final String PRINCIPAL = "principal";

    private CentralAuthenticationServiceImpl cas;
    private Authentication authentication;
    private TicketRegistry ticketRegMock;

    private static class VerifyServiceByIdMatcher extends ArgumentMatcher<Service> {
        private final String id;

        VerifyServiceByIdMatcher(final String id) {
            this.id = id;
        }

        @Override
        public boolean matches(final Object argument) {
            final Service s = (Service) argument;
            return s != null && s.getId().equals(this.id);
        }

    }
    
    @Before
    public void prepareNewCAS() throws Exception {
        this.authentication = mock(Authentication.class);
        when(this.authentication.getAuthenticationDate()).thenReturn(DateTime.now());
        final CredentialMetaData metadata = new BasicCredentialMetaData(TestUtils.getCredentialsWithSameUsernameAndPassword("principal"));
        final Map<String, HandlerResult> successes = new HashMap<>();
        successes.put("handler1", new DefaultHandlerResult(mock(AuthenticationHandler.class), metadata));
        when(this.authentication.getCredentials()).thenReturn(Arrays.asList(metadata));
        when(this.authentication.getSuccesses()).thenReturn(successes);
        when(this.authentication.getPrincipal()).thenReturn(new DefaultPrincipalFactory().createPrincipal(PRINCIPAL));
         
        final Service service1 = getService(SVC1_ID);
        final ServiceTicket stMock = createMockServiceTicket(ST_ID, service1); 
        
        final TicketGrantingTicket tgtRootMock = createRootTicketGrantingTicket();
        
        final TicketGrantingTicket tgtMock = createMockTicketGrantingTicket(TGT_ID, stMock, false,
                tgtRootMock, new ArrayList<Authentication>());
        when(tgtMock.getProxiedBy()).thenReturn(getService("proxiedBy"));

        final List<Authentication> authnListMock = mock(List.class);
        //Size is required to be 2, so that we can simulate proxying capabilities
        when(authnListMock.size()).thenReturn(2);
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
        final DefaultTicketFactory factory = new DefaultTicketFactory();
        factory.setTicketGrantingTicketFactory(new DefaultTicketGrantingTicketFactory());
        factory.setProxyGrantingTicketFactory(new DefaultProxyGrantingTicketFactory());
        factory.setServiceTicketFactory(new DefaultServiceTicketFactory());
        factory.setProxyTicketFactory(new DefaultProxyTicketFactory());

        factory.initialize();

        this.cas = new CentralAuthenticationServiceImpl(ticketRegMock,
                factory, smMock, mock(LogoutManager.class));
        this.cas.setApplicationEventPublisher(mock(ApplicationEventPublisher.class));

    }

    private AuthenticationContext getAuthenticationContext() {
        final AuthenticationContext ctx = mock(AuthenticationContext.class);
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

    @Test(expected=InvalidTicketException.class)
    public void verifyNonExistentServiceWhenDelegatingTicketGrantingTicket() throws Exception {
        this.cas.createProxyGrantingTicket("bad-st", getAuthenticationContext());
    }

    @Test(expected=UnauthorizedServiceException.class)
    public void verifyInvalidServiceWhenDelegatingTicketGrantingTicket() throws Exception {
        this.cas.createProxyGrantingTicket(ST_ID, getAuthenticationContext());
    }

    @Test(expected=UnauthorizedProxyingException.class)
    public void disallowVendingServiceTicketsWhenServiceIsNotAllowedToProxyCAS1019() throws Exception {
        this.cas.grantServiceTicket(TGT_ID, org.jasig.cas.services.TestUtils.getService(SVC1_ID), getAuthenticationContext());
    }

    @Test(expected=IllegalArgumentException.class)
    public void getTicketGrantingTicketIfTicketIdIsNull() throws InvalidTicketException {
        this.cas.getTicket(null, TicketGrantingTicket.class);
    }

    @Test(expected=InvalidTicketException.class)
    public void getTicketGrantingTicketIfTicketIdIsMissing() throws InvalidTicketException {
        this.cas.getTicket("TGT-9000", TicketGrantingTicket.class);
    }

    @Test
    public void getTicketsWithNoPredicate() {
        final Collection<Ticket> c = this.cas.getTickets(Predicates.<Ticket>alwaysTrue());
        assertEquals(c.size(), this.ticketRegMock.getTickets().size());
    }

    @Test
    public void verifyChainedAuthenticationsOnValidation() throws Exception {
        final Service svc = org.jasig.cas.services.TestUtils.getService(SVC2_ID);
        final ServiceTicket st = this.cas.grantServiceTicket(TGT2_ID, svc, getAuthenticationContext());
        assertNotNull(st);
        
        final Assertion assertion = this.cas.validateServiceTicket(st.getId(), svc);
        assertNotNull(assertion);
        
        assertEquals(assertion.getService(), svc);
        assertEquals(assertion.getPrimaryAuthentication().getPrincipal().getId(), PRINCIPAL);
        assertTrue(assertion.getChainedAuthentications().size()  == 2);
        for (int i = 0; i < assertion.getChainedAuthentications().size(); i++) {
            final Authentication auth = assertion.getChainedAuthentications().get(i);
            assertEquals(auth, authentication);
        }
    }
    
    private TicketGrantingTicket createRootTicketGrantingTicket() {
        final TicketGrantingTicket tgtRootMock = mock(TicketGrantingTicket.class);
        when(tgtRootMock.isExpired()).thenReturn(false);
        when(tgtRootMock.getAuthentication()).thenReturn(this.authentication);
        return tgtRootMock;
    }
    
    private TicketGrantingTicket createMockTicketGrantingTicket(final String id,
            final ServiceTicket svcTicket, final boolean isExpired, 
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
        request.addParameter("service", name);
        return new WebApplicationServiceFactory().createService(request);
    }


}
