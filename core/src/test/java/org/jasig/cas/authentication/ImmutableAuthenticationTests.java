/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jasig.cas.TestUtils;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class ImmutableAuthenticationTests extends TestCase {

    private Authentication authentication;

    private Map attributes = new HashMap();

    protected void setUp() throws Exception {
        super.setUp();
        this.authentication = new ImmutableAuthentication(TestUtils
            .getPrincipal(), this.attributes);
    }

    public void testGetters() {
        assertEquals("Principals are not equal", TestUtils.getPrincipal(),
            this.authentication.getPrincipal());
        assertEquals("Authentication Attributes not equal.",
            this.authentication.getAttributes(), this.attributes);
    }

    public void testAuthenticatedDate() {
        Date dateFromFirstCall = this.authentication.getAuthenticatedDate();
        Date dateFromSecondCall = this.authentication.getAuthenticatedDate();

        assertNotSame("Dates are the same.", dateFromFirstCall,
            dateFromSecondCall);
        assertEquals("Dates are not equal.", dateFromFirstCall,
            dateFromSecondCall);
    }

    public void testNullHashMap() {
        assertNotNull("Attributes are null.", TestUtils.getAuthentication()
            .getAttributes());
    }

    public void testHashCode() {
        assertEquals("Hashcodes do not match.", HashCodeBuilder
            .reflectionHashCode(this.authentication), this.authentication
            .hashCode());
    }

    public void testToString() {
        assertEquals("toString values do not match.", ToStringBuilder
            .reflectionToString(this.authentication), this.authentication
            .toString());
    }

    public void testEquals() {
        assertTrue("Authentications are not equal", this.authentication
            .equals(this.authentication));
    }
}
