/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.ticket.proxy.support;

import org.jasig.cas.TestUtils;
import org.jasig.cas.ticket.proxy.ProxyHandler;
import org.jasig.cas.ticket.proxy.support.Cas10ProxyHandler;
import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ Date$
 * @since 3.0
 */
public class Cas10ProxyHandlerTests extends TestCase {

    private ProxyHandler proxyHandler = new Cas10ProxyHandler();

    public void testNoCredentialsOrProxy() {
        assertNull(this.proxyHandler.handle(null, null));
    }

    public void testCredentialsAndProxy() {
        assertNull(this.proxyHandler.handle(TestUtils
            .getCredentialsWithSameUsernameAndPassword(), "test"));
    }
}