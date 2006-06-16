/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.util;

import junit.framework.TestCase;


public class WebUtilTests extends TestCase {

    public void testStripJsession() {
        assertEquals("test", WebUtils.stripJsessionFromUrl("test"));
        assertEquals("http://www.cnn.com", WebUtils.stripJsessionFromUrl("http://www.cnn.com;jsession=fsfsadfsdfsafsd"));
    }
}
