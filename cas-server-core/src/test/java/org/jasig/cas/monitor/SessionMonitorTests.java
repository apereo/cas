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
package org.jasig.cas.monitor;

import org.jasig.cas.mock.MockService;
import org.jasig.cas.mock.MockTicketGrantingTicket;
import org.jasig.cas.ticket.registry.DefaultTicketRegistry;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for {@link SessionMonitor} class.
 *
 * @author Marvin S. Addison
 * @version $Revision: $
 */
public class SessionMonitorTests {

    private DefaultTicketRegistry registry;
    private SessionMonitor monitor;

    @Before
    public void setUp() {
        registry = new DefaultTicketRegistry();
        monitor = new SessionMonitor();
        monitor.setTicketRegistry(registry);
    }

    @Test
    public void testObserveOk() throws Exception {
        addTicketsToRegistry(5, 10);
        final SessionStatus status = monitor.observe();
        assertEquals(5, status.getSessionCount());
        assertEquals(10, status.getServiceTicketCount());
        assertEquals(StatusCode.OK, status.getCode());
    }

    @Test
    public void testObserveWarnSessionsExceeded() throws Exception {
        addTicketsToRegistry(10, 1);
        monitor.setSessionCountWarnThreshold(5);
        final SessionStatus status = monitor.observe();
        assertEquals(StatusCode.WARN, status.getCode());
        assertTrue(status.getDescription().contains("Session count"));
    }

    @Test
    public void testObserveWarnServiceTicketsExceeded() throws Exception {
        addTicketsToRegistry(1, 10);
        monitor.setServiceTicketCountWarnThreshold(5);
        final SessionStatus status = monitor.observe();
        assertEquals(StatusCode.WARN, status.getCode());
        assertTrue(status.getDescription().contains("Service ticket count"));
    }

    private void addTicketsToRegistry(final int tgtCount, final int stCount) {
        MockTicketGrantingTicket ticket = null;
        for (int i = 0; i < tgtCount; i++) {
            ticket = new MockTicketGrantingTicket("junit");
            registry.addTicket(ticket);
        }
        String id;
        for (int i = 0; i < stCount; i++) {
            registry.addTicket(ticket.grantServiceTicket(new MockService("junit")));
        }
    }
}
