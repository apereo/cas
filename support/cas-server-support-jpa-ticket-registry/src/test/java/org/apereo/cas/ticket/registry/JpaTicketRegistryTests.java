package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.JpaTicketRegistryConfiguration;
import org.apereo.cas.config.JpaTicketRegistryTicketCatalogConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.config.support.EnvironmentConversionServiceInitializer;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
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
import org.apereo.cas.util.SchedulingUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
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
@SpringBootTest(classes = {
        JpaTicketRegistryTests.JpaTestConfiguration.class,
        RefreshAutoConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasCoreAuthenticationConfiguration.class, 
        CasCoreServicesAuthenticationConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasCoreAuthenticationPolicyConfiguration.class,
        CasCoreAuthenticationMetadataConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasCoreAuthenticationHandlersConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasPersonDirectoryConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasCoreConfiguration.class,
        CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        JpaTicketRegistryTicketCatalogConfiguration.class,
        JpaTicketRegistryConfiguration.class,
        CasCoreWebConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class})
@ContextConfiguration(initializers = EnvironmentConversionServiceInitializer.class)
public class JpaTicketRegistryTests {
    /**
     * Number of clients contending for operations in concurrent test.
     */
    private static final int CONCURRENT_SIZE = 20;

    private static final UniqueTicketIdGenerator ID_GENERATOR = new DefaultUniqueTicketIdGenerator(64);

    private static final ExpirationPolicy EXP_POLICY_TGT = new HardTimeoutExpirationPolicy(1000);

    private static final ExpirationPolicy EXP_POLICY_ST = new MultiTimeUseOrTimeoutExpirationPolicy(1, 1000);

    private static final ExpirationPolicy EXP_POLICY_PGT = new HardTimeoutExpirationPolicy(2000);

    private static final ExpirationPolicy EXP_POLICY_PT = new MultiTimeUseOrTimeoutExpirationPolicy(1, 2000);

    private static final Logger LOGGER = LoggerFactory.getLogger(JpaTicketRegistryTests.class);

    @Autowired
    @Qualifier("ticketTransactionManager")
    private PlatformTransactionManager txManager;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @TestConfiguration
    public static class JpaTestConfiguration {
        @Autowired
        protected ApplicationContext applicationContext;

        @PostConstruct
        public void init() {
            SchedulingUtils.prepScheduledAnnotationBeanPostProcessor(applicationContext);
        }
    }
    
    @Test
    public void verifyTicketDeletionInBulk() {
        final TicketGrantingTicket newTgt = newTGT();
        addTicketInTransaction(newTgt);
        final TicketGrantingTicket tgtFromDb = (TicketGrantingTicket) getTicketInTransaction(newTgt.getId());
        final ServiceTicket newSt = grantServiceTicketInTransaction(tgtFromDb);
        final ServiceTicket stFromDb = (ServiceTicket) getTicketInTransaction(newSt.getId());
        final ProxyGrantingTicket newPgt = grantProxyGrantingTicketInTransaction(stFromDb);
        final ProxyGrantingTicket pgtFromDb = (ProxyGrantingTicket) getTicketInTransaction(newPgt.getId());
        final ProxyTicket newPt = grantProxyTicketInTransaction(pgtFromDb);

        getTicketInTransaction(newPt.getId());
        deleteTicketsInTransaction();
    }


    @Test
    public void verifyTicketCreationAndDeletion() {
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
        updateTicketInTransaction(stFromDb.getGrantingTicket());
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
        updateTicketInTransaction(st2FromDb.getGrantingTicket());
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
    public void verifyConcurrentServiceTicketGeneration() {
        final TicketGrantingTicket newTgt = newTGT();
        addTicketInTransaction(newTgt);
        final ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_SIZE);
        try {
            final List<ServiceTicketGenerator> generators = new ArrayList<>(CONCURRENT_SIZE);
            for (int i = 0; i < CONCURRENT_SIZE; i++) {
                generators.add(new ServiceTicketGenerator(newTgt.getId(), this.ticketRegistry, this.txManager));
            }
            final List<Future<String>> results = executor.invokeAll(generators);
            for (final Future<String> result : results) {
                assertNotNull(result.get());
            }
        } catch (final Exception e) {
            LOGGER.error("testConcurrentServiceTicketGeneration produced an error", e);
            fail("testConcurrentServiceTicketGeneration failed.");
        } finally {
            executor.shutdownNow();
        }

        assertEquals(CONCURRENT_SIZE, this.ticketRegistry.getTickets().size() - 1);
    }
    
