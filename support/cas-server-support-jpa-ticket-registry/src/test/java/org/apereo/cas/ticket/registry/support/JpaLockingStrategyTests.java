package org.apereo.cas.ticket.registry.support;

import org.apereo.cas.configuration.model.support.jpa.ticketregistry.JpaTicketRegistryProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.registry.JpaTicketRegistryTests;
import org.apereo.cas.ticket.registry.generic.JpaLockEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.LockModeType;
import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link JpaLockingStrategy}.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@SpringBootTest(classes = JpaTicketRegistryTests.SharedTestConfiguration.class,
    properties = "cas.ticket.registry.cleaner.schedule.enabled=false")
@Slf4j
@Tag("JDBC")
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
        assertTrue(lockCount <= 1, () -> "Lock count should be <= 1 but was " + lockCount);

        val releaseCount = executor.invokeAll(lockers).stream().filter(result -> {
            try {
                return result.get();
            } catch (final InterruptedException | ExecutionException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }).count();
        assertTrue(releaseCount <= 1, () -> "Release count should be <= 1 but was " + releaseCount);
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

    @Test
    public void verifyAcquireFails() {
        val strategy = new JpaLockingStrategy("CAS", UUID.randomUUID().toString(), 0);
        val mgr = mock(EntityManager.class);
        doThrow(new RuntimeException()).when(mgr).persist(any());
        when(mgr.merge(any())).thenThrow(new RuntimeException());
        strategy.setEntityManager(mgr);
        val lock = mock(JpaLockEntity.class);
        assertFalse(strategy.acquire(lock));

        when(mgr.find(any(), anyString(), any(LockModeType.class)))
            .thenThrow(new RuntimeException());
        assertFalse(strategy.acquire());

        when(lock.getExpirationDate()).thenReturn(ZonedDateTime.now(Clock.systemUTC()).minusDays(1));
        when(lock.getUniqueId()).thenReturn(UUID.randomUUID().toString());
        when(mgr.find(any(), anyString(), any(LockModeType.class))).thenReturn(lock);
        assertFalse(strategy.acquire());
    }

    @RequiredArgsConstructor
    private static class TransactionalLockInvocationHandler implements InvocationHandler {

        private final JpaLockingStrategy jpaLock;

        private final PlatformTransactionManager txManager;

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) {
            return new TransactionTemplate(txManager).execute(status -> {
                try {
                    val result = method.invoke(jpaLock, args);
                    jpaLock.getEntityManager().flush();
                    LOGGER.debug("Performed [{}] on [{}]", method.getName(), jpaLock);
                    return result;
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

    private LockingStrategy[] getConcurrentLocks(final String appId) {
        val locks = new LockingStrategy[CONCURRENT_SIZE];
        IntStream.rangeClosed(1, locks.length)
            .forEach(i -> locks[i - 1] = newLockTxProxy(appId, appId + '-' + i, JpaTicketRegistryProperties.DEFAULT_LOCK_TIMEOUT));
        return locks;
    }

    private LockingStrategy newLockTxProxy(final String appId, final String uniqueId, final String ttl) {
        val lock = new JpaLockingStrategy(appId, uniqueId, Beans.newDuration(ttl).getSeconds());
        lock.setEntityManager(SharedEntityManagerCreator.createSharedEntityManager(factory));
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

}
