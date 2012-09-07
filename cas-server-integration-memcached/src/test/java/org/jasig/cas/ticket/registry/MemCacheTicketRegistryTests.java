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

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collection;

import org.jasig.cas.ticket.registry.support.MemcachedBaseTest;
import org.jasig.cas.ticket.ServiceTicket;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

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
public class MemCacheTicketRegistryTests extends MemcachedBaseTest {

    private MemCacheTicketRegistry registry;

    private final String registryBean;

    private final boolean binaryProtocol;

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
    public void setUp() throws Exception {
        super.setUp();
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
}
