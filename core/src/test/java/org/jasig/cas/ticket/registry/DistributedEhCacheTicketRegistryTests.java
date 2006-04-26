/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.registry;

import net.sf.ehcache.Cache;

import org.jasig.cas.TestUtils;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Test case to test the DefaultTicketRegistry based on test cases to test all
 * Ticket Registries.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 */
public class DistributedEhCacheTicketRegistryTests extends AbstractTicketRegistryTests {

    private static final String APPLICATION_CONTEXT_FILE_NAME = "ehcacheContext.xml";

    private static final String APPLICATION_CONTEXT_CACHE_BEAN_NAME = "cache";
    private static final String APPLICATION_CONTEXT_CACHE_BEAN_NAME_2 = "cache2";

    private Cache cache;
    
    private Cache cache2;

    private EhCacheTicketRegistry ticketRegistry;

    public DistributedEhCacheTicketRegistryTests() throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
            APPLICATION_CONTEXT_FILE_NAME);
        this.cache = (Cache) context
            .getBean(APPLICATION_CONTEXT_CACHE_BEAN_NAME);
        
        this.cache2 = (Cache) context
        .getBean(APPLICATION_CONTEXT_CACHE_BEAN_NAME_2);
        this.ticketRegistry = new EhCacheTicketRegistry();
        this.ticketRegistry.setServiceTicketCache(this.cache);
        this.ticketRegistry.setTicketGrantingTicketCache(this.cache2);
        this.ticketRegistry.afterPropertiesSet();
    }

    public void testBadCacheGetTicket() {
        Cache badCache = new Cache("test1", 1, true, false, 5, 2);

        this.ticketRegistry.setServiceTicketCache(badCache);
        this.ticketRegistry.setTicketGrantingTicketCache(badCache);

        try {
            this.ticketRegistry.getTicket("testTicket");
            fail("Exception expected.");
        } catch (Exception e) {
            // this is okay
        }
    }

    public void testBadCacheGetTickets() {
        Cache badCache = new Cache("test2", 1, true, false, 5, 2);

        this.ticketRegistry.setServiceTicketCache(badCache);
        this.ticketRegistry.setTicketGrantingTicketCache(badCache);

        try {
            this.ticketRegistry.getTickets();
            fail("Exception expected.");
        } catch (Exception e) {
            // this is okay
        }
    }
    
    public void testGetServiceTicket() {
        final TicketGrantingTicket tgt = new TicketGrantingTicketImpl("test1", TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
        final ServiceTicket s = tgt.grantServiceTicket("test", TestUtils.getService(), new NeverExpiresExpirationPolicy(), false);

        this.ticketRegistry.addTicket(s);
        final ServiceTicket s2 = (ServiceTicket) this.ticketRegistry.getTicket(s.getId(), ServiceTicket.class);
        assertNotNull(this.ticketRegistry.getTicket(s.getId()));
        
        assertEquals(s.getCreationTime(), s2.getCreationTime());
        assertEquals(s.getGrantingTicket(), s2.getGrantingTicket());
        assertEquals(s.getId(), s2.getId());
        assertEquals(s.getService(), s2.getService());
        assertEquals(s.isExpired(), s2.isExpired());
        assertEquals(s.isFromNewLogin(), s2.isFromNewLogin());
        assertEquals(s.isValidFor(TestUtils.getService()), s2.isValidFor(TestUtils.getService()));
        assertEquals(s.toString(), s2.toString());
        assertNotNull(s2.grantTicketGrantingTicket("test", TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy()));
        
        // XXX this is cheating, knowing what order the equals method is called in
        assertEquals(s2, s);
        
        try {
            this.ticketRegistry.getTicket(s.getId(), TicketGrantingTicket.class);
            fail("Exception expected.");
        } catch (final ClassCastException e) {
            return;
        }
    }
    
    public void testGetTicketGrantingTicket() {
        final TicketGrantingTicket tgt = new TicketGrantingTicketImpl("test1", TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

        this.ticketRegistry.addTicket(tgt);
        final TicketGrantingTicket tgt2 = (TicketGrantingTicket) this.ticketRegistry.getTicket(tgt.getId(), TicketGrantingTicket.class);
        
        assertEquals(tgt.getCreationTime(), tgt2.getCreationTime());
        assertEquals(tgt.getAuthentication(), tgt2.getAuthentication());
        assertEquals(tgt.getChainedAuthentications(), tgt2.getChainedAuthentications());
        assertEquals(tgt.getGrantingTicket(), tgt2.getGrantingTicket());
        assertEquals(tgt.getId(), tgt2.getId());
        assertEquals(tgt.isExpired(), tgt2.isExpired());
        assertEquals(tgt.isRoot(), tgt2.isRoot());
        assertNotNull(tgt2.grantServiceTicket("test", TestUtils.getService(), new NeverExpiresExpirationPolicy(), false));
        tgt2.expire();
        assertTrue(tgt.isExpired());
        assertEquals(tgt.toString(), tgt2.toString());
        
        assertEquals(tgt2, tgt);
    }
    
    public TicketRegistry getNewTicketRegistry() throws Exception {
        this.cache.removeAll();
        this.cache2.removeAll();
        return this.ticketRegistry;
    }
}