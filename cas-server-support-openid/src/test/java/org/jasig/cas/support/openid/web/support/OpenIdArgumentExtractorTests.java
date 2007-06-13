/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.support.openid.web.support;

import org.jasig.cas.support.openid.web.support.OpenIdArgumentExtractor;
import org.springframework.mock.web.MockHttpServletRequest;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 */
public class OpenIdArgumentExtractorTests extends TestCase {

    private OpenIdArgumentExtractor extractor;

    public void setUp() throws Exception {
        this.extractor = new OpenIdArgumentExtractor();
    }

    public void testNoOpenIdServiceExists() {
        assertNull(this.extractor.extractService(new MockHttpServletRequest()));
    }
}
