/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import org.springframework.webflow.test.MockRequestContext;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 *
 */
public class HasServiceCheckActionTests extends TestCase {

    private HasServiceCheckAction action = new HasServiceCheckAction();

    public void testHasService() {
        assertEquals("hasService", this.action.doExecuteInternal(
            new MockRequestContext(), "test", "service", true, true, true)
            .getId());
    }

    public void testHasNoService() {
        assertEquals("authenticatedButNoService", this.action
            .doExecuteInternal(new MockRequestContext(), "test", "", true,
                true, true).getId());
    }
}
