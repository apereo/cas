/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Id$
 */
public class DefaultUniqueTokenIdGeneratorTests extends TestCase {

    public void testUniqueGenerationOfTicketIds() {
        DefaultUniqueTokenIdGenerator generator = new DefaultUniqueTokenIdGenerator();

        assertNotSame(generator.getNewTokenId(), generator.getNewTokenId());
    }
}
