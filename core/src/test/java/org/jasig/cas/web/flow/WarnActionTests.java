/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import javax.servlet.http.Cookie;

import org.jasig.cas.TestUtils;
import org.jasig.cas.web.CasArgumentExtractor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.util.CookieGenerator;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class WarnActionTests extends TestCase {

    private static final String COOKIE_PRIVACY = "CASPRIVACY";
    
    private WarnAction warnAction = new WarnAction();
    
    private CookieGenerator warnCookieGenerator;
    
    private CookieGenerator ticketGrantingTicketCookieGenerator;
    
    private CasArgumentExtractor casArgumentExtractor;
    
    protected void setUp() throws Exception {
        this.warnAction = new WarnAction();
        this.warnCookieGenerator = new CookieGenerator();
        
        this.warnCookieGenerator.setCookieName(COOKIE_PRIVACY);
        
        this.ticketGrantingTicketCookieGenerator = new CookieGenerator();
        this.ticketGrantingTicketCookieGenerator.setCookieName("test");
        
        this.casArgumentExtractor = new CasArgumentExtractor(this.ticketGrantingTicketCookieGenerator, this.warnCookieGenerator);
        this.warnAction.setCasArgumentExtractor(this.casArgumentExtractor);
    }

    public void testWarnFromCookie() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie[] {new Cookie(
            COOKIE_PRIVACY,
            "true")});

        assertEquals("warn", this.warnAction.doExecute(
            TestUtils.getContext(request)).getId());
    }

    public void testWarnFromRequestParameter() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(this.casArgumentExtractor.getWarnParameterName(), "true");

        assertEquals("redirect", this.warnAction.doExecute(
            TestUtils.getContext(request)).getId());
    }

    public void testNoWarn() throws Exception {
        assertEquals("redirect", this.warnAction.doExecute(
            TestUtils.getContext()).getId());
    }
}
