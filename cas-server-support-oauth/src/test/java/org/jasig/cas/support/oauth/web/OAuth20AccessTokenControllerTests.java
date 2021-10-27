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
package org.jasig.cas.support.oauth.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * This class tests the {@link OAuth20AccessTokenController} class.
 *
 * @author Jerome Leleu
 * @since 3.5.2
 */
public final class OAuth20AccessTokenControllerTests {

    private static final String CONTEXT = "/oauth2.0/";

    private static final String CLIENT_ID = "1";

    private static final String CLIENT_SECRET = "secret";

    private static final String WRONG_CLIENT_SECRET = "wrongSecret";

    private static final String CODE = "ST-1";

    private static final String TGT_ID = "TGT-1";

    private static final String REDIRECT_URI = "http://someurl";

    private static final String OTHER_REDIRECT_URI = "http://someotherurl";

    private static final int TIMEOUT = 7200;

    @Test
    public void verifyNoClientId() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.CODE, CODE);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.afterPropertiesSet();
        oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(400, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyNoRedirectUri() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.CODE, CODE);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.afterPropertiesSet();
        oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(400, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyNoClientSecret() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.CODE, CODE);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.afterPropertiesSet();
        oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(400, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyNoCode() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.afterPropertiesSet();
        oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(400, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyNoCasService() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.CODE, CODE);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        final ServicesManager servicesManager = mock(ServicesManager.class);
        when(servicesManager.getAllServices()).thenReturn(new ArrayList<RegisteredService>());
        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setServicesManager(servicesManager);
        oauth20WrapperController.afterPropertiesSet();
        oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(400, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyRedirectUriDoesNotStartWithServiceId() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.CODE, CODE);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        final ServicesManager servicesManager = mock(ServicesManager.class);
        final List<RegisteredService> services = new ArrayList<>();
        services.add(getRegisteredService(OTHER_REDIRECT_URI, CLIENT_SECRET));
        when(servicesManager.getAllServices()).thenReturn(services);
        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setServicesManager(servicesManager);
        oauth20WrapperController.afterPropertiesSet();
        oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(400, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyWrongSecret() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.CODE, CODE);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        final ServicesManager servicesManager = mock(ServicesManager.class);
        final List<RegisteredService> services = new ArrayList<>();
        services.add(getRegisteredService(REDIRECT_URI, WRONG_CLIENT_SECRET));
        when(servicesManager.getAllServices()).thenReturn(services);
        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setServicesManager(servicesManager);
        oauth20WrapperController.afterPropertiesSet();
        oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(400, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyNoServiceTicket() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.CODE, CODE);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        final ServicesManager servicesManager = mock(ServicesManager.class);
        final List<RegisteredService> services = new ArrayList<>();
        services.add(getRegisteredService(REDIRECT_URI, CLIENT_SECRET));
        when(servicesManager.getAllServices()).thenReturn(services);
        final TicketRegistry ticketRegistry = mock(TicketRegistry.class);
        when(ticketRegistry.getTicket(CODE)).thenReturn(null);
        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setServicesManager(servicesManager);
        oauth20WrapperController.setTicketRegistry(ticketRegistry);
        oauth20WrapperController.afterPropertiesSet();
        oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(400, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_GRANT, mockResponse.getContentAsString());
    }

    @Test
    public void verifyExpiredServiceTicket() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.CODE, CODE);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        final ServicesManager servicesManager = mock(ServicesManager.class);
        final List<RegisteredService> services = new ArrayList<>();
        services.add(getRegisteredService(REDIRECT_URI, CLIENT_SECRET));
        when(servicesManager.getAllServices()).thenReturn(services);
        final TicketRegistry ticketRegistry = mock(TicketRegistry.class);
        final ServiceTicket serviceTicket = mock(ServiceTicket.class);
        when(serviceTicket.isExpired()).thenReturn(true);
        when(ticketRegistry.getTicket(CODE)).thenReturn(serviceTicket);
        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setServicesManager(servicesManager);
        oauth20WrapperController.setTicketRegistry(ticketRegistry);
        oauth20WrapperController.afterPropertiesSet();
        oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(400, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_GRANT, mockResponse.getContentAsString());
    }

    @Test
    public void verifyOK() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.CODE, CODE);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        final ServicesManager servicesManager = mock(ServicesManager.class);
        final List<RegisteredService> services = new ArrayList<>();
        services.add(getRegisteredService(REDIRECT_URI, CLIENT_SECRET));
        when(servicesManager.getAllServices()).thenReturn(services);
        final TicketRegistry ticketRegistry = mock(TicketRegistry.class);
        final ServiceTicket serviceTicket = mock(ServiceTicket.class);
        final TicketGrantingTicket ticketGrantingTicket = mock(TicketGrantingTicket.class);
        // 10 seconds
        final int timeBefore = 10;
        when(ticketGrantingTicket.getCreationTime()).thenReturn(System.currentTimeMillis() - timeBefore * 1000);
        when(ticketGrantingTicket.getId()).thenReturn(TGT_ID);
        when(serviceTicket.isExpired()).thenReturn(false);
        when(serviceTicket.getId()).thenReturn(CODE);
        when(serviceTicket.getGrantingTicket()).thenReturn(ticketGrantingTicket);
        when(ticketRegistry.getTicket(CODE)).thenReturn(serviceTicket);
        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setServicesManager(servicesManager);
        oauth20WrapperController.setTicketRegistry(ticketRegistry);
        oauth20WrapperController.setTimeout(TIMEOUT);
        oauth20WrapperController.afterPropertiesSet();
        oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        verify(ticketRegistry).deleteTicket(CODE);
        assertEquals("text/plain", mockResponse.getContentType());
        assertEquals(200, mockResponse.getStatus());
        final String body = mockResponse.getContentAsString();
        assertTrue(body.startsWith(OAuthConstants.ACCESS_TOKEN + "=" + TGT_ID + "&" + OAuthConstants.EXPIRES + "="));
        // delta = 2 seconds
        final int delta = 2;
        final int timeLeft = Integer.parseInt(StringUtils.substringAfter(body, "&" + OAuthConstants.EXPIRES + "="));
        assertTrue(timeLeft >= TIMEOUT - timeBefore - delta);
        assertTrue(timeLeft <= TIMEOUT - timeBefore + delta);
    }

    private RegisteredService getRegisteredService(final String serviceId, final String secret) {
        final OAuthRegisteredService registeredServiceImpl = new OAuthRegisteredService();
        registeredServiceImpl.setName("The registered service name");
        registeredServiceImpl.setServiceId(serviceId);
        registeredServiceImpl.setClientId(CLIENT_ID);
        registeredServiceImpl.setClientSecret(secret);
        return registeredServiceImpl;
    }
}
