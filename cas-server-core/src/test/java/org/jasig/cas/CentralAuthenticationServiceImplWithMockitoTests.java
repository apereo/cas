/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas;

import com.google.common.base.Predicates;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.AuthenticationManager;
import org.jasig.cas.authentication.BasicCredentialMetaData;
import org.jasig.cas.authentication.CredentialMetaData;
import org.jasig.cas.authentication.DefaultHandlerResult;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.principal.DefaultPrincipalFactory;
import org.jasig.cas.authentication.principal.Service;
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
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.jasig.cas.util.UniqueTicketIdGenerator;
import org.jasig.cas.validation.Assertion;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
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

    private CentralAuthenticationService cas;
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
    public void prepareNewCAS() {
        this.authentication = mock(Authentication.class);
        when(this.authentication.getAuthenticationDate()).thenReturn(new Date());
        final CredentialMetaData metadata = new BasicCredentialMetaData(TestUtils.getCredentialsWithSameUsernameAndPassword("principal"));
        final Map<String, HandlerResult> successes = new HashMap<>();
        successes.put("handler1", new DefaultHandlerResult(mock(AuthenticationHandler.class), metadata));
        when(this.authentication.getCredentials()).thenReturn(Arrays.asList(metadata));
        when(this.authentication.getSuccesses()).thenReturn(successes);
        when(this.authentication.getPrincipal()).thenReturn(new DefaultPrincipalFactory().createPrincipal(PRINCIPAL));
         
        final Service service1 = TestUtils.getService(SVC1_ID);
        final ServiceTicket stMock = createMockServiceTicket(ST_ID, service1); 
        
        final TicketGrantingTicket tgtRootMock = createRootTicketGrantingTicket();
        
        final TicketGrantingTicket tgtMock = createMockTicketGrantingTicket(TGT_ID, stMock, false,
                tgtRootMock, new ArrayList<Authentication>());
        when(tgtMock.getProxiedBy()).thenReturn(TestUtils.getService("proxiedBy"));

        final List<Authentication> authnListMock = mock(List.class);
        //Size is required to be 2, so that we can simulate proxying capabilities
        when(authnListMock.size()).thenReturn(2);
        when(authnListMock.get(anyInt())).thenReturn(this.authentication);
        when(tgtMock.getChainedAuthentications()).thenReturn(authnListMock);
        when(stMock.getGrantingTicket()).thenReturn(tgtMock);
        
        final Service service2 = TestUtils.getService(SVC2_ID);
        final ServiceTicket stMock2 = createMockServiceTicket(ST2_ID, service2);
        
        final TicketGrantingTicket tgtMock2 = createMockTicketGrantingTicket(TGT2_ID, stMock2, false, tgtRootMock, authnListMock);        
        
        //Mock TicketRegistry
        this.ticketRegMock = mock(TicketRegistry.class);
        when(ticketRegMock.getTicket(eq(tgtMock.getId()), eq(TicketGrantingTicket.class))).thenReturn(tgtMock);
        when(ticketRegMock.getTicket(eq(tgtMock2.getId()), eq(TicketGrantingTicket.class))).thenReturn(tgtMock2);
        when(ticketRegMock.getTicket(eq(stMock.getId()), eq(ServiceTicket.class))).thenReturn(stMock);
        when(ticketRegMock.getTicket(eq(stMock2.getId()), eq(ServiceTicket.class))).thenReturn(stMock2);
        when(ticketRegMock.getTickets()).thenReturn(Arrays.asList(tgtMock, tgtMock2, stMock, stMock2));

        //Mock ServicesManager
        final RegisteredService mockRegSvc1 = createMockRegisteredService(service1.getId(), true, getServiceProxyPolicy(false));
        final RegisteredService mockRegSvc2 = createMockRegisteredService("test", false, getServiceProxyPolicy(true)); 
        final RegisteredService mockRegSvc3 = createMockRegisteredService(service2.getId(), true, getServiceProxyPolicy(true)); 
        
        final ServicesManager smMock = mock(ServicesManager.class);
        when(smMock.findServiceBy(argThat(new VerifyServiceByIdMatcher(service1.getId())))).thenReturn(mockRegSvc1);
        when(smMock.findServiceBy(argThat(new VerifyServiceByIdMatcher("test")))).thenReturn(mockRegSvc2);
        when(smMock.findServiceBy(argThat(new VerifyServiceByIdMatcher(service2.getId())))).thenReturn(mockRegSvc3);
        
        final Map ticketIdGenForServiceMock = mock(Map.class);
        when(ticketIdGenForServiceMock.containsKey(any())).thenReturn(true);
        when(ticketIdGenForServiceMock.get(any())).thenReturn(new DefaultUniqueTicketIdGenerator());
        
        this.cas = new CentralAuthenticationServiceImpl(ticketRegMock, mock(AuthenticationManager.class),
                mock(UniqueTicketIdGenerator.class), ticketIdGenForServiceMock, mock(ExpirationPolicy.class),
                mock(ExpirationPolicy.class), smMock, mock(LogoutManager.class));
    }

    @Test(expected=InvalidTicketException.class)
    public void verifyNonExistentServiceWhenDelegatingTicketGrantingTicket() throws Exception {
        this.cas.delegateTicketGrantingTicket("bad-st", TestUtils.getCredentialsWithSameUsernameAndPassword());
    }

    @Test(expected=UnauthorizedServiceException.class)
    public void verifyInvalidServiceWhenDelegatingTicketGrantingTicket() throws Exception {
        this.cas.delegateTicketGrantingTicket(ST_ID, TestUtils.getCredentialsWithSameUsernameAndPassword());
    }

    @Test(expected=UnauthorizedProxyingException.class)
    public void disallowVendingServiceTicketsWhenServiceIsNotAllowedToProxyCAS1019() throws TicketException {
        this.cas.grantServiceTicket(TGT_ID, TestUtils.getService(SVC1_ID));
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
        final Collection<Ticket> c = this.cas.getTickets(Predicates.alwaysTrue());
        assertEquals(c.size(), this.ticketRegMock.getTickets().size());
    }

    @Test
    public void verifyChainedAuthenticationsOnValidation() throws TicketException {
        final Service svc = TestUtils.getService(SVC2_ID);
        final ServiceTicket st = this.cas.grantServiceTicket(TGT2_ID, svc);
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
                any(ExpirationPolicy.class), anyBoolean())).thenReturn(svcTicket);
        when(tgtMock.getRoot()).thenReturn(root);
        when(tgtMock.getChainedAuthentications()).thenReturn(chainedAuthnList);
        when(svcTicket.getGrantingTicket()).thenReturn(tgtMock);   
        
        return tgtMock;
    }
    
    private ServiceTicket createMockServiceTicket(final String id, final Service svc) {
        final ServiceTicket stMock = mock(ServiceTicket.class);
        when(stMock.getService()).thenReturn(svc);
        when(stMock.getId()).thenReturn(id);
        when(stMock.isValidFor(svc)).thenReturn(true);
        return stMock;
    }
    
    private RegisteredServiceProxyPolicy getServiceProxyPolicy(final boolean canProxy) {
        if (!canProxy) {
            return new RefuseRegisteredServiceProxyPolicy();
        }
        
        return new RegexMatchingRegisteredServiceProxyPolicy(".*");
    }

    private RegisteredService createMockRegisteredService(final String svcId,
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
}
