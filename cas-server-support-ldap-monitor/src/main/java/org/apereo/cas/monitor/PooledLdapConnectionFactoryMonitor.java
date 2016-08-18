package org.apereo.cas.monitor;

import org.ldaptive.Connection;
import org.ldaptive.pool.PooledConnectionFactory;
import org.ldaptive.pool.Validator;

/**
 * Monitors an ldaptive {@link PooledConnectionFactory}.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class PooledLdapConnectionFactoryMonitor extends AbstractPoolMonitor {

    /** Source of connections to validate. */
    private PooledConnectionFactory connectionFactory;

    /** Connection validator. */
    private Validator<Connection> validator;


    /**
     * Instantiates a new Pooled connection factory monitor.
     */
    public PooledLdapConnectionFactoryMonitor() {}

    /**
     * Creates a new instance that monitors the given pooled connection factory.
     *
     * @param  factory  Connection factory to monitor.
     * @param  validator  Validates connections from the factory.
     */
    public PooledLdapConnectionFactoryMonitor(
            final PooledConnectionFactory factory, final Validator<Connection> validator) {
        this.connectionFactory = factory;
        this.validator = validator;
    }


    @Override
    protected StatusCode checkPool() throws Exception {
        if (this.connectionFactory != null && this.validator != null) {
            try(Connection conn = this.connectionFactory.getConnection()) {
                return this.validator.validate(conn) ? StatusCode.OK : StatusCode.ERROR;
            }
        }
        return StatusCode.UNKNOWN;
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
