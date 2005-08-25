/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util;

import org.jasig.cas.util.DefaultLongNumericGenerator;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class DefaultLongNumericGeneratorTests extends TestCase {

    public void testNoWrap() {
        try {
            new DefaultLongNumericGenerator(Long.MAX_VALUE, false)
                .getNextLong();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            return;
        }
    }

    public void testWrap() {
        assertEquals(1, new DefaultLongNumericGenerator(Long.MAX_VALUE, true)
            .getNextLong());
    }

    public void testInitialValue() {
        assertEquals(10L + 1, new DefaultLongNumericGenerator(10L, true)
            .getNextLong());
    }

    public void testIncrementWithNoWrap() {
        assertEquals(1, new DefaultLongNumericGenerator().getNextLong());
    }

    public void testMinimumSize() {
        assertEquals(1, new DefaultLongNumericGenerator().minLength());
    }

    public void testMaximumLength() {
        assertEquals(Long.toString(Long.MAX_VALUE).length(),
            new DefaultLongNumericGenerator(false).maxLength());
    }

    public void testToString() {
        assertEquals("1", new DefaultLongNumericGenerator(false)
            .getNextNumberAsString());
    }
}
