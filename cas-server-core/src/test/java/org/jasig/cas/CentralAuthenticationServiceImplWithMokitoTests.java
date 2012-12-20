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

import java.util.List;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.service.Service;
import org.jasig.cas.services.RegisteredServiceImpl;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.services.UnauthorizedProxyingException;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.junit.Test;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests with the help of Mockito framework
 *
 * @author Dmitriy Kopylenko
 */
public class CentralAuthenticationServiceImplWithMokitoTests {

    @Test
    public void disallowVendingServiceTicketsWhenServiceIsNotAllowedToProxy_CAS1019() throws TicketException {
        //Main class under test
        final CentralAuthenticationServiceImpl cas = new CentralAuthenticationServiceImpl();

        //Mock ST
        final ServiceTicket stMock = mock(ServiceTicket.class);
        when(stMock.getId()).thenReturn("st-id");

        //Mock TGT
        final TicketGrantingTicket tgtMock = mock(TicketGrantingTicket.class);
        when(tgtMock.isExpired()).thenReturn(false);
        when(tgtMock.grantServiceTicket(anyString(), any(Service.class), any(ExpirationPolicy.class), anyBoolean())).thenReturn(stMock);
        final List<Authentication> authnListMock = mock(List.class);
        when(authnListMock.size()).thenReturn(2); // <-- criteria for testing the CAS-1019 feature
        when(tgtMock.getChainedAuthentications()).thenReturn(authnListMock);

        //Mock TicketRegistry
        final TicketRegistry ticketRegMock = mock(TicketRegistry.class);
        when(ticketRegMock.getTicket(anyString(), eq(TicketGrantingTicket.class))).thenReturn(tgtMock);

        //Mock ServicesManager
        final RegisteredServiceImpl registeredService = new RegisteredServiceImpl();
        registeredService.setAllowedToProxy(false); // <-- criteria for testing the CAS-1019 feature
        final ServicesManager smMock = mock(ServicesManager.class);
        when(smMock.findServiceBy(any(Service.class))).thenReturn(registeredService);

        //Set the stubbed dependencies
        cas.setTicketRegistry(ticketRegMock);
        cas.setServicesManager(smMock);

        //Finally, test the feature
        try{
            cas.grantServiceTicket("tgt-id", TestUtils.getService());
            fail("Should have thrown UnauthorizedProxyingException");
        }
        catch (UnauthorizedProxyingException e) {
            //Expected
        }
    }
}
