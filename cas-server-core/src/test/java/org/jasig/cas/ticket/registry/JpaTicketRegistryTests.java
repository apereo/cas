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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.sql.DataSource;

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.mock.MockService;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.annotation.ProfileValueSourceConfiguration;
import org.springframework.test.annotation.SystemProfileValueSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;


/**
 * Unit test for {@link JpaTicketRegistry} class.
 *
 * @author Marvin S. Addison
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/jpaTestApplicationContext.xml")
@ProfileValueSourceConfiguration(SystemProfileValueSource.class)
public class JpaTicketRegistryTests {
    /** Logger instance. */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** Number of clients contending for operations in concurrent test. */
    private static final int CONCURRENT_SIZE = 20;

    private static UniqueTicketIdGenerator ID_GENERATOR = new DefaultUniqueTicketIdGenerator(64);

    private static ExpirationPolicy EXP_POLICY_TGT = new HardTimeoutExpirationPolicy(1000);

    private static ExpirationPolicy EXP_POLICY_ST = new MultiTimeUseOrTimeoutExpirationPolicy(1, 1000);

    @Autowired
    private PlatformTransactionManager txManager;

    @Autowired
    private JpaTicketRegistry jpaTicketRegistry;

    private JdbcTemplate simpleJdbcTemplate;


    /**
     * Set the datasource.
     */
    @Autowired
    public void setDataSource(final DataSource dataSource) {
        this.simpleJdbcTemplate = new JdbcTemplate(dataSource);
    }


    @Before
    public void setUp() {
        JdbcTestUtils.deleteFromTables(simpleJdbcTemplate, "SERVICETICKET");
        JdbcTestUtils.deleteFromTables(simpleJdbcTemplate, "TICKETGRANTINGTICKET");
    }


    @Test
    public void testTicketCreationAndDeletion() throws Exception {
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
    @IfProfileValue(name="cas.jpa.concurrent", value="true")
    public void testConcurrentServiceTicketGeneration() throws Exception {
        final TicketGrantingTicket newTgt = newTGT();
        addTicketInTransaction(newTgt);
        final ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_SIZE);
        try {
            final List<ServiceTicketGenerator> generators = new ArrayList<ServiceTicketGenerator>(CONCURRENT_SIZE);
            for (int i = 0; i < CONCURRENT_SIZE; i++) {
                generators.add(new ServiceTicketGenerator(newTgt.getId()));
            }
            final List<Future<String>> results = executor.invokeAll(generators);
            for (Future<String> result : results) {
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
        final Principal principal = new SimplePrincipal(
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
        new TransactionTemplate(txManager).execute(new TransactionCallback<Void>() {
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

    class ServiceTicketGenerator implements Callable<String> {

        private String parentTgtId;

        public ServiceTicketGenerator(final String tgtId) {
            parentTgtId = tgtId;
        }

        /** {@inheritDoc} */
        @Override
        public String call() throws Exception {
            return new TransactionTemplate(txManager).execute(new TransactionCallback<String>() {
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
