/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services.advice;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.services.DefaultServiceRegistry;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServiceRegistry;
import org.jasig.cas.services.ServiceRegistryManager;
import org.jasig.cas.services.UnauthorizedServiceException;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class ServiceAllowedMethodBeforeAdviceTests extends TestCase {

    private ServiceAllowedMethodBeforeAdvice advice;

    private ServiceRegistry serviceRegistry;

    private ServiceRegistryManager serviceRegistryManager;

    protected void setUp() throws Exception {
        this.advice = new ServiceAllowedMethodBeforeAdvice();

        this.serviceRegistry = new DefaultServiceRegistry();
        this.serviceRegistryManager = (ServiceRegistryManager) this.serviceRegistry;

        this.advice.setServiceRegistry(this.serviceRegistry);

        RegisteredService service = new RegisteredService("Test", false, false,
            null, null, null);
        this.serviceRegistryManager.addService(service);
        this.advice.afterPropertiesSet();
    }

    public void testNoServiceFound() {
        final String ticketId = "Test";
        final Service service = new SimpleService("Test2");

        try {
            this.advice.before(null, new Object[] {ticketId, service}, null);
            fail("Exception expected.");
        } catch (UnauthorizedServiceException e) {
            return;
        }
    }

    public void testServiceFound() throws Exception {
        final String ticketId = "Test";
        final Service service = new SimpleService("Test");

        this.advice.before(null, new Object[] {ticketId, service}, null);
    }

    public void testGetServiceRegistry() {
        assertEquals(this.serviceRegistry, this.advice.getServiceRegistry());
    }

    public void testAfterPropertiesSet() {
        this.advice.setServiceRegistry(null);
        try {
            this.advice.afterPropertiesSet();
            fail("Exception expected.");
        } catch (Exception e) {
            return;
        }
    }

}
