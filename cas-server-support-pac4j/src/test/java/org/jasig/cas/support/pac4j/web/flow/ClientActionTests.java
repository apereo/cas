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
package org.jasig.cas.support.pac4j.web.flow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.junit.Test;
import org.pac4j.core.client.Clients;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.TwitterClient;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

/**
 * This class tests the {@link ClientAction} class.
 *
 * @author Jerome Leleu
 * @since 3.5.2
 */
public final class ClientActionTests {

    private static final String MY_KEY = "my_key";

    private static final String MY_SECRET = "my_secret";

    private static final String MY_LOGIN_URL = "http://casserver/login";

    private static final String MY_SERVICE = "http://myservice";

    private static final String MY_THEME = "my_theme";

    private static final String MY_LOCALE = "fr";

    private static final String MY_METHOD = "POST";

    @Test
    public void testStartAuthentication() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setParameter(ClientAction.THEME, MY_THEME);
        mockRequest.setParameter(ClientAction.LOCALE, MY_LOCALE);
        mockRequest.setParameter(ClientAction.METHOD, MY_METHOD);

        final MockHttpSession mockSession = new MockHttpSession();
        mockRequest.setSession(mockSession);

        final ServletExternalContext servletExternalContext = mock(ServletExternalContext.class);
        when(servletExternalContext.getNativeRequest()).thenReturn(mockRequest);

        final MockRequestContext mockRequestContext = new MockRequestContext();
        mockRequestContext.setExternalContext(servletExternalContext);
        mockRequestContext.getFlowScope().put(ClientAction.SERVICE, new SimpleWebApplicationServiceImpl(MY_SERVICE));

        final FacebookClient facebookClient = new FacebookClient(MY_KEY, MY_SECRET);
        final TwitterClient twitterClient = new TwitterClient(MY_KEY, MY_SECRET);
        final Clients clients = new Clients(MY_LOGIN_URL, facebookClient, twitterClient);
        final ClientAction action = new ClientAction(mock(CentralAuthenticationService.class), clients);

        final Event event = action.execute(mockRequestContext);
        assertEquals("error", event.getId());
        assertEquals(MY_THEME, mockSession.getAttribute(ClientAction.THEME));
        assertEquals(MY_LOCALE, mockSession.getAttribute(ClientAction.LOCALE));
        assertEquals(MY_METHOD, mockSession.getAttribute(ClientAction.METHOD));
        final MutableAttributeMap flowScope = mockRequestContext.getFlowScope();
        assertTrue(((String) flowScope.get("FacebookClientUrl"))
                .startsWith("https://www.facebook.com/dialog/oauth?client_id=my_key&redirect_uri=http%3A%2F%2Fcasserver%2Flogin%3F"
                        + Clients.DEFAULT_CLIENT_NAME_PARAMETER + "%3DFacebookClient&state="));
        assertEquals(MY_LOGIN_URL + "?" + Clients.DEFAULT_CLIENT_NAME_PARAMETER
                + "=TwitterClient&needs_client_redirection=true", flowScope.get("TwitterClientUrl"));
    }

    @Test
    public void testFinishAuthentication() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setParameter(Clients.DEFAULT_CLIENT_NAME_PARAMETER, "FacebookClient");

        final MockHttpSession mockSession = new MockHttpSession();
        mockSession.setAttribute(ClientAction.THEME, MY_THEME);
        mockSession.setAttribute(ClientAction.LOCALE, MY_LOCALE);
        mockSession.setAttribute(ClientAction.METHOD, MY_METHOD);
        final Service service = new SimpleWebApplicationServiceImpl(MY_SERVICE);
        mockSession.setAttribute(ClientAction.SERVICE, service);
        mockRequest.setSession(mockSession);

        final ServletExternalContext servletExternalContext = mock(ServletExternalContext.class);
        when(servletExternalContext.getNativeRequest()).thenReturn(mockRequest);

        final MockRequestContext mockRequestContext = new MockRequestContext();
        mockRequestContext.setExternalContext(servletExternalContext);

        final FacebookClient facebookClient = new MockFacebookClient();
        final Clients clients = new Clients(MY_LOGIN_URL, facebookClient);

        final ClientAction action = new ClientAction(mock(CentralAuthenticationService.class), clients);
        final Event event = action.execute(mockRequestContext);
        assertEquals("success", event.getId());
        assertEquals(MY_THEME, mockRequest.getAttribute(ClientAction.THEME));
        assertEquals(MY_LOCALE, mockRequest.getAttribute(ClientAction.LOCALE));
        assertEquals(MY_METHOD, mockRequest.getAttribute(ClientAction.METHOD));
        final MutableAttributeMap flowScope = mockRequestContext.getFlowScope();
        assertEquals(service, flowScope.get(ClientAction.SERVICE));
    }
}
