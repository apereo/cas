package org.apereo.cas.ticket.registry;

import com.google.common.base.Throwables;
import org.apereo.cas.authentication.TestUtils;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.config.JpaTicketRegistryConfiguration;
import org.apereo.cas.mock.MockService;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.ticket.support.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicy;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;
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
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RefreshAutoConfiguration.class, JpaTicketRegistryConfiguration.class})
public class JpaTicketRegistryTests {
    /** Number of clients contending for operations in concurrent test. */
    private static final int CONCURRENT_SIZE = 20;

    private static final UniqueTicketIdGenerator ID_GENERATOR = new DefaultUniqueTicketIdGenerator(64);

    private static final ExpirationPolicy EXP_POLICY_TGT = new HardTimeoutExpirationPolicy(1000);

    private static final ExpirationPolicy EXP_POLICY_ST = new MultiTimeUseOrTimeoutExpirationPolicy(1, 1000);

    private static final ExpirationPolicy EXP_POLICY_PGT = new HardTimeoutExpirationPolicy(2000);

    private static final ExpirationPolicy EXP_POLICY_PT = new MultiTimeUseOrTimeoutExpirationPolicy(1, 2000);

    /** Logger instance. */
    private transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    @Qualifier("ticketTransactionManager")
    private PlatformTransactionManager txManager;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry jpaTicketRegistry;

    @Test
    public void verifyTicketCreationAndDeletion() throws Exception {
        // TGT
        final TicketGrantingTicket newTgt = newTGT();
        addTicketInTransaction(newTgt);
        TicketGrantingTicket tgtFromDb = (TicketGrantingTicket) getTicketInTransaction(newTgt.getId());
        assertNotNull(tgtFromDb);
        assertEquals(newTgt.getId(), tgtFromDb.getId());

        // ST
        final ServiceTicket newSt = grantServiceTicketInTransaction(tgtFromDb);
        final ServiceTicket stFromDb = (ServiceTicket) getTicketInTransaction(newSt.getId());
        assertNotNull(stFromDb);
        assertEquals(newSt.getId(), stFromDb.getId());

        // PGT
        final ProxyGrantingTicket newPgt = grantProxyGrantingTicketInTransaction(stFromDb);
        final ProxyGrantingTicket pgtFromDb = (ProxyGrantingTicket) getTicketInTransaction(newPgt.getId());
        assertNotNull(pgtFromDb);
        assertEquals(newPgt.getId(), pgtFromDb.getId());

        tgtFromDb = (TicketGrantingTicket) getTicketInTransaction(newTgt.getId());
        assertNotNull(tgtFromDb);
        assertEquals(1, tgtFromDb.getProxyGrantingTickets().size());

        // PT
        final ProxyTicket newPt = grantProxyTicketInTransaction(pgtFromDb);
        final ProxyTicket ptFromDb = (ProxyTicket) getTicketInTransaction(newPt.getId());
        assertNotNull(ptFromDb);
        assertEquals(newPt.getId(), ptFromDb.getId());

        // ST 2
        final ServiceTicket newSt2 = grantServiceTicketInTransaction(tgtFromDb);
        final ServiceTicket st2FromDb = (ServiceTicket) getTicketInTransaction(newSt2.getId());
        assertNotNull(st2FromDb);
        assertEquals(newSt2.getId(), st2FromDb.getId());

        // PGT 2
        final ProxyGrantingTicket newPgt2 = grantProxyGrantingTicketInTransaction(st2FromDb);
        final ProxyGrantingTicket pgt2FromDb = (ProxyGrantingTicket) getTicketInTransaction(newPgt2.getId());
        assertNotNull(pgt2FromDb);
        assertEquals(newPgt2.getId(), pgt2FromDb.getId());

        tgtFromDb = (TicketGrantingTicket) getTicketInTransaction(newTgt.getId());
        assertNotNull(tgtFromDb);
        assertEquals(2, tgtFromDb.getProxyGrantingTickets().size());

        // delete PGT 2
        deleteTicketInTransaction(pgt2FromDb.getId());
        assertNull(getTicketInTransaction(newPgt2.getId()));
        tgtFromDb = (TicketGrantingTicket) getTicketInTransaction(newTgt.getId());
        assertNotNull(tgtFromDb);
        assertEquals(1, tgtFromDb.getProxyGrantingTickets().size());

        // delete ticket hierarchy
        tgtFromDb = (TicketGrantingTicket) getTicketInTransaction(newTgt.getId());
        assertNotNull(tgtFromDb);
        deleteTicketInTransaction(tgtFromDb.getId());
        assertNull(getTicketInTransaction(newTgt.getId()));
        assertNull(getTicketInTransaction(newSt.getId()));
        assertNull(getTicketInTransaction(newPgt.getId()));
        assertNull(getTicketInTransaction(newPt.getId()));
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
            logger.error("testConcurrentServiceTicketGeneration produced an error", e);
            fail("testConcurrentServiceTicketGeneration failed.");
        } finally {
            executor.shutdownNow();
        }

