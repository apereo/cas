/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class HttpClientTests extends TestCase {

    private HttpClient httpClient;

    protected void setUp() throws Exception {
        this.httpClient = new HttpClient();
        this.httpClient.setConnectionTimeout(1000);
        this.httpClient.setReadTimeout(1000);
    }
    
    public void testOkayUrl() {
        assertTrue(this.httpClient.isValidEndPoint("http://www.jasig.org"));
    }
    
    public void testBadUrl() {
        assertFalse(this.httpClient.isValidEndPoint("http://www.jasig.org/scottb.html"));
    }
}
