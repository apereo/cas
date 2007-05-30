/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.ticket.proxy.support;

import java.net.URL;

import org.jasig.cas.authentication.principal.HttpBasedServiceCredentials;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.jasig.cas.util.HttpClient;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class Cas20ProxyHandlerTests extends TestCase {

    private Cas20ProxyHandler handler;

    protected void setUp() throws Exception {
        this.handler = new Cas20ProxyHandler();
        this.handler.setHttpClient(new HttpClient());
        this.handler.setUniqueTicketIdGenerator(new DefaultUniqueTicketIdGenerator());
    }

    public void testValidProxyTicketWithoutQueryString() throws Exception {
        assertNotNull(this.handler.handle(new HttpBasedServiceCredentials(
            new URL("http://www.rutgers.edu/")), "proxyGrantingTicketId"));
    }

    public void testValidProxyTicketWithQueryString() throws Exception {
        assertNotNull(this.handler.handle(new HttpBasedServiceCredentials(
            new URL("http://www.rutgers.edu/?test=test")),
            "proxyGrantingTicketId"));
    }

    public void testNonValidProxyTicket() throws Exception {
        final HttpClient httpClient = new HttpClient();
        httpClient.setAcceptableCodes(new int[] {900});
        this.handler.setHttpClient(httpClient);
        assertNull(this.handler.handle(new HttpBasedServiceCredentials(new URL(
            "http://www.rutgers.edu")), "proxyGrantingTicketId"));
    }
}
