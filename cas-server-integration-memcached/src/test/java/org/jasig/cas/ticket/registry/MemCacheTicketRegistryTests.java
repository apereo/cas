/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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
package org.jasig.cas.ticket.registry;

import java.net.Socket;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
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
        } catch (final Exception e) {
            return false;
        } finally {
            IOUtils.closeQuietly(socket);
        }
    }
}
