package org.apereo.cas.monitor;

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
     * @param task the task
     */
    void run(MonitorableTask task);

    /**
     * Observe a task as a supplier.
     *
     * @param <T>   the type parameter
     * @param task  the task
     * @param clazz the clazz
     * @return the t
     */
    <T> T supply(MonitorableTask task, Class<T> clazz);
}
