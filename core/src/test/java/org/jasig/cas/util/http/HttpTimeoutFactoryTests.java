/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util.http;

import junit.framework.TestCase;


public class HttpTimeoutFactoryTests extends TestCase {

    private static final int TIMEOUT = 10;
    
    private HttpTimeoutFactory factory;

    protected void setUp() throws Exception {
        this.factory = new HttpTimeoutFactory(TIMEOUT);
    }
    
    public void testGetTimeoutHandler() {
        assertNotNull(this.factory.createURLStreamHandler("t"));
    }
    
    
    
}
