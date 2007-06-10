/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.flow;

import javax.servlet.http.Cookie;

import org.jasig.cas.TestUtils;
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

    protected void setUp() throws Exception {
        this.warnAction = new WarnAction();
        this.warnCookieGenerator = new CookieGenerator();

        this.warnCookieGenerator.setCookieName(COOKIE_PRIVACY);

        this.ticketGrantingTicketCookieGenerator = new CookieGenerator();
        this.ticketGrantingTicketCookieGenerator.setCookieName("test");
        this.warnAction.setWarnCookieGenerator(this.warnCookieGenerator);
    }

    public void testWarnFromCookie() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie[] {new Cookie(COOKIE_PRIVACY, "true")});

        assertEquals("warn", this.warnAction.doExecute(
            TestUtils.getContext(request)).getId());
    }

    public void testWarnFromRequestParameter() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("warn", "true");

        assertEquals("redirect", this.warnAction.doExecute(
            TestUtils.getContext(request)).getId());
    }

    public void testNoWarn() throws Exception {
        assertEquals("redirect", this.warnAction.doExecute(
            TestUtils.getContext()).getId());
    }
}
