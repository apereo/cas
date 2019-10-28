package org.apereo.cas.monitor;

import lombok.val;
import org.ldaptive.Connection;
import org.ldaptive.pool.PooledConnectionFactory;
import org.ldaptive.pool.Validator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthContributor;

import java.util.concurrent.ExecutorService;

/**
 * Monitors an ldaptive {@link PooledConnectionFactory}.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class PooledLdapConnectionFactoryHealthIndicator extends AbstractPoolHealthIndicator implements HealthContributor {

    /**
     * Source of connections to validate.
     */
    private final PooledConnectionFactory connectionFactory;

    /**
     * Connection validator.
     */
    private final Validator<Connection> validator;

    public PooledLdapConnectionFactoryHealthIndicator(final long maxWait,
                                                      final PooledConnectionFactory factory,
                                                      final ExecutorService executor,
                                                      final Validator<Connection> validator) {
        super(maxWait, executor);
        this.connectionFactory = factory;
        this.validator = validator;
    }

    @Override
    protected Health.Builder checkPool(final Health.Builder builder) throws Exception {
        if (this.connectionFactory != null && this.validator != null) {
            try (val conn = this.connectionFactory.getConnection()) {
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
