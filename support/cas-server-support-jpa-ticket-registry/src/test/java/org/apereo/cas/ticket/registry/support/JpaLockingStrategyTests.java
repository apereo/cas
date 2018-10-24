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

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
@SpringBootTest(classes = {
    JpaTicketRegistryTicketCatalogConfiguration.class,
    JpaTicketRegistryConfiguration.class,
    JpaLockingStrategyTests.JpaTestConfiguration.class,
    JpaTicketRegistryTicketCatalogConfiguration.class,
    JpaTicketRegistryConfiguration.class,
    RefreshAutoConfiguration.class,
    AopAutoConfiguration.class,
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
    CasPersonDirectoryConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class})
@ContextConfiguration(initializers = EnvironmentConversionServiceInitializer.class)
@DirtiesContext
@Slf4j
public class JpaLockingStrategyTests {
    /**
     * Number of clients contending for lock in concurrent test.
     */
    private static final int CONCURRENT_SIZE = 13;

    @Autowired
    @Qualifier("ticketTransactionManager")
    private PlatformTransactionManager txManager;

    @Autowired
    @Qualifier("ticketEntityManagerFactory")
    private EntityManagerFactory factory;

    @Autowired
    @Qualifier("dataSourceTicket")
    private DataSource dataSource;

    private static void testConcurrency(final ExecutorService executor,
                                        final Collection<LockingStrategy> locks) throws Exception {
        val lockers = new ArrayList<Locker>(locks.size());
        lockers.addAll(locks.stream().map(Locker::new).collect(Collectors.toList()));

        val lockCount = executor.invokeAll(lockers).stream().filter(result -> {
            try {
                return result.get();
            } catch (final InterruptedException | ExecutionException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }).count();
        assertTrue("Lock count should be <= 1 but was " + lockCount, lockCount <= 1);
        
        val releaseCount = executor.invokeAll(lockers).stream().filter(result -> {
            try {
                return result.get();
            } catch (final InterruptedException | ExecutionException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }).count();
        assertTrue("Release count should be <= 1 but was " + releaseCount, releaseCount <= 1);
    }

    /**
     * Test basic acquire/release semantics.
     */
    @Test
    public void verifyAcquireAndRelease() {
        try {
            val appId = "basic";
            val uniqueId = appId + "-1";
            val lock = newLockTxProxy(appId, uniqueId, JpaTicketRegistryProperties.DEFAULT_LOCK_TIMEOUT);
            assertTrue(lock.acquire());
            assertEquals(uniqueId, getOwner(appId));
            lock.release();
            assertNull(getOwner(appId));
        } catch (final Exception e) {
            LOGGER.debug("testAcquireAndRelease produced an error", e);
            throw new AssertionError("testAcquireAndRelease failed");
        }
    }

    @Test
    public void verifyLockExpiration() {
        try {
            val appId = "expquick";
            val uniqueId = appId + "-1";
            val lock = newLockTxProxy(appId, uniqueId, "1");
            assertTrue(lock.acquire());
            assertEquals(uniqueId, getOwner(appId));
            lock.release();
            assertTrue(lock.acquire());
            lock.release();
            assertNull(getOwner(appId));
        } catch (final Exception e) {
            LOGGER.debug("testLockExpiration produced an error", e);
            throw new AssertionError("testLockExpiration failed");
        }
    }

    /**
     * Verify non-reentrant behavior.
     */
    @Test
    public void verifyNonReentrantBehavior() {
        try {
            val appId = "reentrant";
            val uniqueId = appId + "-1";
            val lock = newLockTxProxy(appId, uniqueId, JpaTicketRegistryProperties.DEFAULT_LOCK_TIMEOUT);
            assertTrue(lock.acquire());
            assertEquals(uniqueId, getOwner(appId));
            assertFalse(lock.acquire());
            lock.release();
            assertNull(getOwner(appId));
        } catch (final Exception e) {
            throw new AssertionError("testNonReentrantBehavior failed.", e);
        }
    }

    /**
     * Test concurrent acquire/release semantics.
     */
    @Test
    public void verifyConcurrentAcquireAndRelease() {
        val executor = Executors.newFixedThreadPool(CONCURRENT_SIZE);
        try {
            testConcurrency(executor, Arrays.asList(getConcurrentLocks("concurrent-new")));
        } catch (final Exception e) {
            throw new AssertionError("testConcurrentAcquireAndRelease failed.", e);
        } finally {
            executor.shutdownNow();
        }
    }

    /**
     * Test concurrent acquire/release semantics for existing lock.
     */
    @Test
    public void verifyConcurrentAcquireAndReleaseOnExistingLock() {
        val locks = getConcurrentLocks("concurrent-exists");
        locks[0].acquire();
        locks[0].release();
        val executor = Executors.newFixedThreadPool(CONCURRENT_SIZE);
        try {
            testConcurrency(executor, Arrays.asList(locks));
        } catch (final Exception e) {
            throw new AssertionError("testConcurrentAcquireAndReleaseOnExistingLock failed.", e);
        } finally {
            executor.shutdownNow();
        }
    }

    private LockingStrategy[] getConcurrentLocks(final String appId) {
        val locks = new LockingStrategy[CONCURRENT_SIZE];
        IntStream.rangeClosed(1, locks.length)
            .forEach(i -> locks[i - 1] = newLockTxProxy(appId, appId + '-' + i, JpaTicketRegistryProperties.DEFAULT_LOCK_TIMEOUT));
        return locks;
    }

    private LockingStrategy newLockTxProxy(final String appId, final String uniqueId, final String ttl) {
        val lock = new JpaLockingStrategy(appId, uniqueId, Beans.newDuration(ttl).getSeconds());
        lock.entityManager = SharedEntityManagerCreator.createSharedEntityManager(factory);
        return (LockingStrategy) Proxy.newProxyInstance(
            JpaLockingStrategy.class.getClassLoader(),
            new Class[]{LockingStrategy.class},
            new TransactionalLockInvocationHandler(lock, this.txManager));
    }

    private String getOwner(final String appId) {
        val simpleJdbcTemplate = new JdbcTemplate(dataSource);
        val results = simpleJdbcTemplate.queryForList(
            "SELECT unique_id FROM locks WHERE application_id=?", appId);
        if (results.isEmpty()) {
            return null;
        }
        return (String) results.get(0).get("unique_id");
    }

    @TestConfiguration
    public static class JpaTestConfiguration implements InitializingBean {
        @Autowired
        protected ApplicationContext applicationContext;

        @Override
        public void afterPropertiesSet() {
            SchedulingUtils.prepScheduledAnnotationBeanPostProcessor(applicationContext);
        }
    }

    private static class TransactionalLockInvocationHandler implements InvocationHandler {


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
                    val result = method.invoke(jpaLock, args);
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
