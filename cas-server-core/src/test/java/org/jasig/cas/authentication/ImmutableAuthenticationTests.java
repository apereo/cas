/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.authentication;

import java.util.Date;

import org.jasig.cas.TestUtils;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class ImmutableAuthenticationTests extends AbstractAuthenticationTests {

    protected void setUp() throws Exception {
        super.setUp();
        this.authentication = new ImmutableAuthentication(TestUtils
            .getPrincipal(), this.attributes);
    }

    public void testAuthenticatedDate() {
        Date dateFromFirstCall = this.authentication.getAuthenticatedDate();
        Date dateFromSecondCall = this.authentication.getAuthenticatedDate();

        assertNotSame("Dates are the same.", dateFromFirstCall,
            dateFromSecondCall);
        assertEquals("Dates are not equal.", dateFromFirstCall,
            dateFromSecondCall);
    }
}
