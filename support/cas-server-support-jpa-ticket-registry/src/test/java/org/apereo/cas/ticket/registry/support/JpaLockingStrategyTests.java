package org.apereo.cas.ticket.registry.support;

import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
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
import org.apereo.cas.configuration.model.support.jpa.ticketregistry.JpaTicketRegistryProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * Unit test for {@link JpaLockingStrategy}.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        JpaLockingStrategyTests.JpaTestConfiguration.class,
        RefreshAutoConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasCoreConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasCoreAuthenticationMetadataConfiguration.class,
        CasCoreAuthenticationHandlersConfiguration.class,
        CasCoreAuthenticationPolicyConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        JpaTicketRegistryTicketCatalogConfiguration.class,
        CasPersonDirectoryConfiguration.class,
        JpaTicketRegistryConfiguration.class,
        CasCoreWebConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class})
@ContextConfiguration(initializers = EnvironmentConversionServiceInitializer.class)
@DirtiesContext
public class JpaLockingStrategyTests {
    /**
     * Number of clients contending for lock in concurrent test.
     */
    private static final int CONCURRENT_SIZE = 13;

    private static final Logger LOGGER = LoggerFactory.getLogger(JpaLockingStrategyTests.class);

    @Autowired
    @Qualifier("ticketTransactionManager")
    private PlatformTransactionManager txManager;

    @Autowired
    @Qualifier("ticketEntityManagerFactory")
    private EntityManagerFactory factory;

    @Autowired
    @Qualifier("dataSourceTicket")
    private DataSource dataSource;

    @TestConfiguration
    public static class JpaTestConfiguration {
        @Autowired
        protected ApplicationContext applicationContext;

        @PostConstruct
        public void init() {
            SchedulingUtils.prepScheduledAnnotationBeanPostProcessor(applicationContext);
        }
    }

    /**
     * Test basic acquire/release semantics.
     *
     */
    @Test
    public void verifyAcquireAndRelease() {
        try {
            final String appId = "basic";
            final String uniqueId = appId + "-1";
            final LockingStrategy lock = newLockTxProxy(appId, uniqueId, JpaTicketRegistryProperties.DEFAULT_LOCK_TIMEOUT);
            assertTrue(lock.acquire());
            assertEquals(uniqueId, getOwner(appId));
            lock.release();
            assertNull(getOwner(appId));
        } catch (final Exception e) {
            LOGGER.debug("testAcquireAndRelease produced an error", e);
            fail("testAcquireAndRelease failed");
        }
    }
    
    @Test
    public void verifyLockExpiration() {
        try {
            final String appId = "expquick";
            final String uniqueId = appId + "-1";
            final LockingStrategy lock = newLockTxProxy(appId, uniqueId, "1");
            assertTrue(lock.acquire());
            assertEquals(uniqueId, getOwner(appId));
            lock.release();
            assertTrue(lock.acquire());
            lock.release();
            assertNull(getOwner(appId));
        } catch (final Exception e) {
            LOGGER.debug("testLockExpiration produced an error", e);
            fail("testLockExpiration failed");
        }
    }

    /**
     * Verify non-reentrant behavior.
     */
    @Test
    public void verifyNonReentrantBehavior() {
        try {
            final String appId = "reentrant";
            final String uniqueId = appId + "-1";
            final LockingStrategy lock = newLockTxProxy(appId, uniqueId, JpaTicketRegistryProperties.DEFAULT_LOCK_TIMEOUT);
            assertTrue(lock.acquire());
            assertEquals(uniqueId, getOwner(appId));
            assertFalse(lock.acquire());
            lock.release();
            assertNull(getOwner(appId));
        } catch (final Exception e) {
            LOGGER.debug("testNonReentrantBehavior produced an error", e);
            fail("testNonReentrantBehavior failed.");
        }
    }

