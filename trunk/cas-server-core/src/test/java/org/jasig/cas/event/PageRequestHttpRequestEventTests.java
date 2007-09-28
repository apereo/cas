/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.event;

import org.springframework.mock.web.MockHttpServletRequest;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class PageRequestHttpRequestEventTests extends TestCase {

    private MockHttpServletRequest request = new MockHttpServletRequest();

    private HttpRequestEvent event = new HttpRequestEvent(this.request);

    public void testGetRequest() {
        assertEquals(this.request, this.event.getRequest());
    }

    public void testGetReferrer() {
        final String REFERER = "I AM A REFERER";

        this.request.addHeader("Referer", REFERER);

        assertEquals(REFERER, this.event.getReferrer());
    }

    public void testGetPage() {
        final String MY_PATH = "/test/I am a test page.html";
        final String PAGE = "I am a test page.html";

        this.request.setContextPath("/test");
        this.request.setRequestURI(MY_PATH);

        assertEquals(PAGE, this.event.getPage());
    }

    public void testGetIpAddress() {
        final String REMOTE_ADDR = "127.0.0.1";
        this.request.setRemoteAddr(REMOTE_ADDR);
        assertEquals(REMOTE_ADDR, this.event.getIpAddress());
    }

    public void testGetMethod() {
        this.request.setMethod("GET");

        assertEquals("GET", this.event.getMethod());
    }

    public void testGetUserAgent() {
        final String USER_AGENT = "UserAgent";

        this.request.addHeader("User-Agent", USER_AGENT);

        assertEquals(USER_AGENT, this.event.getUserAgent());
    }
}
