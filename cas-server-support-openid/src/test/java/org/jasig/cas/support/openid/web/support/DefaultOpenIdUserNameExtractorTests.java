/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.support.openid.web.support;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class DefaultOpenIdUserNameExtractorTests extends TestCase {

    private DefaultOpenIdUserNameExtractor extractor = new DefaultOpenIdUserNameExtractor();

    public void testExtractionSuccessful() {
        assertEquals("scootman28", this.extractor
            .extractLocalUsernameFromUri("http://test.com/scootman28"));
    }
    
    public void testExtractionFailed() {
        assertNull(this.extractor
            .extractLocalUsernameFromUri("test.com"));
    }

    public void testNull() {
        assertNull(this.extractor
            .extractLocalUsernameFromUri(null));
    }
}
