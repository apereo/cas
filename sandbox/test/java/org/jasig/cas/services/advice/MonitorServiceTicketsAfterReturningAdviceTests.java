/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services.advice;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.mock.MockAuthentication;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.registry.DefaultTicketRegistry;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class MonitorServiceTicketsAfterReturningAdviceTests extends TestCase {

    private Map singleSignoutMap;
    
    private MonitorServiceTicketsAfterReturningAdvice advice;
    
    private TicketRegistry ticketRegistry;

    protected void setUp() throws Exception {
        this.ticketRegistry = new DefaultTicketRegistry();
        this.singleSignoutMap = new HashMap();
        
        this.advice = new MonitorServiceTicketsAfterReturningAdvice();
        this.advice.setSingleSignoutMapping(this.singleSignoutMap);
        this.advice.setTicketRegistry(this.ticketRegistry);
        
        this.advice.afterPropertiesSet();
    }
    
    public void testAfterPropertiesSetNoMap() {
        this.advice.setSingleSignoutMapping(null);
        
        try {
            this.advice.afterPropertiesSet();
            fail("Exception expected.");
        } catch (Exception e) {
            return;
        }
    }
    
    public void testAfterPropertiesSetNoRegistry() {
        this.advice.setTicketRegistry(null);
        
        try {
            this.advice.afterPropertiesSet();
            fail("Exception expected.");
        } catch (Exception e) {
            return;
        }
    }
    
    public void testTicketIsNotServiceTicket() throws Throwable {
        this.advice.afterReturning(null, null, new Object[] {new TicketGrantingTicketImpl("Test", new MockAuthentication(), new NeverExpiresExpirationPolicy())}, null);
        assertTrue(this.singleSignoutMap.isEmpty());
    }
    
    public void testTestNoTicketGrantingTicketInMap() throws Throwable {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", new MockAuthentication(), new NeverExpiresExpirationPolicy());
        final ServiceTicket s = t.grantServiceTicket("test2", new SimpleService("test"), new NeverExpiresExpirationPolicy());
        
        this.advice.afterReturning(null, null, new Object[] {s}, null);
        
        assertTrue(this.singleSignoutMap.containsKey(t.getId()));
        Set set = (Set) this.singleSignoutMap.get(t.getId());
        assertTrue(set.contains(s));
    }
    
    public void testTestTicketGrantingTicketInMap() throws Throwable {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", new MockAuthentication(), new NeverExpiresExpirationPolicy());
        final ServiceTicket s = t.grantServiceTicket("test2", new SimpleService("test"), new NeverExpiresExpirationPolicy());
        
        this.singleSignoutMap.put(t.getId(), new HashSet());
        this.advice.afterReturning(null, null, new Object[] {s}, null);
        
        assertTrue(this.singleSignoutMap.containsKey(t.getId()));
        Set set = (Set) this.singleSignoutMap.get(t.getId());
        assertTrue(set.contains(s));
    }
}
