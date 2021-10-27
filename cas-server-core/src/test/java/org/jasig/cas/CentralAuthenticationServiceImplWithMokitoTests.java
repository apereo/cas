/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.AuthenticationManager;
import org.jasig.cas.authentication.BasicCredentialMetaData;
import org.jasig.cas.authentication.CredentialMetaData;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.logout.LogoutManager;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.services.UnauthorizedProxyingException;
import org.jasig.cas.services.UnauthorizedServiceException;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.jasig.cas.util.UniqueTicketIdGenerator;
import org.jasig.cas.validation.Assertion;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests with the help of Mockito framework.
 *
 * @author Dmitriy Kopylenko
 */
public class CentralAuthenticationServiceImplWithMokitoTests {
    private static final String TGT_ID = "tgt-id";
    private static final String TGT2_ID = "tgt2-id";
    
    private static final String ST_ID = "st-id";
    private static final String ST2_ID = "st2-id";
    
    private static final String SVC1_ID = "test1";
    private static final String SVC2_ID = "test2";
    
    private static final String PRINCIPAL = "principal";
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private CentralAuthenticationServiceImpl cas;
    private Authentication authentication;
    
    private static class VerifyServiceByIdMatcher extends ArgumentMatcher<Service> {
        private String id;

        public VerifyServiceByIdMatcher(final String id) {
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
        when(this.authentication.getAuthenticatedDate()).thenReturn(new Date());
        final CredentialMetaData metadata = new BasicCredentialMetaData(TestUtils.getCredentialsWithSameUsernameAndPassword("principal"));
        final Map<String, HandlerResult> successes = new HashMap<String, HandlerResult>();
        successes.put("handler1", new HandlerResult(mock(AuthenticationHandler.class), metadata));
        when(this.authentication.getCredentials()).thenReturn(Arrays.asList(metadata));
        when(this.authentication.getSuccesses()).thenReturn(successes);
        when(this.authentication.getPrincipal()).thenReturn(new SimplePrincipal(PRINCIPAL));
        
        final ServiceTicket stMock = mock(ServiceTicket.class);
        when(stMock.getService()).thenReturn(TestUtils.getService());
        when(stMock.getId()).thenReturn(ST_ID);
        when(stMock.isValidFor(TestUtils.getService())).thenReturn(true);
        
        final TicketGrantingTicket tgtRootMock = mock(TicketGrantingTicket.class);
        when(tgtRootMock.isExpired()).thenReturn(false);
        when(tgtRootMock.getAuthentication()).thenReturn(this.authentication);
        
        final TicketGrantingTicket tgtMock = mock(TicketGrantingTicket.class);
        when(tgtMock.isExpired()).thenReturn(false);
        when(tgtMock.getId()).thenReturn(TGT_ID);
        when(tgtMock.grantServiceTicket(anyString(), argThat(new VerifyServiceByIdMatcher(TestUtils.getService().getId())),
                any(ExpirationPolicy.class), anyBoolean())).thenReturn(stMock);
        when(tgtMock.getRoot()).thenReturn(tgtRootMock);
                
        final List<Authentication> authnListMock = mock(List.class);
        //Size is required to be 2, so that we can simulate proxying capabilities
        when(authnListMock.size()).thenReturn(2);
        when(authnListMock.get(anyInt())).thenReturn(this.authentication);
        when(tgtMock.getChainedAuthentications()).thenReturn(authnListMock);
        when(stMock.getGrantingTicket()).thenReturn(tgtMock);
        
        final Service service2 = TestUtils.getService(SVC2_ID);
        final ServiceTicket stMock2 = mock(ServiceTicket.class);
        when(stMock2.getService()).thenReturn(service2);
        when(stMock2.getId()).thenReturn(ST2_ID);
        when(stMock2.isValidFor(service2)).thenReturn(true);
        
        final TicketGrantingTicket tgtMock2 = mock(TicketGrantingTicket.class);
        when(tgtMock2.isExpired()).thenReturn(false);
        when(tgtMock2.getId()).thenReturn(TGT2_ID);
        when(tgtMock2.grantServiceTicket(anyString(), argThat(new VerifyServiceByIdMatcher(service2.getId())),
                any(ExpirationPolicy.class), anyBoolean())).thenReturn(stMock2);
        when(tgtMock2.getRoot()).thenReturn(tgtRootMock);
        when(tgtMock2.getChainedAuthentications()).thenReturn(authnListMock);
        when(stMock2.getGrantingTicket()).thenReturn(tgtMock2);
        
        
        //Mock TicketRegistry
        final TicketRegistry ticketRegMock = mock(TicketRegistry.class);
        when(ticketRegMock.getTicket(eq(tgtMock.getId()), eq(TicketGrantingTicket.class))).thenReturn(tgtMock);
        when(ticketRegMock.getTicket(eq(tgtMock2.getId()), eq(TicketGrantingTicket.class))).thenReturn(tgtMock2);
        when(ticketRegMock.getTicket(eq(stMock.getId()), eq(ServiceTicket.class))).thenReturn(stMock);
        when(ticketRegMock.getTicket(eq(stMock2.getId()), eq(ServiceTicket.class))).thenReturn(stMock2);
        
        //Mock ServicesManager
        final RegisteredService mockRegSvc1 = mock(RegisteredService.class);
        when(mockRegSvc1.getServiceId()).thenReturn(SVC1_ID);
        when(mockRegSvc1.isEnabled()).thenReturn(true);
        when(mockRegSvc1.isAllowedToProxy()).thenReturn(false);
        when(mockRegSvc1.getName()).thenReturn(SVC1_ID);

        final RegisteredService mockRegSvc2 = mock(RegisteredService.class);
        when(mockRegSvc2.getServiceId()).thenReturn("test");
        when(mockRegSvc2.isEnabled()).thenReturn(false);
        when(mockRegSvc2.getName()).thenReturn("test");

        final RegisteredService mockRegSvc3 = mock(RegisteredService.class);
        when(mockRegSvc3.getServiceId()).thenReturn(service2.getId());
        when(mockRegSvc3.isEnabled()).thenReturn(true);
        when(mockRegSvc3.isAllowedToProxy()).thenReturn(true);
        when(mockRegSvc3.getName()).thenReturn(service2.getId());
        when(mockRegSvc3.matches(argThat(new VerifyServiceByIdMatcher(service2.getId())))).thenReturn(true);
        
        final ServicesManager smMock = mock(ServicesManager.class);
        when(smMock.findServiceBy(argThat(new VerifyServiceByIdMatcher(SVC1_ID)))).thenReturn(mockRegSvc1);
        when(smMock.findServiceBy(argThat(new VerifyServiceByIdMatcher("test")))).thenReturn(mockRegSvc2);
        when(smMock.findServiceBy(argThat(new VerifyServiceByIdMatcher(SVC2_ID)))).thenReturn(mockRegSvc3);
        
        final Map ticketIdGenForServiceMock = mock(Map.class);
        when(ticketIdGenForServiceMock.containsKey(any())).thenReturn(true);
        when(ticketIdGenForServiceMock.get(any())).thenReturn(new DefaultUniqueTicketIdGenerator());
        
        this.cas = new CentralAuthenticationServiceImpl(ticketRegMock, null, mock(AuthenticationManager.class),
                mock(UniqueTicketIdGenerator.class), ticketIdGenForServiceMock, mock(ExpirationPolicy.class),
                mock(ExpirationPolicy.class), smMock, mock(LogoutManager.class));
    }

