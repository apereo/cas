package org.apereo.cas.monitor;

import lombok.extern.slf4j.Slf4j;
import org.ldaptive.Connection;
import org.ldaptive.pool.PooledConnectionFactory;
import org.ldaptive.pool.Validator;
import org.springframework.boot.actuate.health.Health;

import java.util.concurrent.ExecutorService;

/**
 * Monitors an ldaptive {@link PooledConnectionFactory}.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Slf4j
public class PooledLdapConnectionFactoryHealthIndicator extends AbstractPoolHealthIndicator {

    /**
     * Source of connections to validate.
     */
    private final PooledConnectionFactory connectionFactory;

    /**
     * Connection validator.
     */
    private final Validator<Connection> validator;

    public PooledLdapConnectionFactoryHealthIndicator(final int maxWait,
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
