/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.net.URL;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class AuthenticatedServiceTests extends TestCase {

    public void testGetters() {
        final String ID = "id";
        final boolean ALLOWTOPROXY = true;
        final boolean FORCEAUTHENTICATION = true;
        final String THEME = "theme";
        final URL url = null;

        RegisteredService authenticatedService = new RegisteredService(ID,
            ALLOWTOPROXY, FORCEAUTHENTICATION, THEME, url);

        assertEquals(ID, authenticatedService.getId());
        assertEquals(ALLOWTOPROXY, authenticatedService.isAllowedToProxy());
        assertEquals(FORCEAUTHENTICATION, authenticatedService
            .isForceAuthentication());
        assertEquals(THEME, authenticatedService.getTheme());
        assertEquals(url, authenticatedService.getProxyUrl());
    }

    public void testNoId() {
        try {
            new RegisteredService(null, false, false, null, null);
            fail("IllegalArgumentsException expected.");
        } catch (Exception e) {
            return;
        }
    }
}
