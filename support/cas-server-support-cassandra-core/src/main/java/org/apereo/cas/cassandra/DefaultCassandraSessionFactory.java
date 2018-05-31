package org.apereo.cas.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.ProtocolOptions;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SocketOptions;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.LoggingRetryPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.model.support.cassandra.authentication.BaseCassandraProperties;
import org.springframework.beans.factory.DisposableBean;

import java.util.Arrays;

/**
 * This is {@link DefaultCassandraSessionFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class DefaultCassandraSessionFactory implements CassandraSessionFactory, AutoCloseable, DisposableBean {


    private final Cluster cluster;
    private final Session session;

    public DefaultCassandraSessionFactory(final BaseCassandraProperties cassandra) {
        this.cluster = initializeCassandraCluster(cassandra);
        this.session = StringUtils.isBlank(cassandra.getKeyspace()) ? cluster.connect() : cluster.connect(cassandra.getKeyspace());
    }

    private static Cluster initializeCassandraCluster(final BaseCassandraProperties cassandra) {
        final Cluster cluster;
        final var poolingOptions = new PoolingOptions()
            .setMaxRequestsPerConnection(HostDistance.LOCAL, cassandra.getMaxRequestsPerConnection())
            .setConnectionsPerHost(HostDistance.LOCAL, cassandra.getCoreConnections(), cassandra.getMaxConnections());

        final var dcPolicyBuilder = DCAwareRoundRobinPolicy.builder();
        if (StringUtils.isNotBlank(cassandra.getLocalDc())) {
            dcPolicyBuilder.withLocalDc(cassandra.getLocalDc());
        }

        final var loadBalancingPolicy = new TokenAwarePolicy(dcPolicyBuilder.build(), cassandra.isShuffleReplicas());

        final var socketOptions = new SocketOptions()
            .setConnectTimeoutMillis(cassandra.getConnectTimeoutMillis())
            .setReadTimeoutMillis(cassandra.getReadTimeoutMillis());

        final var queryOptions = new QueryOptions()
            .setConsistencyLevel(ConsistencyLevel.valueOf(cassandra.getConsistencyLevel()))
            .setSerialConsistencyLevel(ConsistencyLevel.valueOf(cassandra.getSerialConsistencyLevel()));

        final var retryPolicy = RetryPolicyType.valueOf(cassandra.getRetryPolicy()).getRetryPolicy();
        final var builder =
            Cluster.builder()
                .withCredentials(cassandra.getUsername(), cassandra.getPassword())
                .withPoolingOptions(poolingOptions)
                .withProtocolVersion(ProtocolVersion.valueOf(cassandra.getProtocolVersion()))
                .withLoadBalancingPolicy(loadBalancingPolicy)
                .withSocketOptions(socketOptions)
                .withRetryPolicy(new LoggingRetryPolicy(retryPolicy))
                .withCompression(ProtocolOptions.Compression.valueOf(cassandra.getCompression()))
                .withPort(cassandra.getPort())
                .withQueryOptions(queryOptions);

        Arrays.stream(StringUtils.split(cassandra.getContactPoints(), ','))
            .forEach(contactPoint -> builder.addContactPoint(StringUtils.trim(contactPoint)));

        cluster = builder.build();

        if (LOGGER.isDebugEnabled()) {
            cluster.getMetadata().getAllHosts().forEach(clusterHost ->
                LOGGER.debug("Host [{}]:\n\n\tDC: [{}]\n\tRack: [{}]\n\tVersion: [{}]\n\tDistance: [{}]\n\tUp?: [{}]\n",
                    clusterHost.getAddress(), clusterHost.getDatacenter(), clusterHost.getRack(),
                    clusterHost.getCassandraVersion(), loadBalancingPolicy.distance(clusterHost), clusterHost.isUp()));
        }
        return cluster;
    }

    @Override
    public Session getSession() {
        return this.session;
    }

    /**
     * Destroy.
     */
    @Override
    public void destroy() {
        try {
            LOGGER.debug("Closing Cassandra session");
            session.close();
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
        try {
            LOGGER.debug("Closing Cassandra cluster");
            cluster.close();
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        destroy();
    }
}
