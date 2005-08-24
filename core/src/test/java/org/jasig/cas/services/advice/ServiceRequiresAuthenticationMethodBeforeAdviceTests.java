/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services.advice;

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.services.DefaultServiceRegistry;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServiceRegistry;
import org.jasig.cas.services.ServiceRegistryManager;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class ServiceRequiresAuthenticationMethodBeforeAdviceTests extends
    TestCase {

    private ServiceRequiresAuthenticationMethodBeforeAdvice advice;

    private ServiceRegistry serviceRegistry;

    private ServiceRegistryManager serviceRegistryManager;

    protected void setUp() throws Exception {
        this.advice = new ServiceRequiresAuthenticationMethodBeforeAdvice();

        this.serviceRegistry = new DefaultServiceRegistry();
        this.serviceRegistryManager = (ServiceRegistryManager) this.serviceRegistry;

        this.advice.setServiceRegistry(this.serviceRegistry);

        RegisteredService service = new RegisteredService("Test", false, false,
            null, null);
        this.serviceRegistryManager.addService(service);

        service = new RegisteredService("TestAuth", false, true, null, null);
        this.serviceRegistryManager.addService(service);
        this.advice.afterPropertiesSet();
    }

    public void testNonThreeArguments() throws Exception {
        this.advice.before(null, new Object[] {"ticketId",
            new SimpleService("Test")}, null);
    }

    public void testRequiresAuthNoAuth() throws Exception {
        try {
            this.advice.before(null, new Object[] {"ticketId",
                new SimpleService("TestAuth"), null}, null);
            fail("Exception expected.");
        } catch (IllegalStateException e) {
            return;
        }
    }

    public void testRequiresAuthHasAuth() throws Exception {
        this.advice.before(null, new Object[] {"ticketId",
            new SimpleService("TestAuth"),
            TestUtils.getCredentialsWithSameUsernameAndPassword()}, null);
    }
}
