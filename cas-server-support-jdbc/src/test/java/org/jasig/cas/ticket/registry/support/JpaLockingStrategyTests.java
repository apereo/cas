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
package org.jasig.cas.ticket.registry.support;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
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
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:jpaTestApplicationContext.xml")
@ProfileValueSourceConfiguration(SystemProfileValueSource.class)
public class JpaLockingStrategyTests implements InitializingBean {
    /** Number of clients contending for lock in concurrent test. */
    private static final int CONCURRENT_SIZE = 13;

    /** Logger instance. */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private PlatformTransactionManager txManager;

    @Autowired
    private EntityManagerFactory factory;

    private JdbcTemplate simpleJdbcTemplate;

    /**
     * Set the dataSource.
     */
    @Autowired
    public void setDataSource(final DataSource dataSource) {
        this.simpleJdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * One-time test initialization.
     *
     * @throws Exception On setup errors.
     */
    public void afterPropertiesSet() throws Exception {
        JdbcTestUtils.deleteFromTables(simpleJdbcTemplate, "locks");
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
    @IfProfileValue(name="cas.jpa.concurrent", value="true")
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
    @IfProfileValue(name="cas.jpa.concurrent", value="true")
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
            locks[i - 1] = newLockTxProxy(appId, appId + "-" + i, JpaLockingStrategy.DEFAULT_LOCK_TIMEOUT);
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
        final List<Map<String, Object>> results = simpleJdbcTemplate.queryForList(
                "SELECT unique_id FROM locks WHERE application_id=?", appId);
        if (results.size() == 0) {
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
        private final Logger logger = LoggerFactory.getLogger(this.getClass());
        private final JpaLockingStrategy jpaLock;
        private final PlatformTransactionManager txManager;

        TransactionalLockInvocationHandler(final JpaLockingStrategy lock,
                                      final PlatformTransactionManager txManager) {
            jpaLock = lock;
            this.txManager = txManager;
        }

        public JpaLockingStrategy getLock() {
            return jpaLock;
        }

        /**
     * {@inheritDoc}
     */
        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            return new TransactionTemplate(txManager).execute(new TransactionCallback<Object>() {
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
        private final Logger logger = LoggerFactory.getLogger(this.getClass());
        private final LockingStrategy lock;

        Locker(final LockingStrategy l) {
            lock = l;
        }

        /**
     * {@inheritDoc}
     */
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
        private final Logger logger = LoggerFactory.getLogger(this.getClass());
        private final LockingStrategy lock;

        Releaser(final LockingStrategy l) {
            lock = l;
        }

        /**
     * {@inheritDoc}
     */
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
