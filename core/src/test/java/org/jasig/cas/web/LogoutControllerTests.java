/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web;

import javax.servlet.http.Cookie;

import org.jasig.cas.CentralAuthenticationServiceImpl;
import org.jasig.cas.ticket.registry.DefaultTicketRegistry;
import org.jasig.cas.web.support.WebConstants;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.view.RedirectView;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class LogoutControllerTests extends TestCase {

    private LogoutController logoutController;

    protected void setUp() throws Exception {
        CentralAuthenticationServiceImpl c = new CentralAuthenticationServiceImpl();
        c.setTicketRegistry(new DefaultTicketRegistry());
        
        this.logoutController = new LogoutController();
        this.logoutController.setCentralAuthenticationService(c);
        this.logoutController.afterPropertiesSet();
    }

    public void testAfterPropertiesSet() {
        LogoutController controller = new LogoutController();
        
        try {
            controller.afterPropertiesSet();
            fail("IllegalArgumentException expected.");
        } catch (Exception e) {
            return;
        }
    }
    
    public void testLogoutNoCookie() throws Exception {
        assertNotNull(this.logoutController.handleRequestInternal(new MockHttpServletRequest(), new MockHttpServletResponse()));
    }
    
    public void testLogoutForService() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", "TestService");
        assertTrue(this.logoutController.handleRequestInternal(request, new MockHttpServletResponse()).getView() instanceof RedirectView);
    }
    
    public void testLogoutCookie() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        Cookie cookie = new Cookie(WebConstants.COOKIE_TGC_ID, "test");
        request.setCookies(new Cookie[] {cookie});
        assertNotNull(this.logoutController.handleRequestInternal(request, new MockHttpServletResponse()));
    }
    
}
