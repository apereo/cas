/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.proxy.support;

import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.ticket.proxy.ProxyHandler;
import org.jasig.cas.ticket.proxy.support.Cas10ProxyHandler;
import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Id: Cas10ProxyHandlerTests.java,v 1.2 2005/02/27 05:49:26
 * sbattaglia Exp $
 */
public class Cas10ProxyHandlerTests extends TestCase {

    private ProxyHandler proxyHandler = new Cas10ProxyHandler();

    public void testNoCredentialsOrProxy() {
        assertNull(this.proxyHandler.handle(null, null));
    }

    public void testCredentialsAndProxy() {
        assertNull(this.proxyHandler.handle(new UsernamePasswordCredentials(),
            "test"));
    }
}