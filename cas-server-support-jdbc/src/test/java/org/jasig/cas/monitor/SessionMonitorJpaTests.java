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
package org.jasig.cas.monitor;

import org.jasig.cas.TestUtils;
import org.jasig.cas.mock.MockService;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
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

/**
 * Unit test for {@link org.jasig.cas.monitor.SessionMonitor} class that involves {@link JpaTicketRegistry}.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/jpaTestApplicationContext.xml"})
@Transactional
public class SessionMonitorJpaTests {

    private static final ExpirationPolicy TEST_EXP_POLICY = new HardTimeoutExpirationPolicy(10000);
    private static final UniqueTicketIdGenerator GENERATOR = new DefaultUniqueTicketIdGenerator();

    @Autowired
    private JpaTicketRegistry jpaRegistry;
    private SessionMonitor monitor;

    @Before
    public void setUp() {
        this.monitor = new SessionMonitor();
    }

    @Test
    @Rollback(false)
    public void verifyObserveOkJpaTicketRegistry() throws Exception {
        addTicketsToRegistry(this.jpaRegistry, 5, 5);
        assertEquals(10, this.jpaRegistry.getTickets().size());
        this.monitor.setTicketRegistry(this.jpaRegistry);
        final SessionStatus status = this.monitor.observe();
        assertEquals(5, status.getSessionCount());
        assertEquals(5, status.getServiceTicketCount());
        assertEquals(StatusCode.OK, status.getCode());
    }

    private void addTicketsToRegistry(final TicketRegistry registry, final int tgtCount, final int stCount) {
        TicketGrantingTicketImpl ticket = null;
        for (int i = 0; i < tgtCount; i++) {
            ticket = new TicketGrantingTicketImpl(
                    GENERATOR.getNewTicketId("TGT"),
                    TestUtils.getAuthentication(),
                    TEST_EXP_POLICY);
            registry.addTicket(ticket);
        }

        if (ticket != null) {
          for (int i = 0; i < stCount; i++) {
              registry.addTicket(ticket.grantServiceTicket(
                      GENERATOR.getNewTicketId("ST"),
                      new MockService("junit"),
                      TEST_EXP_POLICY,
                      false));
          }
        }
    }
}
