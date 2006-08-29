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
public class DefaultUniqueTicketIdGeneratorTests extends TestCase {

    public void testUniqueGenerationOfTicketIds() {
        DefaultUniqueTicketIdGenerator generator = new DefaultUniqueTicketIdGenerator(
            10);

        assertNotSame(generator.getNewTicketId("TEST"), generator
            .getNewTicketId("TEST"));
    }
    
    public void testSuffix() {
        final String SUFFIX = "suffix";
        DefaultUniqueTicketIdGenerator generator = new DefaultUniqueTicketIdGenerator(SUFFIX);
        
        assertTrue(generator.getNewTicketId("test").endsWith(SUFFIX));
    }
}
