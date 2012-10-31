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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import junit.framework.TestCase;

import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceImpl;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.scribe.utils.OAuthEncoder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This class tests the {@link OAuth20AuthorizeController} class.
 * 
 * @author Jerome Leleu
 * @since 3.5.2
 */
public final class OAuth20AuthorizeControllerTests extends TestCase {
    
    private static final String CONTEXT = "/oauth2.0/";
    
    private static final String CLIENT_ID = "1";
    
    private static final String REDIRECT_URI = "http://someurl";
    
    private static final String OTHER_REDIRECT_URI = "http://someotherurl";
    
    private static final String CAS_SERVER = "casserver";
    
    private static final String CAS_SCHEME = "https";
    
    private static final int CAS_PORT = 443;
    
    private static final String CAS_URL = CAS_SCHEME + "://" + CAS_SERVER + ":" + CAS_PORT;
    
    private static final String SERVICE_NAME = "serviceName";
    
    private static final String STATE = "state";
    
    public void testNoClientId() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                                                                                     + OAuthConstants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.afterPropertiesSet();
        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuthConstants.ERROR_VIEW, modelAndView.getViewName());
    }
    
    public void testNoRedirectUri() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                                                                                     + OAuthConstants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.afterPropertiesSet();
        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuthConstants.ERROR_VIEW, modelAndView.getViewName());
    }
    
    public void testNoCasService() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                                                                                     + OAuthConstants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        final ServicesManager servicesManager = mock(ServicesManager.class);
        when(servicesManager.getAllServices()).thenReturn(new ArrayList<RegisteredService>());
        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setServicesManager(servicesManager);
        oauth20WrapperController.afterPropertiesSet();
        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuthConstants.ERROR_VIEW, modelAndView.getViewName());
    }
    
    public void testRedirectUriDoesNotStartWithServiceId() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                                                                                     + OAuthConstants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        final ServicesManager servicesManager = mock(ServicesManager.class);
        final RegisteredServiceImpl registeredServiceImpl = new RegisteredServiceImpl();
        registeredServiceImpl.setName(CLIENT_ID);
        registeredServiceImpl.setServiceId(OTHER_REDIRECT_URI);
        final List<RegisteredService> services = new ArrayList<RegisteredService>();
        services.add(registeredServiceImpl);
        when(servicesManager.getAllServices()).thenReturn(services);
        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setServicesManager(servicesManager);
        oauth20WrapperController.afterPropertiesSet();
        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuthConstants.ERROR_VIEW, modelAndView.getViewName());
    }
    
    public void testOK() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                                                                                     + OAuthConstants.AUTHORIZE_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.STATE, STATE);
        mockRequest.setServerName(CAS_SERVER);
        mockRequest.setServerPort(CAS_PORT);
        mockRequest.setScheme(CAS_SCHEME);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        final ServicesManager servicesManager = mock(ServicesManager.class);
        final RegisteredServiceImpl registeredServiceImpl = new RegisteredServiceImpl();
        registeredServiceImpl.setName(CLIENT_ID);
        registeredServiceImpl.setServiceId(REDIRECT_URI);
        registeredServiceImpl.setTheme(SERVICE_NAME);
        final List<RegisteredService> services = new ArrayList<RegisteredService>();
        services.add(registeredServiceImpl);
        when(servicesManager.getAllServices()).thenReturn(services);
        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.setLoginUrl(CAS_URL);
        oauth20WrapperController.setServicesManager(servicesManager);
        oauth20WrapperController.afterPropertiesSet();
        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        final HttpSession session = mockRequest.getSession();
        assertEquals(REDIRECT_URI, (String) session.getAttribute(OAuthConstants.OAUTH20_CALLBACKURL));
        assertEquals(SERVICE_NAME, (String) session.getAttribute(OAuthConstants.OAUTH20_SERVICE_NAME));
        assertEquals(STATE, (String) session.getAttribute(OAuthConstants.OAUTH20_STATE));
        final View view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        final RedirectView redirectView = (RedirectView) view;
        assertEquals(CAS_URL + "?service="
                         + OAuthEncoder.encode(CAS_URL + CONTEXT + OAuthConstants.CALLBACK_AUTHORIZE_URL),
                     redirectView.getUrl());
    }
}
