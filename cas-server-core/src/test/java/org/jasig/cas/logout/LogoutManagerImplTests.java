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
package org.jasig.cas.logout;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.services.LogoutType;
import org.jasig.cas.services.RegisteredServiceImpl;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.util.SimpleHttpClient;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Jerome Leleu
 * @since 4.0.0
 */
public class LogoutManagerImplTests {

    private static final String ID = "id";

    private static final String URL = "http://url";

    private LogoutManagerImpl logoutManager;

    private TicketGrantingTicket tgt;

    private Map<String, Service> services;

    private SimpleWebApplicationServiceImpl simpleWebApplicationServiceImpl;

    private RegisteredServiceImpl registeredService;

    @Before
    public void setUp() {
        final ServicesManager servicesManager = mock(ServicesManager.class);
        this.logoutManager = new LogoutManagerImpl(servicesManager, new SimpleHttpClient(), new SamlCompliantLogoutMessageCreator());
        this.tgt = mock(TicketGrantingTicket.class);
        this.services = new HashMap<String, Service>();
        this.simpleWebApplicationServiceImpl = new SimpleWebApplicationServiceImpl(URL);
        this.services.put(ID, this.simpleWebApplicationServiceImpl);
        when(this.tgt.getServices()).thenReturn(this.services);
        this.registeredService = new RegisteredServiceImpl();
        when(servicesManager.findServiceBy(this.simpleWebApplicationServiceImpl)).thenReturn(this.registeredService);
    }

    @Test
    public void testLogoutDisabled() {
        this.logoutManager.setDisableSingleSignOut(true);
        final Collection<LogoutRequest> logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(0, logoutRequests.size());
    }

    @Test
    public void testLogoutAlreadyLoggedOut() {
        this.simpleWebApplicationServiceImpl.setLoggedOutAlready(true);
        final Collection<LogoutRequest> logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(0, logoutRequests.size());
    }

    @Test
    public void testLogoutTypeNull() {
        final Collection<LogoutRequest> logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(1, logoutRequests.size());
        final LogoutRequest logoutRequest = logoutRequests.iterator().next();
        assertEquals(ID, logoutRequest.getTicketId());
        assertEquals(this.simpleWebApplicationServiceImpl, logoutRequest.getService());
        assertEquals(LogoutRequestStatus.SUCCESS, logoutRequest.getStatus());
        
    }

    @Test
    public void testLogoutTypeBack() {
        this.registeredService.setLogoutType(LogoutType.BACK_CHANNEL);
        final Collection<LogoutRequest> logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(1, logoutRequests.size());
        final LogoutRequest logoutRequest = logoutRequests.iterator().next();
        assertEquals(ID, logoutRequest.getTicketId());
        assertEquals(this.simpleWebApplicationServiceImpl, logoutRequest.getService());
        assertEquals(LogoutRequestStatus.SUCCESS, logoutRequest.getStatus());
    }

    @Test
    public void testLogoutTypeFront() {
        this.registeredService.setLogoutType(LogoutType.FRONT_CHANNEL);
        final Collection<LogoutRequest> logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(1, logoutRequests.size());
        final LogoutRequest logoutRequest = logoutRequests.iterator().next();
        assertEquals(ID, logoutRequest.getTicketId());
        assertEquals(this.simpleWebApplicationServiceImpl, logoutRequest.getService());
        assertEquals(LogoutRequestStatus.NOT_ATTEMPTED, logoutRequest.getStatus());
    }
}
