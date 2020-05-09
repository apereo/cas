package org.apereo.cas.monitor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Describes a monitor that observes a pool of resources.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractPoolHealthIndicator extends AbstractHealthIndicator implements DisposableBean {

    /**
     * Maximum amount of time in ms to wait while validating pool resources.
     */
    private final long maxWait;

    /**
     * Executor that performs pool resource validation.
     */
    private final ExecutorService executor;

    @Override
    protected void doHealthCheck(final Health.Builder builder) {
        var poolBuilder = builder.up();
        var message = StringUtils.EMPTY;
        try {
            val result = this.executor.submit(new Validator(this, builder));
            poolBuilder = result.get(this.maxWait, TimeUnit.MILLISECONDS);
            message = "OK";
        } catch (final TimeoutException e) {
            poolBuilder.down();
            message = String.format("Pool validation timed out. Max wait is %s ms.", this.maxWait);
            LOGGER.trace(e.getMessage(), e);
        } catch (final Exception e) {
            poolBuilder.outOfService();
            message = e.getMessage();
            LOGGER.trace(e.getMessage(), e);
        }
        poolBuilder
            .withDetail("message", message)
            .withDetail("name", getClass().getSimpleName())
            .withDetail("activeCount", getActiveCount())
            .withDetail("idleCount", getIdleCount());
    }

    /**
     * Shuts down the thread pool. Subclasses whose wish to override this method
     * must call super.destroy().
     */
    @Override
    public void destroy() {
        executor.shutdown();
    }

    /**
     * Performs a health check on a the pool.  The recommended implementation is to
     * obtain a pool resource, validate it, and return it to the pool.
     *
     * @param builder the builder
     * @return Status code describing pool health.
     * @throws Exception Thrown to indicate a serious problem with pool validation.
     */
    protected abstract Health.Builder checkPool(Health.Builder builder) throws Exception;

    /**
     * Gets the number of pool resources idle at present.
     *
     * @return Number of idle pool resources.
     */
    protected int getIdleCount() {
        return -1;
    }

    /**
     * Gets the number of pool resources active at present.
     *
     * @return Number of active pool resources.
     */
    protected int getActiveCount() {
        return -1;
    }

    private static class Validator implements Callable<Health.Builder> {
        private final AbstractPoolHealthIndicator monitor;

        private final Health.Builder builder;

        /**
         * Instantiates a new Validator.
         *
         * @param monitor the monitor
         * @param builder the health check builder
         */
        Validator(final AbstractPoolHealthIndicator monitor, final Health.Builder builder) {
            this.monitor = monitor;
            this.builder = builder;
        }

        @Override
        public Health.Builder call() throws Exception {
            return this.monitor.checkPool(this.builder);
        }
    }
}
