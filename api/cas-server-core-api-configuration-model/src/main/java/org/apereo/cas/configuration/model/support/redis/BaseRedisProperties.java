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
     * Whether the module is enabled or not, defaults to true.
     */
    @RequiredProperty
    private boolean enabled = true;

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
    @NestedConfigurationProperty
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
     * <li>{@code UPSTREAM/MASTER}: Default mode. Read from the current upstream (master) node.</li>
     * <li>{@code UPSTREAMPREFERRED/MASTERPREFERRED}: Read from the upstream node (master), but if it is unavailable, read from replica nodes.</li>
     * <li>{@code REPLICA/SLAVE}: Read from replica nodes. </li>
     * <li>{@code REPLICAPREFERRED/SLAVEPREFERRED}: Read from the replica nodes, but if none is unavailable, read
     * from the upstream (master) node.</li>
     * <li>{@code NEAREST}: Read from any node of the cluster with the lowest latency.</li>
     * <li>{@code ANY}: Read from any node of the cluster.</li>
     * <li>{@code ANYREPLICA}: Read from any replica node of the cluster. </li>
     * </ul>
     * Note that modes referencing MASTER/SLAVE are deprecated in the Lettuce-io redis client dependency so migrate config to UPSTREAM/REPLICA.
     */
    private String readFrom;
}
