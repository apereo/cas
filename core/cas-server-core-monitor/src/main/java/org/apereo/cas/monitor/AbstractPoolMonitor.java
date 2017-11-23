package org.apereo.cas.monitor;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Describes a monitor that observes a pool of resources.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
public abstract class AbstractPoolMonitor extends AbstractNamedMonitor<PoolStatus> {

    /** Maximum amount of time in ms to wait while validating pool resources. */
    private final long maxWait;

    /** Executor that performs pool resource validation. */
    private final ExecutorService executor;

    /**
     * Creates a new instance.
     *
     * @param name monitor name
     * @param executorService executor service responsible for pool resource validation.
     * @param maxWait Set the maximum amount of time wait while validating pool resources.
     * If the pool defines a maximum time to wait for a resource, this property
     * should be set less than that value.
     */
    public AbstractPoolMonitor(final String name, final ExecutorService executorService, final long maxWait) {
        super(name);
        this.executor = executorService;
        this.maxWait = maxWait;
    }

    @Override
    public PoolStatus observe() {
        final Future<StatusCode> result = this.executor.submit(new Validator(this));
        StatusCode code;
        String description = null;
        try {
            code = result.get(this.maxWait, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            code = StatusCode.UNKNOWN;
            description = "Validator thread interrupted during pool validation.";
        } catch (final TimeoutException e) {
            code = StatusCode.WARN;
            description = String.format("Pool validation timed out. Max wait is %s ms.", this.maxWait);
        } catch (final Exception e) {
            code = StatusCode.ERROR;
            description = e.getMessage();
        }
        return new PoolStatus(code, description, getActiveCount(), getIdleCount());
    }

    /**
     * Performs a health check on a the pool.  The recommended implementation is to
     * obtain a pool resource, validate it, and return it to the pool.
     *
     * @return Status code describing pool health.
     *
     * @throws Exception Thrown to indicate a serious problem with pool validation.
     */
    protected abstract StatusCode checkPool() throws Exception;

    /**
     * Gets the number of pool resources idle at present.
     *
     * @return Number of idle pool resources.
     */
    protected int getIdleCount() {
        return PoolStatus.UNKNOWN_COUNT;
    }

    /**
     * Gets the number of pool resources active at present.
     *
     * @return Number of active pool resources.
     */
    protected int getActiveCount() {
        return PoolStatus.UNKNOWN_COUNT;
    }

    private static class Validator implements Callable<StatusCode> {
        private final AbstractPoolMonitor monitor;

        /**
         * Instantiates a new Validator.
         *
         * @param monitor the monitor
         */
        Validator(final AbstractPoolMonitor monitor) {
            this.monitor = monitor;
        }

        @Override
        public StatusCode call() throws Exception {
            return this.monitor.checkPool();
        }
    }
}
