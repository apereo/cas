/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.jasig.org/cas/license.
 */
package org.jasig.cas.ticket.registry;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collection;

import com.thimbleware.jmemcached.CacheImpl;
import com.thimbleware.jmemcached.Key;
import com.thimbleware.jmemcached.LocalCacheElement;
import com.thimbleware.jmemcached.MemCacheDaemon;
import com.thimbleware.jmemcached.storage.CacheStorage;
import com.thimbleware.jmemcached.storage.hash.ConcurrentLinkedHashMap;
import junit.framework.Assert;
import org.jasig.cas.ticket.ServiceTicket;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Unit test for MemCacheTicketRegistry class.
 *
 * @author Middleware Services
 * @version $Revision: $
 */
@RunWith(Parameterized.class)
public class MemCacheTicketRegistryTests {

    private ApplicationContext context;

    private MemCacheDaemon<LocalCacheElement> daemon;

    private MemCacheTicketRegistry registry;

    private final String registryBean;

    private final boolean binaryProtocol;

    public MemCacheTicketRegistryTests(final String beanName, final boolean binary) {
        registryBean = beanName;
        binaryProtocol = binary;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getTestParameters() throws Exception {


        return Arrays.asList(new Object[][] {
            new Object[] {"testCase1", false},
            new Object[] {"testCase2", true},
        });
    }

    @Before
    public void setUp() {
        daemon = new MemCacheDaemon<LocalCacheElement>();
        CacheStorage<Key, LocalCacheElement> storage = ConcurrentLinkedHashMap.create(
                ConcurrentLinkedHashMap.EvictionPolicy.FIFO, 100, 1024);
        daemon.setCache(new CacheImpl(storage));
        daemon.setBinary(binaryProtocol);
        daemon.setAddr(new InetSocketAddress("127.0.0.1", 11211));
        daemon.setIdleTime(30);
        daemon.setVerbose(true);
        daemon.start();

        // Have to start memcached listener before starting Spring context since memcached beans need it
        context = new ClassPathXmlApplicationContext("/ticketRegistry-test.xml");
        registry = context.getBean(registryBean, MemCacheTicketRegistry.class);
    }

    @Test
    public void testWriteGetDelete() throws Exception {
        Assert.assertNotNull(registry);
        final String id = "ST-1234567890ABCDEFGHIJKL-node";
        ServiceTicket ticket = mock(ServiceTicket.class, withSettings().serializable());
        when(ticket.getId()).thenReturn(id);
        registry.addTicket(ticket);
        final ServiceTicket ticketFromRegistry = (ServiceTicket) registry.getTicket(id);
        Assert.assertNotNull(ticketFromRegistry);
        Assert.assertEquals(id, ticketFromRegistry.getId());
        registry.deleteTicket(id);
        Assert.assertNull(registry.getTicket(id));
    }

    @After
    public void tearDown() {
        daemon.stop();
    }

    private void startMemcachedServer(final boolean binary) {

    }
}
