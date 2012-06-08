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

import org.jasig.cas.authentication.ImmutableAuthentication;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.mock.MockService;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.registry.DefaultTicketRegistry;
import org.jasig.cas.ticket.registry.JpaTicketRegistry;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.ticket.support.HardTimeoutExpirationPolicy;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.jasig.cas.util.UniqueTicketIdGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link SessionMonitor} class.
 *
 * @author Marvin S. Addison
 * @version $Revision: $
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/jpaTestApplicationContext.xml"})
@Transactional
public class SessionMonitorTests {

    private static final ExpirationPolicy TEST_EXP_POLICY = new HardTimeoutExpirationPolicy(10000);
    private static final UniqueTicketIdGenerator GENERATOR = new DefaultUniqueTicketIdGenerator();

    @Autowired
    private JpaTicketRegistry jpaRegistry;

    private DefaultTicketRegistry defaultRegistry;
    private SessionMonitor monitor;

    @Before
    public void setUp() {
        defaultRegistry = new DefaultTicketRegistry();
        monitor = new SessionMonitor();
        monitor.setTicketRegistry(defaultRegistry);
    }

    @Test
    public void testObserveOk() throws Exception {
        addTicketsToRegistry(defaultRegistry, 5, 10);
        final SessionStatus status = monitor.observe();
        assertEquals(5, status.getSessionCount());
        assertEquals(10, status.getServiceTicketCount());
        assertEquals(StatusCode.OK, status.getCode());
    }

    @Test
    public void testObserveWarnSessionsExceeded() throws Exception {
        addTicketsToRegistry(defaultRegistry, 10, 1);
        monitor.setSessionCountWarnThreshold(5);
        final SessionStatus status = monitor.observe();
        assertEquals(StatusCode.WARN, status.getCode());
        assertTrue(status.getDescription().contains("Session count"));
    }

    @Test
    public void testObserveWarnServiceTicketsExceeded() throws Exception {
        addTicketsToRegistry(defaultRegistry, 1, 10);
        monitor.setServiceTicketCountWarnThreshold(5);
        final SessionStatus status = monitor.observe();
        assertEquals(StatusCode.WARN, status.getCode());
        assertTrue(status.getDescription().contains("Service ticket count"));
    }

    @Test
    @Rollback(false)
    public void testObserveOkJpaTicketRegistry() throws Exception {
        addTicketsToRegistry(jpaRegistry, 5, 5);
        assertEquals(10, jpaRegistry.getTickets().size());
        monitor.setTicketRegistry(jpaRegistry);
        final SessionStatus status = monitor.observe();
        assertEquals(5, status.getSessionCount());
        assertEquals(5, status.getServiceTicketCount());
        assertEquals(StatusCode.OK, status.getCode());
    }

    private void addTicketsToRegistry(final TicketRegistry registry, final int tgtCount, final int stCount) {
        TicketGrantingTicketImpl ticket = null;
        for (int i = 0; i < tgtCount; i++) {
            ticket = new TicketGrantingTicketImpl(
                    GENERATOR.getNewTicketId("TGT"),
                    new ImmutableAuthentication(new SimplePrincipal("grover")),
                    TEST_EXP_POLICY);
            registry.addTicket(ticket);
        }
        String id;
        for (int i = 0; i < stCount; i++) {
            registry.addTicket(ticket.grantServiceTicket(
                    GENERATOR.getNewTicketId("ST"),
                    new MockService("junit"),
                    TEST_EXP_POLICY,
                    false));
        }
    }
}
