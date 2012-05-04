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
package org.jasig.cas.web;

import javax.servlet.http.Cookie;

import org.jasig.cas.AbstractCentralAuthenticationServiceTest;
import org.jasig.cas.web.support.CookieRetrievingCookieGenerator;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.servlet.view.RedirectView;
import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class LogoutControllerTests extends AbstractCentralAuthenticationServiceTest {

    private static final String COOKIE_TGC_ID = "CASTGC";

    private LogoutController logoutController;

    private CookieRetrievingCookieGenerator warnCookieGenerator;
    
    private CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;

    @Before
    public void onSetUp() throws Exception {
       this.warnCookieGenerator = new CookieRetrievingCookieGenerator();
        
        this.warnCookieGenerator.setCookieName("test");
        
        this.ticketGrantingTicketCookieGenerator = new CookieRetrievingCookieGenerator();
        this.ticketGrantingTicketCookieGenerator.setCookieName(COOKIE_TGC_ID);
        
        
        this.logoutController = new LogoutController();
        this.logoutController.setCentralAuthenticationService(getCentralAuthenticationService());
        this.logoutController.setLogoutView("test");
        this.logoutController.setWarnCookieGenerator(this.warnCookieGenerator);
        this.logoutController.setTicketGrantingTicketCookieGenerator(this.ticketGrantingTicketCookieGenerator);
    }

    @Test
    public void testLogoutNoCookie() throws Exception {
        assertNotNull(this.logoutController.handleRequestInternal(
            new MockHttpServletRequest(), new MockHttpServletResponse()));
    }

    @Test
    public void testLogoutForServiceWithFollowRedirects() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", "TestService");
        this.logoutController.setFollowServiceRedirects(true);
        assertTrue(this.logoutController.handleRequestInternal(request,
            new MockHttpServletResponse()).getView() instanceof RedirectView);
    }

    @Test
    public void testLogoutForServiceWithNoFollowRedirects() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", "TestService");
        this.logoutController.setFollowServiceRedirects(false);
        assertTrue(!(this.logoutController.handleRequestInternal(request,
            new MockHttpServletResponse()).getView() instanceof RedirectView));
    }

    @Test
    public void testLogoutCookie() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        Cookie cookie = new Cookie(COOKIE_TGC_ID, "test");
        request.setCookies(new Cookie[] {cookie});
        assertNotNull(this.logoutController.handleRequestInternal(request,
            new MockHttpServletResponse()));
    }

}
