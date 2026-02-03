package org.apereo.cas.util.concurrent;

import module java.base;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.fi.util.function.CheckedConsumer;
import org.jooq.lambda.fi.util.function.CheckedSupplier;

/**
 * This is {@link CasReentrantLock}.
 * <p>
 * When a method is synchronized, only one thread would be allowed to enter that method at a time.
 * If you are using a virtual thread in this scenario, when the thread is moved to BLOCKED state,
 * ideally it should relinquish its control of the underlying OS thread and move back to the heap memory.
 * However in the current virtual thread implementation, when a virtual thread gets
 * BLOCKED because of synchronized method (or block), it will not relinquish its control over the underlying
 * OS thread. Thus, you would not gain the benefits of switching to virtual threads.
 * <p>
 * The following components provides an alternative to synchronized locking with {@link ReentrantLock}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
public class CasReentrantLock {
    private static final int LOCK_TIMEOUT_SECONDS = 3;

    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Acquires the lock if it is not held by another thread within the given
     * waiting time and the current thread has not been
     * {@linkplain Thread#interrupt interrupted}.
     *
     * @return true or false
     */
    public boolean tryLock() {
        return FunctionUtils.doAndHandle(
            () -> lock.tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS),
            e -> {
                Thread.currentThread().interrupt();
                return false;
            }).get();
    }

    /**
     * Acquires the lock if it is not held by another thread within the given
     * waiting time and the current thread has not been
     * {@linkplain Thread#interrupt interrupted}. Then, execute the given supplier.
     *
     * @param <T>      the type parameter
     * @param supplier the supplier
     * @return the result of the supplier
     */
    public <T> T tryLock(final CheckedSupplier<T> supplier) {
        if (tryLock()) {
            try {
                return supplier.get();
            } catch (final Throwable e) {
                LoggingUtils.error(LOGGER, e);
                throw new RuntimeException(e);
            } finally {
                unlock();
            }
        }
        return null;
    }

    /**
     * Acquires the lock if it is not held by another thread within the given
     * waiting time and the current thread has not been
     * {@linkplain Thread#interrupt interrupted}. Then, execute the given consumer.
     *
     * @param <T>      the type parameter
     * @param consumer the consumer
     */
    public <T> void tryLock(final CheckedConsumer<T> consumer) {
        if (tryLock()) {
            try {
                consumer.accept(null);
            } catch (final Throwable e) {
                LoggingUtils.error(LOGGER, e);
                throw new RuntimeException(e);
            } finally {
                unlock();
            }
        }
    }

    /**
     * Attempts to release this lock.
     */
    public void unlock() {
        lock.unlock();
    }
}
