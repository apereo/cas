package org.apereo.cas.configuration.model.support.redis;

import org.apereo.cas.configuration.support.DurationCapable;
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
     * Command timeout.
     */
    @DurationCapable
    private String timeout = "PT60S";

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
     * Connection timeout.
     */
    @DurationCapable
    private String connectTimeout = "PT10S";

    /**
     * Setting that describes how Lettuce routes read operations to replica nodes.
     * Note that modes referencing MASTER/SLAVE are deprecated (but still supported) in the Lettuce redis client dependency
     * so migrate config to UPSTREAM/REPLICA.
     */
    private RedisReadFromTypes readFrom;

    /**
     * The Lettuce library {@code ReadFrom} types that determine how Lettuce routes read operations to replica nodes.
     */
    public enum RedisReadFromTypes {
        /**
         * Read from the current upstream node.
         */
        UPSTREAM,
        /**
         * Read from the upstream node, but if it is unavailable, read from replica nodes.
         */
        UPSTREAMPREFERRED,
        /**
         * Read from the current upstream node.
         * @deprecated Use {@link org.apereo.cas.configuration.model.support.redis.BaseRedisProperties.RedisReadFromTypes#UPSTREAM} instead.
         */
        @Deprecated
        MASTER,
        /**
         * Read from the upstream node, but if it is unavailable, read from replica nodes.
         * @deprecated Use {@link org.apereo.cas.configuration.model.support.redis.BaseRedisProperties.RedisReadFromTypes#UPSTREAMPREFERRED} instead.
         */
        @Deprecated
        MASTERPREFERRED,
        /**
         * Read from replica nodes.
         * @deprecated Use {@link org.apereo.cas.configuration.model.support.redis.BaseRedisProperties.RedisReadFromTypes#REPLICA} instead.
         */
        @Deprecated
        SLAVE,
        /**
         *  Read from the replica nodes, but if none is unavailable, read from the upstream node.
         * @deprecated Use {@link org.apereo.cas.configuration.model.support.redis.BaseRedisProperties.RedisReadFromTypes#REPLICAPREFERRED} instead.
         */
        @Deprecated
        SLAVEPREFERRED,
        /**
         * Read from replica nodes.
         */
        REPLICA,
        /**
         * Read from the replica nodes, but if none is unavailable, read from the upstream node.
         */
        REPLICAPREFERRED,
        /**
         * Read from any node of the cluster.
         */
        ANY,
        /**
         * Read from any replica node of the cluster.
         */
        ANYREPLICA,
        /**
         * Read from the nearest node.
         */
        NEAREST
    }
}
