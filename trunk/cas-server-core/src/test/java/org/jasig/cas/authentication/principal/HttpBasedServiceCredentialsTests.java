/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.authentication.principal;

import java.net.URL;

import org.jasig.cas.TestUtils;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class HttpBasedServiceCredentialsTests extends TestCase {

    public void testProperUrl() {
        assertEquals(TestUtils.CONST_GOOD_URL, TestUtils
            .getHttpBasedServiceCredentials().getCallbackUrl().toExternalForm());
    }
    
    public void testEqualsWithNull() throws Exception {
        final HttpBasedServiceCredentials c = new HttpBasedServiceCredentials(new URL("http://www.cnn.com"));
        
        assertFalse(c.equals(null));
    }
    
    public void testEqualsWithFalse() throws Exception {
        final HttpBasedServiceCredentials c = new HttpBasedServiceCredentials(new URL("http://www.cnn.com"));
        final HttpBasedServiceCredentials c2 = new HttpBasedServiceCredentials(new URL("http://www.msn.com"));
        
        assertFalse(c.equals(c2));
        assertFalse(c.equals(new Object()));
    }
    
    public void testEqualsWithTrue() throws Exception {
        final HttpBasedServiceCredentials c = new HttpBasedServiceCredentials(new URL("http://www.cnn.com"));
        final HttpBasedServiceCredentials c2 = new HttpBasedServiceCredentials(new URL("http://www.cnn.com"));
        
        assertTrue(c.equals(c2));
        assertTrue(c2.equals(c));
    }
}