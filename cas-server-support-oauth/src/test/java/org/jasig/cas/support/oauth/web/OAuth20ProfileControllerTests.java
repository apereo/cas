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
package org.jasig.cas.support.oauth.web;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * This class tests the {@link OAuth20ProfileController} class.
 *
 * @author Jerome Leleu
 * @since 3.5.2
 */
public final class OAuth20ProfileControllerTests {

    private static final String CONTEXT = "/oauth2.0/";

    private static final String ID = "1234";

    private static final String TGT_ID = "TGT-1";

    private static final String NAME = "attributeName";

    private static final String NAME2 = "attributeName2";

    private static final String VALUE = "attributeValue";

    private static final String CONTENT_TYPE = "application/json";

    @Test
    public void testNoAccessToken() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.PROFILE_URL);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.afterPropertiesSet();
        oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(200, mockResponse.getStatus());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());
        assertEquals("{\"error\":\"" + OAuthConstants.MISSING_ACCESS_TOKEN + "\"}", mockResponse.getContentAsString());
    }

    @Test
    public void testNoTicketGrantingTicketImpl() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.PROFILE_URL);
        mockRequest.setParameter(OAuthConstants.ACCESS_TOKEN, TGT_ID);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        final TicketRegistry ticketRegistry = mock(TicketRegistry.class);
        when(ticketRegistry.getTicket(TGT_ID)).thenReturn(null);
        oauth20WrapperController.setTicketRegistry(ticketRegistry);
        oauth20WrapperController.afterPropertiesSet();
        oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(200, mockResponse.getStatus());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());
        assertEquals("{\"error\":\"" + OAuthConstants.EXPIRED_ACCESS_TOKEN + "\"}", mockResponse.getContentAsString());
    }

    @Test
    public void testExpiredTicketGrantingTicketImpl() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.PROFILE_URL);
        mockRequest.setParameter(OAuthConstants.ACCESS_TOKEN, TGT_ID);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        final TicketRegistry ticketRegistry = mock(TicketRegistry.class);
        final TicketGrantingTicket ticketGrantingTicket = mock(TicketGrantingTicket.class);
        when(ticketGrantingTicket.isExpired()).thenReturn(true);
        when(ticketRegistry.getTicket(TGT_ID)).thenReturn(ticketGrantingTicket);
        oauth20WrapperController.setTicketRegistry(ticketRegistry);
        oauth20WrapperController.afterPropertiesSet();
        oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(200, mockResponse.getStatus());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());
        assertEquals("{\"error\":\"" + OAuthConstants.EXPIRED_ACCESS_TOKEN + "\"}", mockResponse.getContentAsString());
    }

    @Test
    public void testOK() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.PROFILE_URL);
        mockRequest.setParameter(OAuthConstants.ACCESS_TOKEN, TGT_ID);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        final TicketRegistry ticketRegistry = mock(TicketRegistry.class);
        final TicketGrantingTicket ticketGrantingTicket = mock(TicketGrantingTicket.class);
        when(ticketGrantingTicket.isExpired()).thenReturn(false);
        when(ticketRegistry.getTicket(TGT_ID)).thenReturn(ticketGrantingTicket);
        final Authentication authentication = mock(Authentication.class);
        final Principal principal = mock(Principal.class);
        when(principal.getId()).thenReturn(ID);
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put(NAME, VALUE);
        List<String> list = Arrays.asList(VALUE, VALUE);
        map.put(NAME2, list);
        when(principal.getAttributes()).thenReturn(map);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(ticketGrantingTicket.getAuthentication()).thenReturn(authentication);
        oauth20WrapperController.setTicketRegistry(ticketRegistry);
        oauth20WrapperController.afterPropertiesSet();
        oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(200, mockResponse.getStatus());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());
        assertEquals("{\"id\":\"" + ID + "\",\"attributes\":[{\"" + NAME + "\":\"" + VALUE + "\"},{\"" + NAME2
                + "\":[\"" + VALUE + "\",\"" + VALUE + "\"]}]}", mockResponse.getContentAsString());
    }
}
