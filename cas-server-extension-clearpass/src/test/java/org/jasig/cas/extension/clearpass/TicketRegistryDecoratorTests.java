package org.jasig.cas.extension.clearpass;

import java.util.HashMap;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.jasig.cas.ticket.registry.DefaultTicketRegistry;
import org.jasig.cas.ticket.registry.EhCacheTicketRegistry;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Misagh Moayyed
 * @since 3.5.0
 */
public class TicketRegistryDecoratorTests {

    @Test
    public void verifyDefaultTicketRegistryWithClearPass() {

        final TicketRegistry ticketRegistry = new DefaultTicketRegistry();
        final Map<String, String> map = new HashMap<>();
        final TicketRegistryDecorator decorator = new TicketRegistryDecorator(ticketRegistry, map);
        assertNotNull(decorator);
        assertEquals(decorator.serviceTicketCount(), 0);
        assertEquals(decorator.sessionCount(), 0);
    }

    @Test
    public void verifyEhCacheTicketRegistryWithClearPass() {
        final Cache serviceTicketsCache = new Cache("serviceTicketsCache", 200, false, false, 100, 100);
        final Cache ticketGrantingTicketCache = new Cache("ticketGrantingTicketCache", 200, false, false, 100, 100);

        final CacheManager manager = new CacheManager(this.getClass().getClassLoader().getResourceAsStream("ehcacheClearPass.xml"));
        manager.addCache(serviceTicketsCache);
        manager.addCache(ticketGrantingTicketCache);

        final Map<String, String> map = new HashMap<>();

        final TicketRegistry ticketRegistry = new EhCacheTicketRegistry(serviceTicketsCache, ticketGrantingTicketCache);
        final TicketRegistryDecorator decorator = new TicketRegistryDecorator(ticketRegistry, map);
        assertNotNull(decorator);

        assertEquals(decorator.serviceTicketCount(), 0);
        assertEquals(decorator.sessionCount(), 0);

        manager.removalAll();
        manager.shutdown();

    }
}
