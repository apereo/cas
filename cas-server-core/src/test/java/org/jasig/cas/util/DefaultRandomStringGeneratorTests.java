/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.util;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class DefaultRandomStringGeneratorTests extends TestCase {

    private static final int LENGTH = 35;

    private RandomStringGenerator randomStringGenerator = new DefaultRandomStringGenerator(
        LENGTH);

    public void testMaxLength() {
        assertEquals(LENGTH, this.randomStringGenerator.getMaxLength());
    }

    public void testMinLength() {
        assertEquals(LENGTH, this.randomStringGenerator.getMinLength());
    }

    public void testRandomString() {
        assertNotSame(this.randomStringGenerator.getNewString(),
            this.randomStringGenerator.getNewString());
    }
}
