package org.jasig.cas.adaptors.x509.authentication.handler.support.ldap;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PreDestroy;
import javax.validation.constraints.NotNull;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.SearchExecutor;
import org.ldaptive.pool.BlockingConnectionPool;
import org.ldaptive.pool.ConnectionPool;
import org.ldaptive.pool.PoolConfig;
import org.ldaptive.pool.PooledConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Fetches a CRL from an LDAP instance.
 * @author Daniel Fisher
 * @since 4.1
 */
@Component("poolingLdaptiveResourceCRLFetcher")
public class PoolingLdaptiveResourceCRLFetcher extends LdaptiveResourceCRLFetcher {

    /** Connection pool template. */
    protected BlockingConnectionPool connectionPool;

    /** Map of connection pools. */
    private final Map<String, PooledConnectionFactory> connectionPoolMap = new HashMap<>();

    /** Serialization support. */
    protected PoolingLdaptiveResourceCRLFetcher() {}

    /**
     * Instantiates a new Ldap resource cRL fetcher.

     * @param connectionConfig the connection configuration
     * @param searchExecutor the search executor
     * @param connectionPool pooling configuration
     */
    public PoolingLdaptiveResourceCRLFetcher(
            @NotNull final ConnectionConfig connectionConfig,
            @NotNull final SearchExecutor searchExecutor,
            @NotNull final BlockingConnectionPool connectionPool) {
        super(connectionConfig, searchExecutor);
        this.connectionPool = connectionPool;
    }

    /**
     * Close connection pull and shut down the executor.
     */
    @PreDestroy
    public void destroy() {
        logger.debug("Shutting down connection pools...");
        for (final PooledConnectionFactory factory : connectionPoolMap.values()) {
            factory.getConnectionPool().close();
        }
    }

    @Override
    protected ConnectionFactory prepareConnectionFactory(final String ldapURL) {
        final PooledConnectionFactory connectionFactory;
        synchronized (connectionPoolMap) {
            if (connectionPoolMap.containsKey(ldapURL)) {
                connectionFactory = connectionPoolMap.get(ldapURL);
            } else {
                connectionFactory = new PooledConnectionFactory(newConnectionPool(ldapURL));
                connectionPoolMap.put(ldapURL, connectionFactory);
            }
        }
        return connectionFactory;
    }


    /**
     * Creates a new instance of a connection pool. Copied from {@link #connectionPool}.
     *
     * @param ldapURL to connect to
     * @return connection pool
     */
    private ConnectionPool newConnectionPool(final String ldapURL) {
        final BlockingConnectionPool pool = new BlockingConnectionPool(
                newPoolConfig(this.connectionPool.getPoolConfig()),
                (DefaultConnectionFactory) super.prepareConnectionFactory(ldapURL));
        pool.setBlockWaitTime(this.connectionPool.getBlockWaitTime());
        pool.setActivator(this.connectionPool.getActivator());
        pool.setPassivator(this.connectionPool.getPassivator());
        pool.setValidator(this.connectionPool.getValidator());
        pool.setConnectOnCreate(this.connectionPool.getConnectOnCreate());
        pool.setFailFastInitialize(this.connectionPool.getFailFastInitialize());
        pool.setName(String.format("x509-crl-%s", ldapURL));
        pool.setPruneStrategy(this.connectionPool.getPruneStrategy());
        pool.initialize();
        return pool;
    }

    /**
     * Creates a new instance of pool config.
     *
     * @param config to copy properties from
     * @return pool config
     */
    private static PoolConfig newPoolConfig(final PoolConfig config) {
        final PoolConfig pc = new PoolConfig();
        pc.setMinPoolSize(config.getMinPoolSize());
        pc.setMaxPoolSize(config.getMaxPoolSize());
        pc.setValidateOnCheckIn(config.isValidateOnCheckIn());
        pc.setValidateOnCheckOut(config.isValidateOnCheckOut());
        pc.setValidatePeriodically(config.isValidatePeriodically());
        pc.setValidatePeriod(config.getValidatePeriod());
        return pc;
    }

    @Autowired(required=false)
    public void setConnectionPool(@Qualifier("poolingLdaptiveConnectionPool")  final BlockingConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Autowired(required = false)
    @Override
    public void setSearchExecutor(@Qualifier("poolingLdaptiveResourceCRLSearchExecutor") final SearchExecutor searchExecutor) {
        super.setSearchExecutor(searchExecutor);
    }

    @Autowired(required=false)
    @Override
    public void setConnectionConfig(@Qualifier("poolingLdaptiveResourceCRLConnectionConfig") final ConnectionConfig connectionConfig) {
        super.setConnectionConfig(connectionConfig);
    }

}
