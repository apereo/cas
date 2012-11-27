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
package org.jasig.cas.support.oauth.web.flow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.support.oauth.OAuthConfiguration;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.junit.Test;
import org.scribe.up.provider.OAuthProvider;
import org.scribe.up.provider.impl.FacebookProvider;
import org.scribe.up.provider.impl.TwitterProvider;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

/**
 * This class tests the {@link OAuthAction} class.
 * 
 * @author Jerome Leleu
 * @since 3.5.2
 */
public final class OAuthActionTests {
    
    private final static String MY_KEY = "my_key";
    
    private final static String MY_SECRET = "my_secret";
    
    private final static String MY_LOGIN_URL = "http://casserver/login";
    
    private final static String MY_SERVICE = "http://myservice";
    
    private final static String MY_THEME = "my_theme";
    
    private final static String MY_LOCALE = "fr";
    
    private final static String MY_METHOD = "POST";
    
    private OAuthConfiguration newConfiguration() {
        final FacebookProvider facebookProvider = new FacebookProvider();
        facebookProvider.setKey(MY_KEY);
        facebookProvider.setSecret(MY_SECRET);
        final TwitterProvider twitterProvider = new TwitterProvider();
        twitterProvider.setKey(MY_KEY);
        twitterProvider.setSecret(MY_SECRET);
        final OAuthConfiguration oAuthConfiguration = new OAuthConfiguration();
        oAuthConfiguration.setLoginUrl(MY_LOGIN_URL);
        final List<OAuthProvider> providers = new ArrayList<OAuthProvider>();
        providers.add(facebookProvider);
        providers.add(twitterProvider);
        oAuthConfiguration.setProviders(providers);
        return oAuthConfiguration;
    }
    
    @Test
    public void testStartAuthentication() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setParameter(OAuthConstants.THEME, MY_THEME);
        mockRequest.setParameter(OAuthConstants.LOCALE, MY_LOCALE);
        mockRequest.setParameter(OAuthConstants.METHOD, MY_METHOD);
        
        final MockHttpSession mockSession = new MockHttpSession();
        mockRequest.setSession(mockSession);
        
        final ServletExternalContext servletExternalContext = mock(ServletExternalContext.class);
        when(servletExternalContext.getNativeRequest()).thenReturn(mockRequest);
        
        final MockRequestContext mockRequestContext = new MockRequestContext();
        mockRequestContext.setExternalContext(servletExternalContext);
        mockRequestContext.getFlowScope().put(OAuthConstants.SERVICE, new SimpleWebApplicationServiceImpl(MY_SERVICE));
        
        final OAuthAction oAuthAction = new OAuthAction();
        oAuthAction.setConfiguration(newConfiguration());
        final Event event = oAuthAction.execute(mockRequestContext);
        assertEquals("error", event.getId());
        assertEquals(MY_THEME, mockSession.getAttribute(OAuthConstants.THEME));
        assertEquals(MY_LOCALE, mockSession.getAttribute(OAuthConstants.LOCALE));
        assertEquals(MY_METHOD, mockSession.getAttribute(OAuthConstants.METHOD));
        final MutableAttributeMap flowScope = mockRequestContext.getFlowScope();
        assertTrue(((String) flowScope.get("FacebookProviderUrl"))
            .startsWith("https://www.facebook.com/dialog/oauth?client_id=my_key&redirect_uri=http%3A%2F%2Fcasserver%2Flogin%3Foauth_provider%3DFacebookProvider&state="));
        assertEquals("/oauth10login?oauth_provider=TwitterProvider", flowScope.get("TwitterProviderUrl"));
    }
    
    @Test
    public void testFinishAuthentication() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setParameter(OAuthConstants.OAUTH_PROVIDER, "FacebookProvider");
        
        final MockHttpSession mockSession = new MockHttpSession();
        mockSession.setAttribute(OAuthConstants.THEME, MY_THEME);
        mockSession.setAttribute(OAuthConstants.LOCALE, MY_LOCALE);
        mockSession.setAttribute(OAuthConstants.METHOD, MY_METHOD);
        final Service service = new SimpleWebApplicationServiceImpl(MY_SERVICE);
        mockSession.setAttribute(OAuthConstants.SERVICE, service);
        mockRequest.setSession(mockSession);
        
        final ServletExternalContext servletExternalContext = mock(ServletExternalContext.class);
        when(servletExternalContext.getNativeRequest()).thenReturn(mockRequest);
        
        final MockRequestContext mockRequestContext = new MockRequestContext();
        mockRequestContext.setExternalContext(servletExternalContext);
        
        final CentralAuthenticationService centralAuthenticationService = mock(CentralAuthenticationService.class);
        
        final OAuthAction oAuthAction = new OAuthAction();
        oAuthAction.setConfiguration(newConfiguration());
        oAuthAction.setCentralAuthenticationService(centralAuthenticationService);
        final Event event = oAuthAction.execute(mockRequestContext);
        assertEquals("success", event.getId());
        assertEquals(MY_THEME, mockRequest.getAttribute(OAuthConstants.THEME));
        assertEquals(MY_LOCALE, mockRequest.getAttribute(OAuthConstants.LOCALE));
        assertEquals(MY_METHOD, mockRequest.getAttribute(OAuthConstants.METHOD));
        final MutableAttributeMap flowScope = mockRequestContext.getFlowScope();
        assertEquals(service, flowScope.get(OAuthConstants.SERVICE));
    }
}
