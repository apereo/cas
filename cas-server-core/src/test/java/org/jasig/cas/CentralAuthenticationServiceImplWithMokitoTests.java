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

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.services.UnauthorizedProxyingException;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;

import java.util.List;


import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import static org.mockito.Mockito.*;

/**
 * Unit tests with the help of Mockito framework.
 *
 * @author Dmitriy Kopylenko
 */
public class CentralAuthenticationServiceImplWithMokitoTests {

    private CentralAuthenticationServiceImpl cas;

    private class VerifyServiceByIdMatcher extends ArgumentMatcher<Service> {
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
        this.cas = new CentralAuthenticationServiceImpl();

        final ServiceTicket stMock = mock(ServiceTicket.class);
        when(stMock.getId()).thenReturn("st-id");
        when(stMock.getService()).thenReturn(TestUtils.getService());

        final TicketGrantingTicket tgtMock = mock(TicketGrantingTicket.class);
        when(tgtMock.isExpired()).thenReturn(false);
        when(tgtMock.grantServiceTicket(anyString(), any(Service.class),
                any(ExpirationPolicy.class), anyBoolean())).thenReturn(stMock);
        final List<Authentication> authnListMock = mock(List.class);
        when(authnListMock.size()).thenReturn(2);
        when(tgtMock.getChainedAuthentications()).thenReturn(authnListMock);

        //Mock TicketRegistry
        final TicketRegistry ticketRegMock = mock(TicketRegistry.class);
        when(ticketRegMock.getTicket(anyString(), eq(TicketGrantingTicket.class))).thenReturn(tgtMock);
        when(ticketRegMock.getTicket(eq(stMock.getId()), eq(ServiceTicket.class))).thenReturn(stMock);

        //Mock ServicesManager
        final RegisteredService mockRegSvc1 = mock(RegisteredService.class);
        when(mockRegSvc1.getServiceId()).thenReturn("test1");
        when(mockRegSvc1.isEnabled()).thenReturn(true);
        when(mockRegSvc1.isAllowedToProxy()).thenReturn(false);

        final RegisteredService mockRegSvc2 = mock(RegisteredService.class);
        when(mockRegSvc2.getServiceId()).thenReturn("test");
        when(mockRegSvc2.isEnabled()).thenReturn(false);

        final ServicesManager smMock = mock(ServicesManager.class);
        when(smMock.findServiceBy(argThat(new VerifyServiceByIdMatcher("test1")))).thenReturn(mockRegSvc1);
        when(smMock.findServiceBy(argThat(new VerifyServiceByIdMatcher("test")))).thenReturn(mockRegSvc2);

        this.cas.setTicketRegistry(ticketRegMock);
        this.cas.setServicesManager(smMock);
    }

    @Test(expected=InvalidTicketException.class)
    public void testNonExistentServiceWhenDelegatingTicketGrantingTicket() throws TicketException {
        this.cas.delegateTicketGrantingTicket("bad-st", TestUtils.getCredentialsWithSameUsernameAndPassword());
    }

    @Test(expected=UnauthorizedProxyingException.class)
    public void testInvalidServiceWhenDelegatingTicketGrantingTicket() throws TicketException {
        this.cas.delegateTicketGrantingTicket("st-id", TestUtils.getCredentialsWithSameUsernameAndPassword());
    }

    @Test(expected=UnauthorizedProxyingException.class)
    public void disallowVendingServiceTicketsWhenServiceIsNotAllowedToProxy_CAS1019() throws TicketException {
        this.cas.grantServiceTicket("tgt-id", TestUtils.getService("test1"));
    }
}
