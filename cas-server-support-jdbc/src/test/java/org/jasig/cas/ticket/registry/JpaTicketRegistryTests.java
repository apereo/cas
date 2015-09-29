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
package org.jasig.cas.ticket.registry;

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.principal.DefaultPrincipalFactory;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.mock.MockService;
import org.jasig.cas.monitor.SessionMonitor;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.support.HardTimeoutExpirationPolicy;
import org.jasig.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicy;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.jasig.cas.util.UniqueTicketIdGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.*;


/**
 * Unit test for {@link JpaTicketRegistry} class.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public class JpaTicketRegistryTests {
    /** Number of clients contending for operations in concurrent test. */
    private static final int CONCURRENT_SIZE = 20;

    private static final UniqueTicketIdGenerator ID_GENERATOR = new DefaultUniqueTicketIdGenerator(64);

    private static final ExpirationPolicy EXP_POLICY_TGT = new HardTimeoutExpirationPolicy(1000);

    private static final ExpirationPolicy EXP_POLICY_ST = new MultiTimeUseOrTimeoutExpirationPolicy(1, 1000);

    /** Logger instance. */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private PlatformTransactionManager txManager;

    private TicketRegistry jpaTicketRegistry;

    @Before
    public void setup() {
        final ClassPathXmlApplicationContext ctx = new
            ClassPathXmlApplicationContext("classpath:/jpaSpringContext.xml");
        this.jpaTicketRegistry = ctx.getBean("jpaTicketRegistry", TicketRegistry.class);
        this.txManager = ctx.getBean("ticketTransactionManager", PlatformTransactionManager.class);
    }

    @Test
    public void verifyTicketCreationAndDeletion() throws Exception {
        final TicketGrantingTicket newTgt = newTGT();
        addTicketInTransaction(newTgt);
        final TicketGrantingTicket tgtFromDb = (TicketGrantingTicket) getTicketInTransaction(newTgt.getId());
        assertNotNull(tgtFromDb);
        assertEquals(newTgt.getId(), tgtFromDb.getId());
        final ServiceTicket newSt = grantServiceTicketInTransaction(tgtFromDb);
        final ServiceTicket stFromDb = (ServiceTicket) getTicketInTransaction(newSt.getId());
        assertNotNull(stFromDb);
        assertEquals(newSt.getId(), stFromDb.getId());
        deleteTicketInTransaction(newTgt.getId());
        assertNull(getTicketInTransaction(newTgt.getId()));
        assertNull(getTicketInTransaction(newSt.getId()));
    }

    @Test
    public void verifyConcurrentServiceTicketGeneration() throws Exception {
        final TicketGrantingTicket newTgt = newTGT();
        addTicketInTransaction(newTgt);
        final ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_SIZE);
        try {
            final List<ServiceTicketGenerator> generators = new ArrayList<>(CONCURRENT_SIZE);
            for (int i = 0; i < CONCURRENT_SIZE; i++) {
                generators.add(new ServiceTicketGenerator(newTgt.getId(), this.jpaTicketRegistry, this.txManager));
            }
            final List<Future<String>> results = executor.invokeAll(generators);
            for (final Future<String> result : results) {
                assertNotNull(result.get());
            }
        } catch (final Exception e) {
            logger.debug("testConcurrentServiceTicketGeneration produced an error", e);
            fail("testConcurrentServiceTicketGeneration failed.");
        } finally {
            executor.shutdownNow();
        }
    }


    static TicketGrantingTicket newTGT() {
        final Principal principal = new DefaultPrincipalFactory().createPrincipal(
                "bob", Collections.singletonMap("displayName", (Object) "Bob"));
        return new TicketGrantingTicketImpl(
                ID_GENERATOR.getNewTicketId("TGT"),
                TestUtils.getAuthentication(principal),
                EXP_POLICY_TGT);
    }

    static ServiceTicket newST(final TicketGrantingTicket parent) {
       return parent.grantServiceTicket(
               ID_GENERATOR.getNewTicketId("ST"),
               new MockService("https://service.example.com"),
               EXP_POLICY_ST,
               false);
    }

    void addTicketInTransaction(final Ticket ticket) {
        new TransactionTemplate(txManager).execute(new TransactionCallback<Object>() {
            public Void doInTransaction(final TransactionStatus status) {
                jpaTicketRegistry.addTicket(ticket);
                return null;
            }
        });
    }

    void deleteTicketInTransaction(final String ticketId) {
        new TransactionTemplate(txManager).execute(new TransactionCallback<Void>() {
            public Void doInTransaction(final TransactionStatus status) {
                jpaTicketRegistry.deleteTicket(ticketId);
                return null;
            }
        });
    }

    Ticket getTicketInTransaction(final String ticketId) {
        return new TransactionTemplate(txManager).execute(new TransactionCallback<Ticket>() {
            public Ticket doInTransaction(final TransactionStatus status) {
                return jpaTicketRegistry.getTicket(ticketId);
            }
        });
    }

    ServiceTicket grantServiceTicketInTransaction(final TicketGrantingTicket parent) {
        return new TransactionTemplate(txManager).execute(new TransactionCallback<ServiceTicket>() {
            public ServiceTicket doInTransaction(final TransactionStatus status) {
                final ServiceTicket st = newST(parent);
                jpaTicketRegistry.addTicket(st);
                return st;
            }
        });
    }

    private static class ServiceTicketGenerator implements Callable<String> {
        private final PlatformTransactionManager txManager;
        private final String parentTgtId;
        private final TicketRegistry jpaTicketRegistry;

        ServiceTicketGenerator(final String tgtId, final TicketRegistry jpaTicketRegistry,
                                      final PlatformTransactionManager txManager) {
            parentTgtId = tgtId;
            this.jpaTicketRegistry = jpaTicketRegistry;
            this.txManager = txManager;
        }


        @Override
        public String call() throws Exception {
            return new TransactionTemplate(txManager).execute(new TransactionCallback<String>() {
                @Override
                public String doInTransaction(final TransactionStatus status) {
                    // Querying for the TGT prior to updating it as done in
                    // CentralAuthenticationServiceImpl#grantServiceTicket(String, Service, Credential)
                    final ServiceTicket st = newST((TicketGrantingTicket) jpaTicketRegistry.getTicket(parentTgtId));
                    jpaTicketRegistry.addTicket(st);
                    return st.getId();
                }
            });
        }

    }
}
