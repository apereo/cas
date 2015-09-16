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

import java.util.Map;

import org.jasig.cas.support.oauth.OAuthConstants;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;

/**
 * This class tests the {@link OAuth20CallbackAuthorizeController} class.
 *
 * @author Jerome Leleu
 * @since 3.5.2
 */
public final class OAuth20CallbackAuthorizeControllerTests {

    private static final String CONTEXT = "/oauth2.0/";

    private static final String SERVICE_TICKET = "ST-1";

    private static final String REDIRECT_URI = "http://someurl";

    private static final String SERVICE_NAME = "serviceName";

    private static final String STATE = "state";

    @Test
    public void verifyOK() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest(
                "GET",
                CONTEXT
                + OAuthConstants.CALLBACK_AUTHORIZE_URL);
        mockRequest.addParameter(OAuthConstants.TICKET, SERVICE_TICKET);
        final MockHttpSession mockSession = new MockHttpSession();
        mockSession.putValue(OAuthConstants.OAUTH20_CALLBACKURL, REDIRECT_URI);
        mockSession.putValue(OAuthConstants.OAUTH20_SERVICE_NAME, SERVICE_NAME);
        mockRequest.setSession(mockSession);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.afterPropertiesSet();
        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuthConstants.CONFIRM_VIEW, modelAndView.getViewName());
        final Map<String, Object> map = modelAndView.getModel();
        assertEquals(SERVICE_NAME, map.get("serviceName"));
        assertEquals(REDIRECT_URI + "?" + OAuthConstants.CODE + "=" + SERVICE_TICKET, map.get("callbackUrl"));
    }

    @Test
    public void verifyOKWithState() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest(
                "GET",
                CONTEXT
                + OAuthConstants.CALLBACK_AUTHORIZE_URL);
        mockRequest.addParameter(OAuthConstants.TICKET, SERVICE_TICKET);
        final MockHttpSession mockSession = new MockHttpSession();
        mockSession.putValue(OAuthConstants.OAUTH20_CALLBACKURL, REDIRECT_URI);
        mockSession.putValue(OAuthConstants.OAUTH20_SERVICE_NAME, SERVICE_NAME);
        mockSession.putValue(OAuthConstants.OAUTH20_STATE, STATE);
        mockRequest.setSession(mockSession);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.afterPropertiesSet();
        final ModelAndView modelAndView = oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(OAuthConstants.CONFIRM_VIEW, modelAndView.getViewName());
        final Map<String, Object> map = modelAndView.getModel();
        assertEquals(SERVICE_NAME, map.get("serviceName"));
        assertEquals(REDIRECT_URI + "?" + OAuthConstants.CODE + "=" + SERVICE_TICKET + "&" + OAuthConstants.STATE + "="
                + STATE, map.get("callbackUrl"));
    }
}