        final TicketGrantingTicket tgtFromDb = (TicketGrantingTicket) getTicketInTransaction(newTgt.getId());
        assertEquals(CONCURRENT_SIZE, tgtFromDb.getCountOfUses());
    }


    static TicketGrantingTicket newTGT() {
        final Principal principal = new DefaultPrincipalFactory().createPrincipal(
                "bob", Collections.singletonMap("displayName", (Object) "Bob"));
        return new TicketGrantingTicketImpl(
                ID_GENERATOR.getNewTicketId(TicketGrantingTicket.PREFIX),
                TestUtils.getAuthentication(principal),
                EXP_POLICY_TGT);
    }

    static ServiceTicket newST(final TicketGrantingTicket parent) {
       return parent.grantServiceTicket(
               ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
               new MockService("https://service.example.com"),
               EXP_POLICY_ST,
               false,
               true);
    }

    static ProxyGrantingTicket newPGT(final ServiceTicket parent) {
        try {
            return parent.grantProxyGrantingTicket(
                    ID_GENERATOR.getNewTicketId(ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX),
                    TestUtils.getAuthentication(),
                    EXP_POLICY_PGT);
        } catch (final AbstractTicketException e) {
            throw Throwables.propagate(e);
        }
    }

    static ProxyTicket newPT(final ProxyGrantingTicket parent) {
        return parent.grantProxyTicket(
                ID_GENERATOR.getNewTicketId(ProxyTicket.PROXY_TICKET_PREFIX),
                new MockService("https://proxy-service.example.com"),
                EXP_POLICY_PT,
                false);
    }

    private void addTicketInTransaction(final Ticket ticket) {
        new TransactionTemplate(txManager).execute(status -> {
            jpaTicketRegistry.addTicket(ticket);
            return null;
        });
    }

    private void deleteTicketInTransaction(final String ticketId) {
        new TransactionTemplate(txManager).execute((TransactionCallback<Void>) status -> {
            jpaTicketRegistry.deleteTicket(ticketId);
            return null;
        });
    }

    private Ticket getTicketInTransaction(final String ticketId) {
        return new TransactionTemplate(txManager).execute(status -> jpaTicketRegistry.getTicket(ticketId));
    }

    private ServiceTicket grantServiceTicketInTransaction(final TicketGrantingTicket parent) {
        return new TransactionTemplate(txManager).execute(status -> {
            final ServiceTicket st = newST(parent);
            jpaTicketRegistry.addTicket(st);
            return st;
        });
    }

    private ProxyGrantingTicket grantProxyGrantingTicketInTransaction(final ServiceTicket parent) {
        return new TransactionTemplate(txManager).execute(status -> {
            final ProxyGrantingTicket pgt = newPGT(parent);
            jpaTicketRegistry.addTicket(pgt);
            return pgt;
        });
    }

    private ProxyTicket grantProxyTicketInTransaction(final ProxyGrantingTicket parent) {
        return new TransactionTemplate(txManager).execute(status -> {
            final ProxyTicket st = newPT(parent);
            jpaTicketRegistry.addTicket(st);
            return st;
        });
    }

    private static class ServiceTicketGenerator implements Callable<String> {
        private PlatformTransactionManager txManager;
        private String parentTgtId;
        private TicketRegistry jpaTicketRegistry;

        ServiceTicketGenerator(final String tgtId, final TicketRegistry jpaTicketRegistry,
                                      final PlatformTransactionManager txManager) {
            parentTgtId = tgtId;
            this.jpaTicketRegistry = jpaTicketRegistry;
            this.txManager = txManager;
        }


        @Override
        public String call() throws Exception {
            return new TransactionTemplate(txManager).execute(status -> {
                // Querying for the TGT prior to updating it as done in
                // CentralAuthenticationServiceImpl#grantServiceTicket(String, Service, Credential)
                final ServiceTicket st = newST((TicketGrantingTicket) jpaTicketRegistry.getTicket(parentTgtId));
                jpaTicketRegistry.addTicket(st);
                return st.getId();
            });
        }

    }
}
