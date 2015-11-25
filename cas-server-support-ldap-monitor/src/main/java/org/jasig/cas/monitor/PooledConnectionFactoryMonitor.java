package org.jasig.cas.monitor;

import org.ldaptive.Connection;
import org.ldaptive.pool.PooledConnectionFactory;
import org.ldaptive.pool.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

/**
 * Monitors an ldaptive {@link PooledConnectionFactory}.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Component("pooledLdapConnectionFactoryMonitor")
public class PooledConnectionFactoryMonitor extends AbstractPoolMonitor {

    /** Source of connections to validate. */
    @Nullable
    @Autowired(required=false)
    @Qualifier("pooledConnectionFactoryMonitorConnectionFactory")
    private PooledConnectionFactory connectionFactory;

    /** Connection validator. */
    @Nullable
    @Autowired(required=false)
    @Qualifier("pooledConnectionFactoryMonitorValidator")
    private Validator<Connection> validator;


    /**
     * Instantiates a new Pooled connection factory monitor.
     */
    public PooledConnectionFactoryMonitor() {}

    /**
     * Creates a new instance that monitors the given pooled connection factory.
     *
     * @param  factory  Connection factory to monitor.
     * @param  validator  Validates connections from the factory.
     */
    public PooledConnectionFactoryMonitor(
            final PooledConnectionFactory factory, final Validator<Connection> validator) {
        this.connectionFactory = factory;
        this.validator = validator;
    }


    @Override
    protected StatusCode checkPool() throws Exception {
        if (this.connectionFactory != null && this.validator != null) {
            try (final Connection conn = this.connectionFactory.getConnection()) {
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
