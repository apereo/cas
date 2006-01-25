/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import org.springframework.webflow.test.MockRequestContext;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 */
public class RenewRequestCheckActionTests extends TestCase {

    private RenewRequestCheckAction action = new RenewRequestCheckAction();

    public void testRenewIsTrue() {
        assertEquals("authenticationRequired", this.action.doExecuteInternal(
            new MockRequestContext(), "test", "test", false, true, true).getId());
    }

    public void testRenewIsFalse() {
        assertEquals("generateServiceTicket", this.action.doExecuteInternal(
            new MockRequestContext(), "test", "test", false, false, true).getId());
    }

}
