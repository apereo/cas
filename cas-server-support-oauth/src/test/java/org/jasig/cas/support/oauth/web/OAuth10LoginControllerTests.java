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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.jasig.cas.support.oauth.OAuthConfiguration;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.web.flow.OAuthAction;
import org.junit.Test;
import org.scribe.up.provider.OAuthProvider;
import org.scribe.up.provider.impl.TwitterProvider;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This class tests the {@link OAuth10LoginController} class.
 * 
 * @author Jerome Leleu
 * @since 3.5.2
 */
public final class OAuth10LoginControllerTests {
    
    private final static String MY_LOGIN_URL = "http://casserver/login";
    
    @Test
    public void testOK() throws Exception {
        // must be an OAuth 1.0 provider
        final TwitterProvider twitterProvider = new TwitterProvider();
        twitterProvider.setKey("OPEWaSoTuAe49K4dSoRvNw");
        twitterProvider.setSecret("aKmvleltXAmLKcnlMgzRjTsCnhV3QVMVDh153xJttCo");
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", MY_LOGIN_URL);
        mockRequest.setParameter(OAuthConstants.OAUTH_PROVIDER, twitterProvider.getType());
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        final OAuthConfiguration oAuthConfiguration = new OAuthConfiguration();
        oAuthConfiguration.setLoginUrl(MY_LOGIN_URL);
        final List<OAuthProvider> providers = new ArrayList<OAuthProvider>();
        providers.add(twitterProvider);
        oAuthConfiguration.setProviders(providers);
        // use OAuthAction to init oAuthConfiguration (as it's done in its class)
        final OAuthAction oAuthAction = new OAuthAction();
        oAuthAction.setConfiguration(oAuthConfiguration);
        final OAuth10LoginController oAuth10LoginController = new OAuth10LoginController();
        oAuth10LoginController.setConfiguration(oAuthConfiguration);
        final ModelAndView modelAndView = oAuth10LoginController.handleRequest(mockRequest, mockResponse);
        final View view = modelAndView.getView();
        assertTrue(view instanceof RedirectView);
        final RedirectView redirectView = (RedirectView) view;
        assertTrue(redirectView.getUrl().startsWith("https://api.twitter.com/oauth/authorize?oauth_token="));
    }
}
