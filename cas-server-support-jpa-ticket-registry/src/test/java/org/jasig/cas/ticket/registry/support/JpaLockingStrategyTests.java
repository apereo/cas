package org.jasig.cas.ticket.registry.support;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

/**
 * Unit test for {@link JpaLockingStrategy}.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public class JpaLockingStrategyTests {
    /** Number of clients contending for lock in concurrent test. */
    private static final int CONCURRENT_SIZE = 13;

    /** Logger instance. */
    private final transient Logger logger = LoggerFactory.getLogger(getClass());


    private PlatformTransactionManager txManager;


    private EntityManagerFactory factory;


    private DataSource dataSource;

    @Before
    public void setup() {
        final ClassPathXmlApplicationContext ctx = new
            ClassPathXmlApplicationContext("classpath:/jpaSpringContext.xml");
        this.factory = ctx.getBean("ticketEntityManagerFactory", EntityManagerFactory.class);
        this.txManager = ctx.getBean("ticketTransactionManager", PlatformTransactionManager.class);
        this.dataSource = ctx.getBean("dataSourceTicket", DataSource.class);
    }

    /**
     * Test basic acquire/release semantics.
     *
     * @throws Exception On errors.
     */
    @Test
    public void verifyAcquireAndRelease() throws Exception {
        final String appId = "basic";
        final String uniqueId = appId + "-1";
        final LockingStrategy lock = newLockTxProxy(appId, uniqueId, JpaLockingStrategy.DEFAULT_LOCK_TIMEOUT);
        try {
            assertTrue(lock.acquire());
            assertEquals(uniqueId, getOwner(appId));
            lock.release();
            assertNull(getOwner(appId));
        } catch (final Exception e) {
            logger.debug("testAcquireAndRelease produced an error", e);
            fail("testAcquireAndRelease failed");
        }
    }

    /**
     * Test lock expiration.
     *
     * @throws Exception On errors.
     */
    @Test
    public void verifyLockExpiration() throws Exception {
        final String appId = "expquick";
        final String uniqueId = appId + "-1";
        final LockingStrategy lock = newLockTxProxy(appId, uniqueId, 1);
        try {
            assertTrue(lock.acquire());
            assertEquals(uniqueId, getOwner(appId));
            assertFalse(lock.acquire());
            Thread.sleep(1500);
            assertTrue(lock.acquire());
            assertEquals(uniqueId, getOwner(appId));
            lock.release();
            assertNull(getOwner(appId));
        } catch (final Exception e) {
            logger.debug("testLockExpiration produced an error", e);
            fail("testLockExpiration failed");
        }
    }

    /**
     * Verify non-reentrant behavior.
     */
    @Test
    public void verifyNonReentrantBehavior() {
        final String appId = "reentrant";
        final String uniqueId = appId + "-1";
        final LockingStrategy lock = newLockTxProxy(appId, uniqueId, JpaLockingStrategy.DEFAULT_LOCK_TIMEOUT);
        try {
            assertTrue(lock.acquire());
            assertEquals(uniqueId, getOwner(appId));
            assertFalse(lock.acquire());
            lock.release();
            assertNull(getOwner(appId));
        } catch (final Exception e) {
            logger.debug("testNonReentrantBehavior produced an error", e);
            fail("testNonReentrantBehavior failed.");
        }
    }

    /**
     * Test concurrent acquire/release semantics.
     */
    @Test
    public void verifyConcurrentAcquireAndRelease() throws Exception {
        final ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_SIZE);
        try {
            testConcurrency(executor, getConcurrentLocks("concurrent-new"));
        } catch (final Exception e) {
            logger.debug("testConcurrentAcquireAndRelease produced an error", e);
            fail("testConcurrentAcquireAndRelease failed.");
        } finally {
            executor.shutdownNow();
        }
    }

    /**
     * Test concurrent acquire/release semantics for existing lock.
     */
    @Test
    public void verifyConcurrentAcquireAndReleaseOnExistingLock() throws Exception {
        final LockingStrategy[] locks = getConcurrentLocks("concurrent-exists");
        locks[0].acquire();
        locks[0].release();
        final ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_SIZE);
        try {
            testConcurrency(executor, locks);
        } catch (final Exception e) {
            logger.debug("testConcurrentAcquireAndReleaseOnExistingLock produced an error", e);
            fail("testConcurrentAcquireAndReleaseOnExistingLock failed.");
        } finally {
            executor.shutdownNow();
        }
    }

    private LockingStrategy[] getConcurrentLocks(final String appId) {
        final LockingStrategy[] locks = new LockingStrategy[CONCURRENT_SIZE];
        for (int i = 1; i <= locks.length; i++) {
            locks[i - 1] = newLockTxProxy(appId, appId + '-' + i, JpaLockingStrategy.DEFAULT_LOCK_TIMEOUT);
        }
        return locks;
    }

    private LockingStrategy newLockTxProxy(final String appId, final String uniqueId, final int ttl) {
        final JpaLockingStrategy lock = new JpaLockingStrategy();
        lock.entityManager = SharedEntityManagerCreator.createSharedEntityManager(factory);
        lock.setApplicationId(appId);
        lock.setUniqueId(uniqueId);
        lock.setLockTimeout(ttl);
        return (LockingStrategy) Proxy.newProxyInstance(
               JpaLockingStrategy.class.getClassLoader(),
               new Class[] {LockingStrategy.class},
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

    private void testConcurrency(final ExecutorService executor, final LockingStrategy[] locks) throws Exception {
        final List<Locker> lockers = new ArrayList<>(locks.length);
        for (int i = 0; i < locks.length; i++) {
            lockers.add(new Locker(locks[i]));
        }

        int lockCount = 0;
        for (final Future<Boolean> result : executor.invokeAll(lockers)) {
            if (result.get()) {
                lockCount++;
            }
        }
        assertTrue("Lock count should be <= 1 but was " + lockCount, lockCount <= 1);

        final List<Releaser> releasers = new ArrayList<>(locks.length);
        for (int i = 0; i < locks.length; i++) {
            releasers.add(new Releaser(locks[i]));
        }
        int releaseCount = 0;
        for (final Future<Boolean> result : executor.invokeAll(lockers)) {
            if (result.get()) {
                releaseCount++;
            }
        }
        assertTrue("Release count should be <= 1 but was " + releaseCount, releaseCount <= 1);
    }

    private static class TransactionalLockInvocationHandler implements InvocationHandler {
        private final transient Logger logger = LoggerFactory.getLogger(this.getClass());
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
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            return new TransactionTemplate(txManager).execute(new TransactionCallback<Object>() {
                @Override
                public Object doInTransaction(final TransactionStatus status) {
                    try {
                        final Object result = method.invoke(jpaLock, args);
                        jpaLock.entityManager.flush();
                        logger.debug("Performed {} on {}", method.getName(), jpaLock);
                        return result;
                        // Force result of transaction to database
                    } catch (final Exception e) {
                        throw new RuntimeException("Transactional method invocation failed.", e);
                    }
                }
            });
        }

    }

    private static class Locker implements Callable<Boolean> {
        private final transient Logger logger = LoggerFactory.getLogger(this.getClass());
        private final LockingStrategy lock;

        Locker(final LockingStrategy l) {
            lock = l;
        }

        @Override
        public Boolean call() throws Exception {
            try {
                return lock.acquire();
            } catch (final Exception e) {
                logger.debug("{} failed to acquire lock", lock, e);
                return false;
            }
        }
    }

    private static class Releaser implements Callable<Boolean> {
        private final transient Logger logger = LoggerFactory.getLogger(this.getClass());
        private final LockingStrategy lock;

        Releaser(final LockingStrategy l) {
            lock = l;
        }

        @Override
        public Boolean call() throws Exception {
            try {
                lock.release();
                return true;
            } catch (final Exception e) {
                logger.debug("{} failed to release lock", lock, e);
                return false;
            }
        }
    }

}
