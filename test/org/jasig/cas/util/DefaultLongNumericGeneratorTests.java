/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.util;

import org.jasig.cas.util.DefaultLongNumericGenerator;
import org.jasig.cas.util.LongNumericGenerator;

import junit.framework.TestCase;


/**
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class DefaultLongNumericGeneratorTests extends TestCase {
    
    public void testNoWrap() {
        LongNumericGenerator generator = new DefaultLongNumericGenerator(Long.MAX_VALUE, false);
        
        try {
            generator.getNextLong();
        } catch (IllegalStateException e) {
            return;
        }
        
        fail("Expected IllegalStateException");
    }
    
    public void testWrap() {
        LongNumericGenerator generator = new DefaultLongNumericGenerator(Long.MAX_VALUE, true);
        
        try {
            long response = generator.getNextLong();
            assertEquals(response, Long.MIN_VALUE);
        } catch (IllegalStateException e) {
            fail("Unexpected IllegalStateException");
        }
    }
    
    public void testInitialValue() {
        LongNumericGenerator generator = new DefaultLongNumericGenerator(10L, true);
        
        long response = generator.getNextLong();
        
        assertEquals(10L+1, response);
    }
}
