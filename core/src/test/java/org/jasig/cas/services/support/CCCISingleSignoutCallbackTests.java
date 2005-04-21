/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services.support;

import java.net.URL;

import org.jasig.cas.services.CallbackRegisteredService;

import junit.framework.TestCase;

public class CCCISingleSignoutCallbackTests extends TestCase {

    public void testValidUrl() throws Exception {
        CallbackRegisteredService service = new CallbackRegisteredService(
            "http://www.rutgers.edu", true, true, "test",
            new CCCISingleSignoutCallback(), new URL("http://www.rutgers.edu"));
        assertTrue(service.getSingleSignoutCallback()
            .signOut(service, "ticket"));
    }

    public void testValidUrlWithQueryString() throws Exception {
        CallbackRegisteredService service = new CallbackRegisteredService(
            "http://www.rutgers.edu?test=test", true, true, "test",
            new CCCISingleSignoutCallback(), new URL("http://www.rutgers.edu"));
        assertTrue(service.getSingleSignoutCallback()
            .signOut(service, "ticket"));
    }

    public void testInValidUrl() throws Exception {
        CallbackRegisteredService service = new CallbackRegisteredService("test", true, true,
            "test", new CCCISingleSignoutCallback(), new URL(
                "http://www.rutgers.edu"));
        assertFalse(service.getSingleSignoutCallback().signOut(service,
            "ticket"));
    }
}
