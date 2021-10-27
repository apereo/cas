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
package org.jasig.cas.logout;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.services.LogoutType;
import org.jasig.cas.services.RegisteredServiceImpl;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.util.http.HttpClient;
import org.jasig.cas.util.http.HttpMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.net.URL;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


/**
 * @author Jerome Leleu
 * @since 4.0.0
 */
@RunWith(JUnit4.class)
public class LogoutManagerImplTests {

    private static final String ID = "id";

    private static final String URL = "http://www.github.com";

    private LogoutManagerImpl logoutManager;

    @Mock
    private TicketGrantingTicket tgt;

    private Map<String, Service> services;

    private SimpleWebApplicationServiceImpl simpleWebApplicationServiceImpl;

    private RegisteredServiceImpl registeredService;

    @Mock
    private ServicesManager servicesManager;

    @Mock
    private HttpClient client;

    public LogoutManagerImplTests() {
        MockitoAnnotations.initMocks(this);
    }

    @Before
    public void setUp() {

        when(client.isValidEndPoint(any(String.class))).thenReturn(true);
        when(client.isValidEndPoint(any(URL.class))).thenReturn(true);
        when(client.sendMessageToEndPoint(any(HttpMessage.class))).thenReturn(true);
        this.logoutManager = new LogoutManagerImpl(servicesManager, client, new SamlCompliantLogoutMessageCreator());

        this.services = new HashMap<>();
        this.simpleWebApplicationServiceImpl = new SimpleWebApplicationServiceImpl(URL);
        this.services.put(ID, this.simpleWebApplicationServiceImpl);
        when(this.tgt.getServices()).thenReturn(this.services);

        this.registeredService = new RegisteredServiceImpl();
        when(servicesManager.findServiceBy(this.simpleWebApplicationServiceImpl)).thenReturn(this.registeredService);
    }

    @Test
    public void verifyServiceLogoutUrlIsUsed() throws Exception {
        this.registeredService.setLogoutUrl(new URL("https://www.apereo.org"));
        final Collection<LogoutRequest> logoutRequests = this.logoutManager.performLogout(tgt);
        final LogoutRequest logoutRequest = logoutRequests.iterator().next();
        assertEquals(logoutRequest.getLogoutUrl(), this.registeredService.getLogoutUrl());
    }

    @Test
    public void verifyLogoutDisabled() {
        this.logoutManager.setSingleLogoutCallbacksDisabled(true);
        final Collection<LogoutRequest> logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(0, logoutRequests.size());
    }

    @Test
    public void verifyLogoutAlreadyLoggedOut() {
        this.simpleWebApplicationServiceImpl.setLoggedOutAlready(true);
        final Collection<LogoutRequest> logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(0, logoutRequests.size());
    }

    @Test
    public void verifyLogoutTypeNotSet() {
        final Collection<LogoutRequest> logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(1, logoutRequests.size());
        final LogoutRequest logoutRequest = logoutRequests.iterator().next();
        assertEquals(ID, logoutRequest.getTicketId());
        assertEquals(this.simpleWebApplicationServiceImpl, logoutRequest.getService());
        assertEquals(LogoutRequestStatus.SUCCESS, logoutRequest.getStatus());
    }

    @Test
    public void verifyLogoutTypeBack() {
        this.registeredService.setLogoutType(LogoutType.BACK_CHANNEL);
        final Collection<LogoutRequest> logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(1, logoutRequests.size());
        final LogoutRequest logoutRequest = logoutRequests.iterator().next();
        assertEquals(ID, logoutRequest.getTicketId());
        assertEquals(this.simpleWebApplicationServiceImpl, logoutRequest.getService());
        assertEquals(LogoutRequestStatus.SUCCESS, logoutRequest.getStatus());
    }

    @Test
    public void verifyLogoutTypeNone() {
        this.registeredService.setLogoutType(LogoutType.NONE);
        final Collection<LogoutRequest> logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(0, logoutRequests.size());
    }

    @Test
    public void verifyLogoutTypeNull() {
        this.registeredService.setLogoutType(null);
        final Collection<LogoutRequest> logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(1, logoutRequests.size());
        final LogoutRequest logoutRequest = logoutRequests.iterator().next();
        assertEquals(ID, logoutRequest.getTicketId());
    }

    @Test
    public void verifyLogoutTypeFront() {
        this.registeredService.setLogoutType(LogoutType.FRONT_CHANNEL);
        final Collection<LogoutRequest> logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(1, logoutRequests.size());
        final LogoutRequest logoutRequest = logoutRequests.iterator().next();
        assertEquals(ID, logoutRequest.getTicketId());
        assertEquals(this.simpleWebApplicationServiceImpl, logoutRequest.getService());
        assertEquals(LogoutRequestStatus.NOT_ATTEMPTED, logoutRequest.getStatus());
    }
    
    @Test
    public void verifyAsynchronousLogout() {
        this.registeredService.setLogoutType(LogoutType.BACK_CHANNEL);
        this.logoutManager.setAsynchronous(false);
        final Collection<LogoutRequest> logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(1, logoutRequests.size());
    }
}