    /**
     * Test concurrent acquire/release semantics.
     */
    @Test
    public void verifyConcurrentAcquireAndRelease() {
        final ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_SIZE);
        try {
            testConcurrency(executor, Arrays.asList(getConcurrentLocks("concurrent-new")));
        } catch (final Exception e) {
            LOGGER.debug("testConcurrentAcquireAndRelease produced an error", e);
            fail("testConcurrentAcquireAndRelease failed.");
        } finally {
            executor.shutdownNow();
        }
    }

    /**
     * Test concurrent acquire/release semantics for existing lock.
     */
    @Test
    public void verifyConcurrentAcquireAndReleaseOnExistingLock() {
        final LockingStrategy[] locks = getConcurrentLocks("concurrent-exists");
        locks[0].acquire();
        locks[0].release();
        final ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_SIZE);
        try {
            testConcurrency(executor, Arrays.asList(locks));
        } catch (final Exception e) {
            LOGGER.debug("testConcurrentAcquireAndReleaseOnExistingLock produced an error", e);
            fail("testConcurrentAcquireAndReleaseOnExistingLock failed.");
        } finally {
            executor.shutdownNow();
        }
    }

    private LockingStrategy[] getConcurrentLocks(final String appId) {
        final LockingStrategy[] locks = new LockingStrategy[CONCURRENT_SIZE];
        IntStream.rangeClosed(1, locks.length)
                .forEach(i -> locks[i - 1] = newLockTxProxy(appId, appId + '-' + i, JpaTicketRegistryProperties.DEFAULT_LOCK_TIMEOUT));
        return locks;
    }

    private LockingStrategy newLockTxProxy(final String appId, final String uniqueId, final String ttl) {
        final JpaLockingStrategy lock = new JpaLockingStrategy(appId, uniqueId, Beans.newDuration(ttl).getSeconds());
        lock.entityManager = SharedEntityManagerCreator.createSharedEntityManager(factory);
        return (LockingStrategy) Proxy.newProxyInstance(
                JpaLockingStrategy.class.getClassLoader(),
                new Class[]{LockingStrategy.class},
                new TransactionalLockInvocationHandler(lock, this.txManager));
    }

    private String getOwner(final String appId) {
        final JdbcTemplate simpleJdbcTemplate = new JdbcTemplate(dataSource);
        final List<Map<String, Object>> results = simpleJdbcTemplate.queryForList(
                "SELECT unique_id FROM locks WHERE application_id=?", appId);
        if (results.isEmpty()) {
            return null;
        }
        return (String) results.get(0).get("unique_id");
    }

    private static void testConcurrency(final ExecutorService executor,
                                        final Collection<LockingStrategy> locks) throws Exception {
        final List<Locker> lockers = new ArrayList<>(locks.size());
        lockers.addAll(locks.stream().map(Locker::new).collect(Collectors.toList()));

        final long lockCount = executor.invokeAll(lockers).stream().filter(result -> {
            try {
                return result.get();
            } catch (final InterruptedException | ExecutionException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }).count();
        assertTrue("Lock count should be <= 1 but was " + lockCount, lockCount <= 1);

        final List<Releaser> releasers = new ArrayList<>(locks.size());

        releasers.addAll(locks.stream().map(Releaser::new).collect(Collectors.toList()));
        final long releaseCount = executor.invokeAll(lockers).stream().filter(result -> {
            try {
                return result.get();
            } catch (final InterruptedException | ExecutionException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }).count();
        assertTrue("Release count should be <= 1 but was " + releaseCount, releaseCount <= 1);
    }

    private static class TransactionalLockInvocationHandler implements InvocationHandler {
        private static final Logger LOGGER = LoggerFactory.getLogger(TransactionalLockInvocationHandler.class);

        private final JpaLockingStrategy jpaLock;
        private final PlatformTransactionManager txManager;

        TransactionalLockInvocationHandler(final JpaLockingStrategy lock,
                                           final PlatformTransactionManager txManager) {
            jpaLock = lock;
            this.txManager = txManager;
        }

        public JpaLockingStrategy getLock() {
            return this.jpaLock;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) {
            return new TransactionTemplate(txManager).execute(status -> {
                try {
                    final Object result = method.invoke(jpaLock, args);
                    jpaLock.entityManager.flush();
                    LOGGER.debug("Performed [{}] on [{}]", method.getName(), jpaLock);
                    return result;
                    // Force result of transaction to database
                } catch (final Exception e) {
                    throw new IllegalArgumentException("Transactional method invocation failed.", e);
                }
            });
        }

    }

    private static class Locker implements Callable<Boolean> {
        private static final Logger LOGGER = LoggerFactory.getLogger(Locker.class);

        private final LockingStrategy lock;

        Locker(final LockingStrategy l) {
            lock = l;
        }

        @Override
        public Boolean call() {
            try {
                return lock.acquire();
            } catch (final Exception e) {
                LOGGER.debug("[{}] failed to acquire lock", lock, e);
                return false;
            }
        }
    }

    private static class Releaser implements Callable<Boolean> {
        private static final Logger LOGGER = LoggerFactory.getLogger(Releaser.class);

        private final LockingStrategy lock;

        Releaser(final LockingStrategy l) {
            lock = l;
        }

        @Override
        public Boolean call() {
            try {
                lock.release();
                return true;
            } catch (final Exception e) {
                LOGGER.debug("[{}] failed to release lock", lock, e);
                return false;
            }
        }
    }

}
