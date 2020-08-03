package org.apereo.cas.configuration.model.support.redis;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link BaseRedisProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-support-redis-core")
@Accessors(chain = true)
public class BaseRedisProperties implements Serializable {

    private static final long serialVersionUID = -2600996981339638782L;

    /**
     * Database index used by the connection factory.
     */
    @RequiredProperty
    private int database;

    /**
     * Redis server host.
     */
    @RequiredProperty
    private String host = "localhost";

    /**
     * Login password of the redis server.
     */
    @RequiredProperty
    private String password;

    /**
     * Redis server port.
     */
    @RequiredProperty
    private int port = 6379;

    /**
     * Connection timeout in milliseconds.
     */
    private int timeout = 2000;

    /**
     * Redis connection pool settings.
     */
    private RedisPoolProperties pool = new RedisPoolProperties();

    /**
     * Redis Sentinel settings.
     */
    @NestedConfigurationProperty
    private RedisSentinelProperties sentinel = new RedisSentinelProperties();

    /**
     * Redis cluster settings.
     */
    @NestedConfigurationProperty
    private RedisClusterProperties cluster = new RedisClusterProperties();

    /**
     * Whether or not to use SSL for connection factory.
     */
    private boolean useSsl;

    /**
     * Setting that describes how Lettuce routes read operations to replica nodes.
     * Accepted mode are :
     * <ul>
     * <li>{@code MASTER}: Default mode. Read from the current master node.</li>
     * <li>{@code MASTER_PREFERRED}: Read from the master, but if it is unavailable, read from replica nodes.</li>
     * <li>{@code REPLICA/SLAVE}: Read from replica nodes. The value REPLICA should be used from lettuce-core version
     * 5.2.</li>
     * <li>{@code REPLICA_PREFERRED/SLAVE_PREFERRED}: Read from the replica nodes, but if none is unavailable, read
     * from the master. The value REPLICA_PREFERRED should be used from lettuce-core version 5.2.</li>
     * <li>{@code NEAREST}: Read from any node of the cluster with the lowest latency.</li>
     * <li>{@code ANY}: Read from any node of the cluster.The value should be used from lettuce-core version 5.2.</li>
     * </ul>
     */
    private String readFrom;
}
