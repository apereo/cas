/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.jasig.org/cas/license.
 */
package org.jasig.cas.ticket.registry;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collection;

import org.jasig.cas.ticket.ServiceTicket;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private MemCacheTicketRegistry registry;

    private final String registryBean;

    private final boolean binaryProtocol;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public MemCacheTicketRegistryTests(final String beanName, final boolean binary) {
        registryBean = beanName;
        binaryProtocol = binary;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getTestParameters() throws Exception {
        return Arrays.asList(
          new Object[] {"testCase1", false},
          new Object[] {"testCase2", true}
        );
    }

    @Before
    public void setUp() {
        // Memcached is a required external test fixture.
        // Abort tests if there is no memcached server available on localhost:11211.
        final boolean environmentOk = isMemcachedListening();
        if (!environmentOk) {
            logger.warn("Aborting test since no memcached server is available on localhost.");
        }
        Assume.assumeTrue(environmentOk);

        context = new ClassPathXmlApplicationContext("/ticketRegistry-test.xml");
        registry = context.getBean(registryBean, MemCacheTicketRegistry.class);
    }

    @Test
    public void testWriteGetDelete() throws Exception {
        final String id = "ST-1234567890ABCDEFGHIJKL-crud";
        final ServiceTicket ticket = mock(ServiceTicket.class, withSettings().serializable());
        when(ticket.getId()).thenReturn(id);
        registry.addTicket(ticket);
        final ServiceTicket ticketFromRegistry = (ServiceTicket) registry.getTicket(id);
        Assert.assertNotNull(ticketFromRegistry);
        Assert.assertEquals(id, ticketFromRegistry.getId());
        registry.deleteTicket(id);
        Assert.assertNull(registry.getTicket(id));
    }

    @Test
    public void testExpiration() throws Exception {
        final String id = "ST-1234567890ABCDEFGHIJKL-exp";
        final ServiceTicket ticket = mock(ServiceTicket.class, withSettings().serializable());
        when(ticket.getId()).thenReturn(id);
        registry.addTicket(ticket);
        Assert.assertNotNull((ServiceTicket) registry.getTicket(id));
        // Sleep a little longer than service ticket expiry defined in Spring context
        Thread.sleep(2100);
        Assert.assertNull((ServiceTicket) registry.getTicket(id));
    }

    private boolean isMemcachedListening() {
        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", 11211);
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // Ignore errors on close
                }
            }
        }
    }
}
