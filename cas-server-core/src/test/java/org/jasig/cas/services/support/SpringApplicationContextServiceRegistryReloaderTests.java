/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.services.support;

import org.jasig.cas.services.DefaultServiceRegistry;
import org.jasig.cas.services.ServiceRegistry;
import org.jasig.cas.services.ServiceRegistryManager;
import org.springframework.core.io.DefaultResourceLoader;

import junit.framework.TestCase;

public class SpringApplicationContextServiceRegistryReloaderTests extends
    TestCase {

    private SpringApplicationContextServiceRegistryReloader reloader;

    private ServiceRegistry serviceRegistry;

    protected void setUp() throws Exception {
        this.serviceRegistry = new DefaultServiceRegistry();
        this.reloader = new SpringApplicationContextServiceRegistryReloader();
        this.reloader.setFileName("services.xml");
        this.reloader
            .setServiceRegistryManager((ServiceRegistryManager) this.serviceRegistry);
        this.reloader.setResourceLoader(new DefaultResourceLoader());
        this.reloader.afterPropertiesSet();
    }

    public void testAfterPropertiesSet() throws Exception {
        this.reloader.afterPropertiesSet();
    }

    public void testAfterPropertiesSetFilesDoesntExist() {
        try {
            this.reloader.setFileName("test");
            this.reloader.afterPropertiesSet();
            fail("Exception expected.");
        } catch (Exception e) {
            return;
        }
    }

    public void testAfterPropertiesSetDefault() throws Exception {
        this.reloader.setFileName(null);
        this.reloader.afterPropertiesSet();
    }

    public void testReloadServiceRegistry() throws Exception {
        this.reloader.reloadServiceRegistry();
        assertEquals(1, this.serviceRegistry.getServices().size());

        // check the if statement if it gets set to false.
        this.reloader.reloadServiceRegistry();
        assertEquals(1, this.serviceRegistry.getServices().size());
    }
}
