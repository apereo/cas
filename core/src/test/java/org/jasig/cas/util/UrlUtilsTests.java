/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util;

import java.net.HttpURLConnection;

import junit.framework.TestCase;

public final class UrlUtilsTests extends TestCase {

    public void testMalformedUrl() {
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, UrlUtils
            .getResponseCodeFromString("fa"));
    }
    
    public void testHandshakeError() {
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, UrlUtils
            .getResponseCodeFromString("https://jira.acs.rutgers.edu/"));
    }

}
