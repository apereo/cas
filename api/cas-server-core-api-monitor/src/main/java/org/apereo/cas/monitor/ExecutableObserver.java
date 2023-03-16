package org.apereo.cas.monitor;

import java.util.function.Supplier;

/**
 * This is {@link ExecutableObserver}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public interface ExecutableObserver {
    /**
     * Observe a task as a runnable.
     *
     * @param task     the task
     * @param runnable the runnable
     * @throws Throwable the throwable
     */
    void run(MonitorableTask task, Runnable runnable) throws Throwable;

    /**
     * Observe a task as a supplier.
     *
     * @param <T>      the type parameter
     * @param task     the task
     * @param supplier the supplier
     * @return the t
     * @throws Throwable the throwable
     */
    <T> T supply(MonitorableTask task, Supplier<T> supplier) throws Throwable;
}
