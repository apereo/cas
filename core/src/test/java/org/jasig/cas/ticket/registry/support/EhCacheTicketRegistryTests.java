/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.registry.support;

import net.sf.ehcache.Cache;

import org.jasig.cas.ticket.registry.AbstractTicketRegistryTests;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.ticket.registry.support.EhCacheTicketRegistry;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Test case to test the DefaultTicketRegistry based on test cases to test all Ticket Registries.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class EhCacheTicketRegistryTests extends AbstractTicketRegistryTests {

    private static final String APPLICATION_CONTEXT_FILE_NAME = "ehcacheContext.xml";

    private static final String APPLICATION_CONTEXT_CACHE_BEAN_NAME = "cache";

    private Cache cache;

    private EhCacheTicketRegistry ticketRegistry;

    public EhCacheTicketRegistryTests() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
            APPLICATION_CONTEXT_FILE_NAME);
        this.cache = (Cache)context
            .getBean(APPLICATION_CONTEXT_CACHE_BEAN_NAME);
        this.ticketRegistry = new EhCacheTicketRegistry();
        this.ticketRegistry.setCache(this.cache);
    }

    public void testBadCacheGetTicket() {
        Cache badCache = new Cache("test1", 1, true, false, 5, 2);

        this.ticketRegistry.setCache(badCache);

        try {
            this.ticketRegistry.getTicket("testTicket");
            fail("Exception expected.");
        }
        catch (Exception e) {
            // this is okay
        }
    }

    public void testBadCacheGetTickets() {
        Cache badCache = new Cache("test2", 1, true, false, 5, 2);

        this.ticketRegistry.setCache(badCache);

        try {
            this.ticketRegistry.getTickets();
            fail("Exception expected.");
        }
        catch (Exception e) {
            // this is okay
        }
    }

    /**
     * @see org.jasig.cas.ticket.registry.AbstractTicketRegistryTests#getNewTicketRegistry()
     */
    public TicketRegistry getNewTicketRegistry() throws Exception {
        this.cache.removeAll();
        return this.ticketRegistry;
    }
}