/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Id$
 */
public class HttpBasedServiceCredentialsTests extends TestCase {

    public void testNullURL() {
        try {
            new HttpBasedServiceCredentials(null);
        }
        catch (IllegalArgumentException e) {
            return;
        }

        fail("IllegalArgumentException expected.");
    }

    public void testProperUrl() {
        try {
            final URL url = new URL("http://www.rutgers.edu");

            final HttpBasedServiceCredentials c = new HttpBasedServiceCredentials(
                url);

            assertEquals(url, c.getCallbackUrl());
        }
        catch (MalformedURLException e) {
            fail("MalformedUrlException caught.");
        }
    }
}