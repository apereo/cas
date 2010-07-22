/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.authentication;

import java.util.HashMap;
import java.util.Map;

import org.jasig.cas.TestUtils;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public abstract class AbstractAuthenticationTests extends TestCase {

    protected Authentication authentication;

    protected Map<String, Object> attributes = new HashMap<String, Object>();

    public final void testGetters() {
        assertEquals("Principals are not equal", TestUtils.getPrincipal(),
            this.authentication.getPrincipal());
        assertEquals("Authentication Attributes not equal.",
            this.authentication.getAttributes(), this.attributes);
    }

    public final void testNullHashMap() {
        assertNotNull("Attributes are null.", TestUtils.getAuthentication()
            .getAttributes());
    }

    public final void testEquals() {
        assertTrue("Authentications are not equal", this.authentication
            .equals(this.authentication));
    }
}
