package org.apereo.cas.monitor;

import org.ldaptive.Connection;
import org.ldaptive.pool.PooledConnectionFactory;
import org.ldaptive.pool.Validator;
import org.springframework.boot.actuate.health.Health;

/**
 * Monitors an ldaptive {@link PooledConnectionFactory}.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class PooledLdapConnectionFactoryHealthIndicator extends AbstractPoolHealthIndicator {

    /**
     * Source of connections to validate.
     */
    private final PooledConnectionFactory connectionFactory;

    /**
     * Connection validator.
     */
    private final Validator<Connection> validator;

    /**
     * Creates a new instance that monitors the given pooled connection factory.
     *
     * @param executorService the executor service
     * @param maxWait         the max wait
     * @param factory         Connection factory to monitor.
     * @param validator       Validates connections from the factory.
     */
    public PooledLdapConnectionFactoryHealthIndicator(final int maxWait, final PooledConnectionFactory factory, final Validator<Connection> validator) {
        super(maxWait);
        this.connectionFactory = factory;
        this.validator = validator;
    }

    @Override
    protected Health.Builder checkPool(final Health.Builder builder) throws Exception {
        if (this.connectionFactory != null && this.validator != null) {
            try (Connection conn = this.connectionFactory.getConnection()) {
                return this.validator.validate(conn) ? builder.up() : builder.down();
            }
        }
        return builder.unknown();
    }

    @Override
    protected int getIdleCount() {
        return this.connectionFactory.getConnectionPool().availableCount();
    }

    @Override
    protected int getActiveCount() {
        return this.connectionFactory.getConnectionPool().activeCount();
    }
}
