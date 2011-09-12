/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.support;

import javax.servlet.http.Cookie;

import org.jasig.cas.authentication.principal.RememberMeCredentials;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.2.1
 *
 */
public final class CookieRetrievingCookieGeneratorTests extends TestCase {
    
    private CookieRetrievingCookieGenerator g;

    protected void setUp() throws Exception {
        this.g = new CookieRetrievingCookieGenerator();
        this.g.setRememberMeMaxAge(100);
        this.g.setCookieDomain("cas.org");
        this.g.setCookieMaxAge(5);
        this.g.setCookiePath("/");
        this.g.setCookieName("test");
    }
    
    public void testCookieAddWithRememberMe() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(RememberMeCredentials.REQUEST_PARAMETER_REMEMBER_ME, "true");
        final MockHttpServletResponse response = new MockHttpServletResponse();
        
       this.g.addCookie(request, response, "test");
        
        final Cookie c = response.getCookie("test");
        assertEquals(100, c.getMaxAge());
        assertEquals("test", c.getValue());
    }
    
    public void testCookieAddWithoutRememberMe() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        
        this.g.addCookie(request, response, "test");
        
        final Cookie c = response.getCookie("test");
        assertEquals(5, c.getMaxAge());
        assertEquals("test", c.getValue());
    }
    
    public void testCookieRetrieve() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final Cookie cookie = new Cookie("test", "test");
        cookie.setDomain("cas.org");
        cookie.setMaxAge(5);
        request.setCookies(new Cookie[] {cookie});
        
        assertEquals("test", this.g.retrieveCookieValue(request));

        
    }
    
    

}
