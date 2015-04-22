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
import org.jasig.cas.services.RegisteredServiceImpl;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.services.UnauthorizedProxyingException;
import org.jasig.cas.ticket.*;
import org.jasig.cas.ticket.registry.TicketRegistry;


import java.util.List;

import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Unit tests with the help of Mockito framework
 *
 * @author Dmitriy Kopylenko
 */
public class CentralAuthenticationServiceImplWithMokitoTests {

    @Test
    public void disallowVendingServiceTicketsWhenServiceIsNotAllowedToProxy_CAS1019() throws TicketException {
        //Main class under test
        CentralAuthenticationServiceImpl cas = new CentralAuthenticationServiceImpl();

        //Mock ST
        ServiceTicket stMock = mock(ServiceTicket.class);
        when(stMock.getId()).thenReturn("st-id");

        //Mock TGT
        TicketGrantingTicket tgtMock = mock(TicketGrantingTicket.class);
        when(tgtMock.isExpired()).thenReturn(false);
        when(tgtMock.grantServiceTicket(anyString(), any(Service.class), any(ExpirationPolicy.class), anyBoolean())).thenReturn(stMock);
        when(tgtMock.getProxiedBy()).thenReturn("proxiedBy");

        //Mock TicketRegistry
        TicketRegistry ticketRegMock = mock(TicketRegistry.class);
        when(ticketRegMock.getTicket(anyString(), eq(TicketGrantingTicket.class))).thenReturn(tgtMock);

        //Mock ServicesManager
        RegisteredServiceImpl registeredService = new RegisteredServiceImpl();
        registeredService.setAllowedToProxy(false); // <-- criteria for testing the CAS-1019 feature
        ServicesManager smMock = mock(ServicesManager.class);
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
