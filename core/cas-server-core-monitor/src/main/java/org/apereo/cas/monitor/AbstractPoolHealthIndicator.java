package org.apereo.cas.monitor;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

import java.util.concurrent.TimeoutException;

/**
 * Describes a monitor that observes a pool of resources.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
public abstract class AbstractPoolHealthIndicator extends AbstractHealthIndicator {

    /**
     * Maximum amount of time in ms to wait while validating pool resources.
     */
    private final long maxWait;

    /**
     * Creates a new instance.
     *
     * @param name            monitor name
     * @param executorService executor service responsible for pool resource validation.
     * @param maxWait         Set the maximum amount of time wait while validating pool resources.
     *                        If the pool defines a maximum time to wait for a resource, this property
     *                        should be set less than that value.
     */
    public AbstractPoolHealthIndicator(final long maxWait) {
        this.maxWait = maxWait;
    }

    @Override
    protected void doHealthCheck(final Health.Builder builder) {
        Health.Builder poolBuilder = builder.up();

        String description;
        try {
            poolBuilder = checkPool(builder);
            description = "OK";
        } catch (final InterruptedException e) {
            description = "Validator thread interrupted during pool validation.";
        } catch (final TimeoutException e) {
            poolBuilder.status("WARN");
            description = String.format("Pool validation timed out. Max wait is %s ms.", this.maxWait);
        } catch (final Exception e) {
            poolBuilder.outOfService();
            description = e.getMessage();
        }
        poolBuilder
            .withDetail("message", description)
            .withDetail("activeCount", getActiveCount())
            .withDetail("idleCount", getIdleCount());
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
}
