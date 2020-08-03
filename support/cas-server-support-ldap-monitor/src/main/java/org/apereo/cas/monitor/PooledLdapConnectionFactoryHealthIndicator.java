package org.apereo.cas.monitor;

import lombok.val;
import org.ldaptive.ConnectionValidator;
import org.ldaptive.PooledConnectionFactory;
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
    private final ConnectionValidator validator;

    public PooledLdapConnectionFactoryHealthIndicator(final long maxWait,
                                                      final PooledConnectionFactory factory,
                                                      final ExecutorService executor,
                                                      final ConnectionValidator validator) {
        super(maxWait, executor);
        this.connectionFactory = factory;
        this.validator = validator;
    }


    @Override
    public void destroy() {
        super.destroy();
        this.connectionFactory.close();
    }

    @Override
    protected Health.Builder checkPool(final Health.Builder builder) throws Exception {
        if (this.connectionFactory != null && this.validator != null) {
            try (val conn = this.connectionFactory.getConnection()) {
                return this.validator.apply(conn) ? builder.up() : builder.down();
            }
        }
        return builder.unknown();
    }

    @Override
    protected int getIdleCount() {
        return this.connectionFactory.availableCount();
    }

    @Override
    protected int getActiveCount() {
        return this.connectionFactory.activeCount();
    }
}
