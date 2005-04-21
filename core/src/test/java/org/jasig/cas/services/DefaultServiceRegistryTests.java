/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.net.URL;
import java.util.Collection;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class DefaultServiceRegistryTests extends TestCase {

    private final String ID = "id";

    private final boolean ALLOWTOPROXY = true;

    private final boolean FORCEAUTHENTICATION = true;

    private final String THEME = "theme";

    private final URL url = null;

    private RegisteredService authenticatedService = new RegisteredService(
        this.ID, this.ALLOWTOPROXY, this.FORCEAUTHENTICATION, this.THEME, this.url);

    private DefaultServiceRegistry serviceRegistry = new DefaultServiceRegistry();

    public void setUp() throws Exception {
        this.serviceRegistry = new DefaultServiceRegistry();
    }

    public void testAddGetServiceFromRegistry() {
        this.serviceRegistry.addService(this.authenticatedService);

        assertEquals(this.authenticatedService, this.serviceRegistry
            .getService(this.authenticatedService.getId()));
    }

    public void testDeleteFoundFromServiceRegistry() {
        this.serviceRegistry.addService(this.authenticatedService);
        assertTrue(this.serviceRegistry.deleteService(this.authenticatedService
            .getId()));
    }

    public void testDeleteNotFoundFromServiceRegistry() {
        assertFalse(this.serviceRegistry
            .deleteService(this.authenticatedService.getId()));
    }

    public void testRetrieveNotFoundInServiceRegistry() {
        assertNull(this.serviceRegistry.getService(this.authenticatedService
            .getId()));
    }

    public void testClearServiceRegistry() {
        this.serviceRegistry.addService(this.authenticatedService);
        this.serviceRegistry.clear();
        assertNull(this.serviceRegistry.getService(this.authenticatedService
            .getId()));
    }

    public void testAddGetServiceExistsFromRegistry() {
        this.serviceRegistry.addService(this.authenticatedService);

        assertTrue(this.serviceRegistry.serviceExists(this.authenticatedService
            .getId()));
    }

    public void testAddGetServiceNotExistsFromRegistry() {
        assertFalse(this.serviceRegistry
            .serviceExists(this.authenticatedService.getId()));
    }

    public void testGetCollection() {
        final RegisteredService authenticatedService2 = new RegisteredService(
            "test2", this.ALLOWTOPROXY, this.FORCEAUTHENTICATION, this.THEME, this.url);

        this.serviceRegistry.addService(authenticatedService2);
        this.serviceRegistry.addService(this.authenticatedService);

        final Collection c = this.serviceRegistry.getServices();

        assertTrue(c.contains(authenticatedService2));
        assertTrue(c.contains(this.authenticatedService));
    }
}
