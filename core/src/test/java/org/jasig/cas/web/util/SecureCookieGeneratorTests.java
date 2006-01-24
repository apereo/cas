/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.util;

import org.springframework.mock.web.MockHttpServletResponse;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 *
 */
public class SecureCookieGeneratorTests extends TestCase {

    private SecureCookieGenerator secureCookieGenerator = new SecureCookieGenerator();
    
    

    protected void setUp() throws Exception {
        this.secureCookieGenerator.setCookieDomain("/");
        this.secureCookieGenerator.setCookieMaxAge(0);
        this.secureCookieGenerator.setCookieName("test");
        this.secureCookieGenerator.setCookiePath("/");
    }

    public void testSettingSecureCookie() {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        this.secureCookieGenerator.setCookieSecure(true);
        this.secureCookieGenerator.addCookie(response, "test");

        assertTrue(response.getCookies()[0].getSecure());
    }

    public void testCookieValue() {
        final String COOKIE_VALUE = "test";
        this.secureCookieGenerator.setCookieValue(COOKIE_VALUE);

        assertEquals(COOKIE_VALUE, this.secureCookieGenerator.getCookieValue());
    }
    
    public void testAddCookieWithSetValue() {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final String COOKIE_VALUE = "test";
        this.secureCookieGenerator.setCookieValue(COOKIE_VALUE);
        this.secureCookieGenerator.addCookie(response);

        assertEquals(COOKIE_VALUE, response.getCookies()[0].getValue());
    }
    
    public void testAddCookieWithNoValue() {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final String COOKIE_VALUE = "";
        this.secureCookieGenerator.setCookieValue(null);
        this.secureCookieGenerator.addCookie(response);

        assertEquals(COOKIE_VALUE, response.getCookies()[0].getValue());
    }
}