    @Test(expected=InvalidTicketException.class)
    public void testNonExistentServiceWhenDelegatingTicketGrantingTicket() throws Exception {
        this.cas.delegateTicketGrantingTicket("bad-st", TestUtils.getCredentialsWithSameUsernameAndPassword());
    }

    @Test(expected=UnauthorizedServiceException.class)
    public void testInvalidServiceWhenDelegatingTicketGrantingTicket() throws Exception {
        
        this.cas.delegateTicketGrantingTicket(ST_ID, TestUtils.getCredentialsWithSameUsernameAndPassword());
    }

    @Test(expected=UnauthorizedProxyingException.class)
    public void disallowVendingServiceTicketsWhenServiceIsNotAllowedToProxyCAS1019() throws TicketException {
        this.cas.grantServiceTicket(TGT_ID, TestUtils.getService(SVC1_ID));
    }
    
    @Test
    public void testChainedAuthenticationsOnValidation() throws TicketException {
        final Service svc = TestUtils.getService(SVC2_ID);
        final String st = this.cas.grantServiceTicket(TGT2_ID, svc);
        assertNotNull(st);
        
        final Assertion assertion = this.cas.validateServiceTicket(st, svc);
        assertNotNull(assertion);
        
        assertEquals(assertion.getService(), svc);
        assertEquals(assertion.getPrimaryAuthentication().getPrincipal().getId(), PRINCIPAL);
        assertTrue(assertion.getChainedAuthentications().size()  == 2);
        for (int i = 0; i < assertion.getChainedAuthentications().size(); i++) {
            final Authentication auth = assertion.getChainedAuthentications().get(i);
            assertEquals(auth, authentication);
        }
    }
}
