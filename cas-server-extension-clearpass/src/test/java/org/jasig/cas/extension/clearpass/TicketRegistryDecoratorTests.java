/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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
