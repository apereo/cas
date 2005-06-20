/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.proxy.support;

import java.net.URL;

import org.jasig.cas.authentication.principal.HttpBasedServiceCredentials;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class Cas20ProxyHandlerTests extends TestCase {

    public void testAfterPropertiesSetBad() {
        Cas20ProxyHandler handler = new Cas20ProxyHandler();

        try {
            handler.afterPropertiesSet();

        } catch (Exception e) {
            fail("Exception not expected.");
        }
    }

    public void testAfterPropertiesSet() {
        Cas20ProxyHandler handler = new Cas20ProxyHandler();
        handler
            .setUniqueTicketIdGenerator(new DefaultUniqueTicketIdGenerator());

        try {
            handler.afterPropertiesSet();
        } catch (Exception e) {
            fail("Exception not expected.");
        }
    }

    public void testValidProxyTicketWithoutQueryString() throws Exception {
        Cas20ProxyHandler handler = new Cas20ProxyHandler();
        handler.afterPropertiesSet();
        assertNotNull(handler.handle(new HttpBasedServiceCredentials(new URL(
            "http://www.rutgers.edu/")), "proxyGrantingTicketId"));
    }

    public void testValidProxyTicketWithQueryString() throws Exception {
        Cas20ProxyHandler handler = new Cas20ProxyHandler();
        handler.afterPropertiesSet();
        assertNotNull(handler.handle(new HttpBasedServiceCredentials(new URL(
            "http://www.rutgers.edu/?test=test")), "proxyGrantingTicketId"));
    }

    public void testNonValidProxyTicket() throws Exception {
        Cas20ProxyHandler handler = new Cas20ProxyHandler();
        handler.afterPropertiesSet();
        assertNull(handler.handle(new HttpBasedServiceCredentials(new URL(
            "http://www.rutgers.edu:9090")), "proxyGrantingTicketId"));
    }
}
