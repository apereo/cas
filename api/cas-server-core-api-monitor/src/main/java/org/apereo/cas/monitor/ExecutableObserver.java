package org.apereo.cas.monitor;

import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.jooq.lambda.fi.util.function.CheckedSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import java.util.function.Function;

/**
 * This is {@link ExecutableObserver}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@FunctionalInterface
public interface ExecutableObserver {
    /**
     * Logger instance.
     */
    Logger LOGGER = LoggerFactory.getLogger(ExecutableObserver.class);

    /**
     * Bean name.
     */
    String BEAN_NAME = "defaultExecutableObserver";
    
    /**
     * Observe a task as a runnable.
     *
     * @param task     the task
     * @param runnable the runnable
     */
    default void run(final MonitorableTask task, final Runnable runnable) {}

    /**
     * Observe a task as a supplier.
     *
     * @param <T>      the type parameter
     * @param task     the task
     * @param supplier the supplier
     * @return the t
     */
    <T> T supply(MonitorableTask task, CheckedSupplier<T> supplier);

    /**
     * Observe invocation.
     *
     * @param observerProvider the observer provider
     * @param joinPoint        the join point
     * @return the object
     * @throws Throwable the throwable
     */
    static Object observe(final ObjectProvider<ExecutableObserver> observerProvider,
                          final ProceedingJoinPoint joinPoint,
                          final Function<MonitorableTask, MonitorableTask> taskCustomizer) throws Throwable {
        val observer = observerProvider.getIfAvailable();
        if (observer != null) {
            val taskName = joinPoint.getSignature().getDeclaringTypeName() + '.' + joinPoint.getSignature().getName();
            val task = taskCustomizer.apply(new MonitorableTask(taskName));
            return observer.supply(task, () -> executeJoinPoint(joinPoint));
        }
        return executeJoinPoint(joinPoint);
    }

    /**
     * Observe object.
     *
     * @param observerProvider the observer provider
     * @param joinPoint        the join point
     * @return the object
     * @throws Throwable the throwable
     */
    static Object observe(final ObjectProvider<ExecutableObserver> observerProvider,
                          final ProceedingJoinPoint joinPoint) throws Throwable {
        return observe(observerProvider, joinPoint, Function.identity());
    }

    private static Object executeJoinPoint(final ProceedingJoinPoint joinPoint) throws Throwable {
        var args = joinPoint.getArgs();
        LOGGER.trace("Executing [{}]", joinPoint.getStaticPart().toLongString());
        return joinPoint.proceed(args);
    }
}
