/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

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
}