    static TicketGrantingTicket newTGT() {
        final Principal principal = new DefaultPrincipalFactory().createPrincipal(
                "bob", Collections.singletonMap("displayName", "Bob"));
        return new TicketGrantingTicketImpl(
                ID_GENERATOR.getNewTicketId(TicketGrantingTicket.PREFIX),
                CoreAuthenticationTestUtils.getAuthentication(principal),
                EXP_POLICY_TGT);
    }

    static ServiceTicket newST(final TicketGrantingTicket parent) {
        final Service testService = RegisteredServiceTestUtils.getService("https://service.example.com");
        return parent.grantServiceTicket(
                ID_GENERATOR.getNewTicketId(ServiceTicket.PREFIX),
                testService,
                EXP_POLICY_ST,
                false,
                true);
    }

    static ProxyGrantingTicket newPGT(final ServiceTicket parent) {
        try {
            return parent.grantProxyGrantingTicket(
                    ID_GENERATOR.getNewTicketId(ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX),
                    CoreAuthenticationTestUtils.getAuthentication(),
                    EXP_POLICY_PGT);
        } catch (final AbstractTicketException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    static ProxyTicket newPT(final ProxyGrantingTicket parent) {
        final Service testService = RegisteredServiceTestUtils.getService("https://proxy-service.example.com");
        return parent.grantProxyTicket(
                ID_GENERATOR.getNewTicketId(ProxyTicket.PROXY_TICKET_PREFIX),
                testService,
                EXP_POLICY_PT,
                false);
    }

    private void addTicketInTransaction(final Ticket ticket) {
        new TransactionTemplate(txManager).execute(status -> {
            ticketRegistry.addTicket(ticket);
            return null;
        });
    }

    private void updateTicketInTransaction(final Ticket ticket) {
        new TransactionTemplate(txManager).execute(status -> {
            ticketRegistry.updateTicket(ticket);
            return null;
        });
    }

    private void deleteTicketsInTransaction() {
        new TransactionTemplate(txManager).execute((TransactionCallback<Void>) status -> {
            ticketRegistry.deleteAll();
            return null;
        });
    }

    private void deleteTicketInTransaction(final String ticketId) {
        new TransactionTemplate(txManager).execute((TransactionCallback<Void>) status -> {
            ticketRegistry.deleteTicket(ticketId);
            return null;
        });
    }

    private Ticket getTicketInTransaction(final String ticketId) {
        return new TransactionTemplate(txManager).execute(status -> ticketRegistry.getTicket(ticketId));
    }

    private ServiceTicket grantServiceTicketInTransaction(final TicketGrantingTicket parent) {
        return new TransactionTemplate(txManager).execute(status -> {
            final ServiceTicket st = newST(parent);
            ticketRegistry.addTicket(st);
            return st;
        });
    }

    private ProxyGrantingTicket grantProxyGrantingTicketInTransaction(final ServiceTicket parent) {
        return new TransactionTemplate(txManager).execute(status -> {
            final ProxyGrantingTicket pgt = newPGT(parent);
            ticketRegistry.addTicket(pgt);
            return pgt;
        });
    }

    private ProxyTicket grantProxyTicketInTransaction(final ProxyGrantingTicket parent) {
        return new TransactionTemplate(txManager).execute(status -> {
            final ProxyTicket st = newPT(parent);
            ticketRegistry.addTicket(st);
            return st;
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
        public String call() {
            return new TransactionTemplate(txManager).execute(status -> {
                final ServiceTicket st = newST((TicketGrantingTicket) jpaTicketRegistry.getTicket(parentTgtId));
                jpaTicketRegistry.addTicket(st);
                return st.getId();
            });
        }
    }
}
