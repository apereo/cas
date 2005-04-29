/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services.advice;

import org.jasig.cas.services.DefaultServiceRegistry;
import org.jasig.cas.services.ServiceRegistry;
import org.jasig.cas.ticket.registry.DefaultTicketRegistry;
import org.jasig.cas.ticket.registry.TicketRegistry;

import junit.framework.TestCase;


public class ServiceAllowedToProxyMethodBeforeAdviceTests extends TestCase {
    private ServiceAllowedToProxyMethodBeforeAdvice advice;
    private ServiceRegistry serviceRegistry;
    private TicketRegistry ticketRegistry;

    protected void setUp() throws Exception {
        this.ticketRegistry = new DefaultTicketRegistry();
        this.serviceRegistry = new DefaultServiceRegistry();
        this.advice = new ServiceAllowedToProxyMethodBeforeAdvice();
        this.advice.setTicketRegistry(this.ticketRegistry);
        this.advice.setServiceRegistry(this.serviceRegistry);
        this.advice.afterPropertiesSet();
    }
    
    public void testAfterPropertiesSetNoTicketRegistry() {
        try {
            this.advice.setTicketRegistry(null);
            this.advice.afterPropertiesSet();
            fail("Exception expected.");
        } catch (Exception e) {
            return;
        }
    }
    
    public void testAfterPropertiesSetNoServiceRegistry() {
        try {
            this.advice.setServiceRegistry(null);
            this.advice.afterPropertiesSet();
            fail("Exception expected.");
        } catch (Exception e) {
            return;
        }
    }
}
